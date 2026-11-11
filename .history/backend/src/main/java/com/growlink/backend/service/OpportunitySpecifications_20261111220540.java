package com.growlink.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.growlink.backend.entity.Opportunity;
import com.growlink.backend.entity.OpportunityCategory;
import com.growlink.backend.entity.OpportunityStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

final class OpportunitySpecifications {
    private OpportunitySpecifications() {}

    static Specification<Opportunity> discover(String keyword, OpportunityCategory category, String location,
            Boolean remote, String organization, LocalDate deadlineAfter, LocalDate deadlineBefore) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("status"), OpportunityStatus.PUBLISHED));
            predicates.add(builder.or(builder.isNull(root.get("deadline")),
                    builder.greaterThanOrEqualTo(root.get("deadline"), LocalDate.now())));

            if (category != null) predicates.add(builder.equal(root.get("category"), category));
            if (remote != null) predicates.add(builder.equal(root.get("remote"), remote));
            if (hasText(location)) predicates.add(builder.like(builder.lower(root.get("location")), contains(location)));
            if (hasText(organization)) predicates.add(builder.like(builder.lower(root.get("organizationName")), contains(organization)));
            if (deadlineAfter != null) predicates.add(builder.greaterThanOrEqualTo(root.get("deadline"), deadlineAfter));
            if (deadlineBefore != null) predicates.add(builder.lessThanOrEqualTo(root.get("deadline"), deadlineBefore));
            if (hasText(keyword)) {
                String pattern = contains(keyword);
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern),
                        builder.like(builder.lower(root.get("organizationName")), pattern),
                        builder.like(builder.lower(root.get("opportunityType")), pattern),
                        matchingSkill(query, root, builder, pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate matchingSkill(jakarta.persistence.criteria.CriteriaQuery<?> query,
            Root<Opportunity> opportunity, jakarta.persistence.criteria.CriteriaBuilder builder, String pattern) {
        Subquery<Integer> skillQuery = query.subquery(Integer.class);
        Root<Opportunity> correlated = skillQuery.correlate(opportunity);
        Join<Opportunity, String> skill = correlated.join("requiredSkills");
        skillQuery.select(builder.literal(1)).where(builder.like(builder.lower(skill), pattern));
        return builder.exists(skillQuery);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String contains(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}