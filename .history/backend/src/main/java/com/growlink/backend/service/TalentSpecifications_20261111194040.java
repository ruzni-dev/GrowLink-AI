package com.growlink.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.growlink.backend.entity.Education;
import com.growlink.backend.entity.ProfileVisibility;
import com.growlink.backend.entity.Skill;
import com.growlink.backend.entity.TalentCategory;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserProfile;
import com.growlink.backend.entity.UserSkill;
import com.growlink.backend.entity.UserStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

final class TalentSpecifications {
    private TalentSpecifications() {}

    static Specification<User> matching(Long id, String name, String skill, TalentCategory category,
            String location, String proficiencyLevel, Integer minimumExperience, String education) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("status"), UserStatus.ACTIVE));
            predicates.add(publicProfile(query, root, builder, null));

            if (id != null) predicates.add(builder.equal(root.get("id"), id));
            if (hasText(name)) {
                String pattern = contains(name);
                predicates.add(builder.like(builder.lower(root.get("fullName")), pattern));
            }
            if (hasText(location)) {
                String pattern = contains(location);
                Predicate accountLocation = builder.like(builder.lower(root.get("location")), pattern);
                predicates.add(builder.or(accountLocation, publicProfile(query, root, builder, pattern)));
            }
            if (hasText(skill) || category != null || hasText(proficiencyLevel) || minimumExperience != null) {
                Subquery<Integer> skillQuery = query.subquery(Integer.class);
                Root<UserSkill> userSkill = skillQuery.from(UserSkill.class);
                Join<UserSkill, Skill> skillEntity = userSkill.join("skill");
                List<Predicate> skillPredicates = new ArrayList<>();
                skillPredicates.add(builder.equal(userSkill.get("user").get("id"), root.get("id")));
                if (hasText(skill)) skillPredicates.add(builder.like(builder.lower(skillEntity.get("name")), contains(skill)));
                if (category != null) {
                    List<String> labels = category.getStoredLabels().stream().map(label -> label.toLowerCase(Locale.ROOT)).toList();
                    skillPredicates.add(builder.lower(skillEntity.get("category")).in(labels));
                }
                if (hasText(proficiencyLevel)) skillPredicates.add(builder.equal(userSkill.get("proficiencyLevel").as(String.class), proficiencyLevel));
                if (minimumExperience != null) skillPredicates.add(builder.greaterThanOrEqualTo(userSkill.get("yearsOfExperience"), minimumExperience));
                skillQuery.select(builder.literal(1)).where(builder.and(skillPredicates.toArray(Predicate[]::new)));
                predicates.add(builder.exists(skillQuery));
            }
            if (hasText(education)) {
                Subquery<Integer> educationQuery = query.subquery(Integer.class);
                Root<Education> educationEntity = educationQuery.from(Education.class);
                String pattern = contains(education);
                educationQuery.select(builder.literal(1)).where(
                        builder.equal(educationEntity.get("user").get("id"), root.get("id")),
                        builder.or(
                                builder.like(builder.lower(educationEntity.get("qualification")), pattern),
                                builder.like(builder.lower(educationEntity.get("fieldOfStudy")), pattern),
                                builder.like(builder.lower(educationEntity.get("institutionName")), pattern)));
                predicates.add(builder.exists(educationQuery));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate publicProfile(jakarta.persistence.criteria.CriteriaQuery<?> query, Root<User> user,
            jakarta.persistence.criteria.CriteriaBuilder builder, String locationPattern) {
        Subquery<Integer> profileQuery = query.subquery(Integer.class);
        Root<UserProfile> profile = profileQuery.from(UserProfile.class);
        List<Predicate> profilePredicates = new ArrayList<>();
        profilePredicates.add(builder.equal(profile.get("user").get("id"), user.get("id")));
        profilePredicates.add(builder.equal(profile.get("visibility"), ProfileVisibility.PUBLIC));
        if (locationPattern != null) profilePredicates.add(builder.like(builder.lower(profile.get("location")), locationPattern));
        profileQuery.select(builder.literal(1)).where(builder.and(profilePredicates.toArray(Predicate[]::new)));
        return builder.exists(profileQuery);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String contains(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}