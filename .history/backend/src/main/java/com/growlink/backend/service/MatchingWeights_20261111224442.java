package com.growlink.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MatchingWeights {
    private final double skill;
    private final double category;
    private final double education;
    private final double experience;
    private final double location;
    private final double remote;
    private final double eligibility;
    private final double interest;
    private final double goals;

    public MatchingWeights(
            @Value("${app.recommendations.weights.skill:0.40}") double skill,
            @Value("${app.recommendations.weights.category:0.15}") double category,
            @Value("${app.recommendations.weights.education:0.15}") double education,
            @Value("${app.recommendations.weights.experience:0.10}") double experience,
            @Value("${app.recommendations.weights.location:0.08}") double location,
            @Value("${app.recommendations.weights.remote:0.04}") double remote,
            @Value("${app.recommendations.weights.eligibility:0.03}") double eligibility,
            @Value("${app.recommendations.weights.interest:0.03}") double interest,
            @Value("${app.recommendations.weights.goals:0.02}") double goals) {
        this.skill = skill;
        this.category = category;
        this.education = education;
        this.experience = experience;
        this.location = location;
        this.remote = remote;
        this.eligibility = eligibility;
        this.interest = interest;
        this.goals = goals;
    }

    public double weight(MatchingDimension dimension) {
        return switch (dimension) {
            case SKILL -> skill;
            case CATEGORY -> category;
            case EDUCATION -> education;
            case EXPERIENCE -> experience;
            case LOCATION -> location;
            case REMOTE -> remote;
            case ELIGIBILITY -> eligibility;
            case INTEREST -> interest;
            case GOALS -> goals;
        };
    }
}