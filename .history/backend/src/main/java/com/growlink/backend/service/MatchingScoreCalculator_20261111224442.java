package com.growlink.backend.service;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MatchingScoreCalculator {
    private final MatchingWeights weights;

    public MatchingScoreCalculator(MatchingWeights weights) {
        this.weights = weights;
    }

    public int calculate(Map<MatchingDimension, Double> dimensionScores) {
        double weightedScore = 0;
        double includedWeight = 0;
        for (Map.Entry<MatchingDimension, Double> entry : dimensionScores.entrySet()) {
            double weight = weights.weight(entry.getKey());
            if (weight <= 0) continue;
            double score = Math.max(0, Math.min(1, entry.getValue()));
            weightedScore += score * weight;
            includedWeight += weight;
        }
        return includedWeight == 0 ? 0 : (int) Math.round(weightedScore / includedWeight * 100);
    }
}