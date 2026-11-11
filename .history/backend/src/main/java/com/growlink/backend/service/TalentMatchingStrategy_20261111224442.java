package com.growlink.backend.service;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.growlink.backend.dto.PublicTalentResponse;
import com.growlink.backend.dto.PublicSkillResponse;

@Component
public class TalentMatchingStrategy {
    public MatchingAnalysis analyze(RecommendationContext context, PublicTalentResponse talent) {
        Map<MatchingDimension, Double> scores = new EnumMap<>(MatchingDimension.class);
        Map<MatchingDimension, String> evidence = new EnumMap<>(MatchingDimension.class);
        Set<String> talentSkills = talent.skills().stream().map(PublicSkillResponse::name)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<String> matchedSkills = MatchingText.matchedSkills(context.desiredSkills(), talentSkills);
        add(scores, evidence, MatchingDimension.SKILL, MatchingText.skillScore(context.desiredSkills(), talentSkills),
                matchedSkills.isEmpty() ? null : "Their public profile includes " + String.join(", ", matchedSkills) + ".");

        Set<String> talentCategories = talent.skills().stream().map(PublicSkillResponse::category)
                .filter(value -> value != null && !value.isBlank()).collect(java.util.stream.Collectors.toSet());
        Set<String> categoryContext = new LinkedHashSet<>(context.categories());
        categoryContext.addAll(context.interests());
        Double categoryScore = categoryContext.isEmpty() || talentCategories.isEmpty() ? null
                : MatchingText.textOverlap(String.join(" ", talentCategories), categoryContext);
        add(scores, evidence, MatchingDimension.CATEGORY, categoryScore,
                categoryScore != null && categoryScore > 0 ? "Their expertise category aligns with your interests." : null);

        String education = talent.education().stream().map(item -> item.qualification() + " " + item.fieldOfStudy())
                .reduce((a, b) -> a + " " + b).orElse("");
        Double educationScore = MatchingText.textOverlap(education, context.educationTerms());
        add(scores, evidence, MatchingDimension.EDUCATION, educationScore,
                educationScore != null && educationScore > 0 ? "Your learning backgrounds share relevant subjects." : null);

        String experience = talent.workExperience().stream().map(item -> item.jobTitle() + " " + item.organization() + " " + item.description())
                .reduce((a, b) -> a + " " + b).orElse("");
        Double experienceScore = MatchingText.textOverlap(experience, context.learningGoals());
        add(scores, evidence, MatchingDimension.EXPERIENCE, experienceScore,
                experienceScore != null && experienceScore > 0 ? "Their public experience connects to your learning goals." : null);

        String expectedLocation = context.preferredLocations().isEmpty() ? context.location() : context.preferredLocations().iterator().next();
        Double locationScore = MatchingText.locationScore(expectedLocation, talent.location());
        add(scores, evidence, MatchingDimension.LOCATION, locationScore,
                locationScore != null && locationScore > 0 ? "Their public location matches your preference." : null);

        String interests = talent.interests().stream().map(item -> item.name()).reduce((a, b) -> a + " " + b).orElse("");
        Double interestScore = MatchingText.textOverlap(interests, context.interests());
        add(scores, evidence, MatchingDimension.INTEREST, interestScore,
                interestScore != null && interestScore > 0 ? "You share interests listed on your profiles." : null);
        return new MatchingAnalysis(scores, matchedSkills, evidence);
    }

    private void add(Map<MatchingDimension, Double> scores, Map<MatchingDimension, String> evidence,
            MatchingDimension dimension, Double score, String reason) {
        if (score == null) return;
        scores.put(dimension, score);
        if (reason != null && !reason.isBlank()) evidence.put(dimension, reason);
    }
}