package com.growlink.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.growlink.backend.entity.MentorshipOffer;
import com.growlink.backend.entity.MentorshipOfferStatus;
import com.growlink.backend.entity.ProfileVisibility;
import com.growlink.backend.entity.Skill;
import com.growlink.backend.entity.TalentCategory;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserProfile;
import com.growlink.backend.entity.UserStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

final class MentorshipSpecifications {
    private MentorshipSpecifications() {}

    static Specification<MentorshipOffer> publicOffers(String queryText, String categoryValue, String location) {
        TalentCategory category = parseCategory(categoryValue);
        return (root, query, builder) -> {
            Join<MentorshipOffer, User> mentor = root.join("mentor");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("status"), MentorshipOfferStatus.ACTIVE));
            predicates.add(builder.equal(mentor.get("status"), UserStatus.ACTIVE));
            predicates.add(publicProfile(query, root, builder, null));

            if (category != null) predicates.add(builder.equal(root.get("category"), category));
            if (hasText(location)) {
                String pattern = contains(location);
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("location")), pattern),
                        publicProfile(query, root, builder, pattern)));
            }
            if (hasText(queryText)) {
                String pattern = contains(queryText);
                Predicate textMatch = builder.or(
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern),
                        builder.like(builder.lower(root.get("experience")), pattern),
                        builder.like(builder.lower(mentor.get("fullName")), pattern),
                        matchingSkill(query, root, builder, pattern));
                predicates.add(textMatch);
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate matchingSkill(jakarta.persistence.criteria.CriteriaQuery<?> query,
            Root<MentorshipOffer> offer, jakarta.persistence.criteria.CriteriaBuilder builder, String pattern) {
        Subquery<Integer> skillQuery = query.subquery(Integer.class);
        Root<MentorshipOffer> correlatedOffer = skillQuery.correlate(offer);
        Join<MentorshipOffer, Skill> skill = correlatedOffer.join("skills");
        skillQuery.select(builder.literal(1)).where(builder.like(builder.lower(skill.get("name")), pattern));
        return builder.exists(skillQuery);
    }

    private static Predicate publicProfile(jakarta.persistence.criteria.CriteriaQuery<?> query,
            Root<MentorshipOffer> offer, jakarta.persistence.criteria.CriteriaBuilder builder, String locationPattern) {
        Subquery<Integer> profileQuery = query.subquery(Integer.class);
        Root<UserProfile> profile = profileQuery.from(UserProfile.class);
        List<Predicate> profilePredicates = new ArrayList<>();
        profilePredicates.add(builder.equal(profile.get("user").get("id"), offer.get("mentor").get("id")));
        profilePredicates.add(builder.equal(profile.get("visibility"), ProfileVisibility.PUBLIC));
        if (locationPattern != null) profilePredicates.add(builder.like(builder.lower(profile.get("location")), locationPattern));
        profileQuery.select(builder.literal(1)).where(builder.and(profilePredicates.toArray(Predicate[]::new)));
        return builder.exists(profileQuery);
    }

    private static TalentCategory parseCategory(String value) {
        if (!hasText(value)) return null;
        try {
            return TalentCategory.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new com.growlink.backend.exception.InvalidProfileDataException(exception.getMessage());
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String contains(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}