package com.growlink.backend.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.growlink.backend.entity.Opportunity;
import com.growlink.backend.entity.OpportunityCategory;
import com.growlink.backend.entity.RemotePreference;

@Component
public class OpportunityMatchingStrategy {
    public MatchingAnalysis analyze(RecommendationContext context, Opportunity opportunity) {
        Map<MatchingDimension, Double> scores = new EnumMap<>(MatchingDimension.class);
        Map<MatchingDimension, String> evidence = new EnumMap<>(MatchingDimension.class);
        Set<String> matchedSkills = new LinkedHashSet<>();
        Set<String> requiredSkills = opportunity.getRequiredSkills();
        Set<String> desiredSkills = context.desiredSkills();

        Double skillScore = MatchingText.skillScore(desiredSkills, requiredSkills);
        Set<String> foundSkills = MatchingText.matchedSkills(desiredSkills, requiredSkills);
        matchedSkills.addAll(foundSkills);
        add(scores, evidence, MatchingDimension.SKILL, skillScore,
                foundSkills.isEmpty() ? null : "Your profile matches these required skills: " + String.join(", ", foundSkills) + ".");

        String category = opportunity.getCategory().name();
        Double categoryScore = categoryScore(context, category, opportunity.getCategory().name());
        add(scores, evidence, MatchingDimension.CATEGORY, categoryScore,
                categoryScore != null && categoryScore > 0 ? "Matches a category or interest you selected." : null);

        Double educationScore = MatchingText.textOverlap(opportunity.getQualification(), context.educationTerms());
        add(scores, evidence, MatchingDimension.EDUCATION, educationScore,
                educationScore != null && educationScore > 0 ? "Your education overlaps the listed qualification." : null);

        String experienceRequirement = join(opportunity.getOpportunityType(), opportunity.getQualification(), opportunity.getDescription());
        Double experienceScore = MatchingText.textOverlap(experienceRequirement, context.experienceTerms());
        add(scores, evidence, MatchingDimension.EXPERIENCE, experienceScore,
                experienceScore != null && experienceScore > 0 ? "Your work experience shares relevant terms with this opportunity." : null);

        String expectedLocation = first(context.preferredLocations(), context.location());
        Double locationScore = opportunity.isRemote() && expectedLocation != null ? 1d
                : MatchingText.locationScore(expectedLocation, opportunity.getLocation());
        add(scores, evidence, MatchingDimension.LOCATION, locationScore,
                locationScore != null && locationScore > 0 ? opportunity.isRemote()
                        ? "Remote format is compatible with your location preferences."
                        : "The opportunity location matches your preferences." : null);

        if (context.remotePreference() != RemotePreference.ANY) {
            boolean compatible = context.remotePreference() == RemotePreference.REMOTE_ONLY
                    ? opportunity.isRemote() : !opportunity.isRemote();
            add(scores, evidence, MatchingDimension.REMOTE, compatible ? 1d : 0d,
                    compatible ? "Matches your remote/on-site preference." : null);
        }

        List<String> eligibilityTerms = new ArrayList<>(context.educationTerms());
        eligibilityTerms.addAll(context.currentSkills());
        Double eligibilityScore = MatchingText.textOverlap(opportunity.getEligibility(), eligibilityTerms);
        add(scores, evidence, MatchingDimension.ELIGIBILITY, eligibilityScore,
                eligibilityScore != null && eligibilityScore > 0 ? "Your listed education or skills align with eligibility details." : null);

        String opportunityText = join(opportunity.getTitle(), opportunity.getDescription(), category,
                String.join(" ", requiredSkills), opportunity.getOrganizationName());
        Double interestScore = MatchingText.textOverlap(opportunityText, context.interests());
        add(scores, evidence, MatchingDimension.INTEREST, interestScore,
                interestScore != null && interestScore > 0 ? "Relevant to interests in your profile or preferences." : null);
        Double goalsScore = MatchingText.textOverlap(opportunityText, context.learningGoals());
        add(scores, evidence, MatchingDimension.GOALS, goalsScore,
                goalsScore != null && goalsScore > 0 ? "Connects with a learning goal you added." : null);

        return new MatchingAnalysis(scores, matchedSkills, evidence);
    }

    private Double categoryScore(RecommendationContext context, String enumValue, String label) {
        Set<String> preferences = new LinkedHashSet<>(context.categories());
        preferences.addAll(context.interests());
        if (preferences.isEmpty()) return null;
        Set<String> categoryTerms = Set.of(enumValue, label, categoryAlias(enumValue));
        return MatchingText.textOverlap(String.join(" ", categoryTerms), preferences);
    }

    private String categoryAlias(String value) {
        return switch (OpportunityCategory.valueOf(value)) {
            case JOB, INTERNSHIP -> "CAREER_PROFESSIONAL_DEVELOPMENT";
            case SCHOLARSHIP, TRAINING -> "EDUCATION_LEARNING";
            case VOLUNTEERING -> "VOLUNTEERING_SOCIAL_SERVICES";
            case COMPETITION, HACKATHON -> "TECHNOLOGY_DIGITAL_SKILLS";
            case EVENT, OTHER -> value;
        };
    }

    private String first(Set<String> preferredLocations, String location) {
        return preferredLocations.isEmpty() ? location : preferredLocations.iterator().next();
    }

    private String join(String... values) {
        return java.util.Arrays.stream(values).filter(value -> value != null && !value.isBlank()).reduce((a, b) -> a + " " + b).orElse("");
    }

    private void add(Map<MatchingDimension, Double> scores, Map<MatchingDimension, String> evidence,
            MatchingDimension dimension, Double score, String reason) {
        if (score == null) return;
        scores.put(dimension, score);
        if (reason != null && !reason.isBlank()) evidence.put(dimension, reason);
    }
}