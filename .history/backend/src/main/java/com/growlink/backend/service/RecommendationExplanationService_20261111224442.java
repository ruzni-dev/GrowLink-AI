package com.growlink.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class RecommendationExplanationService {
    public MatchingResult explain(MatchingAnalysis analysis, MatchingScoreCalculator calculator) {
        List<String> reasons = new ArrayList<>(analysis.explanations());
        if (reasons.isEmpty()) {
            reasons.add("Profile information is limited; this suggestion is based on the details currently available.");
        }
        Set<String> matchedSkills = analysis.matchedSkills();
        return new MatchingResult(calculator.calculate(analysis.scores()), matchedSkills, reasons);
    }

    public MatchingAnalysis buildLearningAnalysis(double demand, Double interest, Double goal,
            int demandCount, Set<String> interests, Set<String> goals) {
        java.util.EnumMap<MatchingDimension, Double> scores = new java.util.EnumMap<>(MatchingDimension.class);
        java.util.EnumMap<MatchingDimension, String> evidence = new java.util.EnumMap<>(MatchingDimension.class);
        scores.put(MatchingDimension.SKILL, demand);
        evidence.put(MatchingDimension.SKILL, "Required by " + demandCount + " current published "
                + (demandCount == 1 ? "opportunity." : "opportunities."));
        if (interest != null) {
            scores.put(MatchingDimension.INTEREST, interest);
            if (interest > 0) evidence.put(MatchingDimension.INTEREST, "Connects with an interest you selected.");
        }
        if (goal != null) {
            scores.put(MatchingDimension.GOALS, goal);
            if (goal > 0) evidence.put(MatchingDimension.GOALS, "Supports a learning goal you added.");
        }
        return new MatchingAnalysis(scores, Set.of(), evidence);
    }

    public List<String> skillGapReasons(MatchingAnalysis analysis, MatchingScoreCalculator calculator) {
        return explain(analysis, calculator).reasons();
    }
}