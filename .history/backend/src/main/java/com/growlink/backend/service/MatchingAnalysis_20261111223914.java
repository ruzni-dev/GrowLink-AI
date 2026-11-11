package com.growlink.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

record MatchingAnalysis(Map<MatchingDimension, Double> scores, Set<String> matchedSkills,
        Map<MatchingDimension, String> evidence) {
    MatchingAnalysis {
        scores = Map.copyOf(scores);
        matchedSkills = Set.copyOf(matchedSkills);
        evidence = Map.copyOf(evidence);
    }

    List<String> explanations() {
        return evidence.values().stream().distinct().toList();
    }
}