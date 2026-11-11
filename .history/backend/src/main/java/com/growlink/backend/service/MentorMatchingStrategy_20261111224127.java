package com.growlink.backend.service;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.growlink.backend.entity.MentorshipMode;
import com.growlink.backend.entity.MentorshipOffer;
import com.growlink.backend.entity.RemotePreference;

@Component
public class MentorMatchingStrategy {
    public MatchingAnalysis analyze(RecommendationContext context, MentorshipOffer offer) {
        Map<MatchingDimension, Double> scores = new EnumMap<>(MatchingDimension.class);
        Map<MatchingDimension, String> evidence = new EnumMap<>(MatchingDimension.class);
        Set<String> desiredSkills = context.desiredSkills();
        Set<String> offerSkills = offer.getSkills().stream().map(skill -> skill.getName()).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<String> matchedSkills = MatchingText.matchedSkills(desiredSkills, offerSkills);
        add(scores, evidence, MatchingDimension.SKILL, MatchingText.skillScore(desiredSkills, offerSkills),
                matchedSkills.isEmpty() ? null : "Mentor expertise includes " + String.join(", ", matchedSkills) + ".");

        Set<String> categoryContext = new LinkedHashSet<>(context.categories());
        categoryContext.addAll(context.interests());
        Double categoryScore = categoryContext.isEmpty() ? null
                : MatchingText.textOverlap(offer.getCategory().name() + " " + offer.getCategory().getLabel(), categoryContext);
        add(scores, evidence, MatchingDimension.CATEGORY, categoryScore,
                categoryScore != null && categoryScore > 0 ? "The mentor's category aligns with your interests." : null);

        Double experienceScore = MatchingText.textOverlap(join(offer.getExperience(), offer.getDescription()),
                context.learningGoals());
        add(scores, evidence, MatchingDimension.EXPERIENCE, experienceScore,
                experienceScore != null && experienceScore > 0 ? "The mentor's experience relates to your learning goals." : null);

        String location = context.preferredLocations().isEmpty() ? context.location() : context.preferredLocations().iterator().next();
        boolean online = offer.getPreferredMode() == MentorshipMode.ONLINE || offer.getPreferredMode() == MentorshipMode.BOTH;
        Double locationScore = online && location != null ? 1d : MatchingText.locationScore(location, offer.getLocation());
        add(scores, evidence, MatchingDimension.LOCATION, locationScore,
                locationScore != null && locationScore > 0 ? "The offer's location or online mode is compatible." : null);

        if (context.remotePreference() != RemotePreference.ANY) {
            boolean compatible = context.remotePreference() == RemotePreference.REMOTE_ONLY
                    ? online : offer.getPreferredMode() != MentorshipMode.ONLINE;
            add(scores, evidence, MatchingDimension.REMOTE, compatible ? 1d : 0d,
                    compatible ? "Matches your preferred mentorship mode." : null);
        }

        String mentorText = join(offer.getTitle(), offer.getDescription(), offer.getExperience(),
                offer.getCategory().getLabel(), String.join(" ", offerSkills));
        Double interestScore = MatchingText.textOverlap(mentorText, context.interests());
        add(scores, evidence, MatchingDimension.INTEREST, interestScore,
                interestScore != null && interestScore > 0 ? "Relevant to your selected interests." : null);
        Double goalsScore = MatchingText.textOverlap(mentorText, context.learningGoals());
        add(scores, evidence, MatchingDimension.GOALS, goalsScore,
                goalsScore != null && goalsScore > 0 ? "Mentor topics align with your stated goals." : null);
        return new MatchingAnalysis(scores, matchedSkills, evidence);
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