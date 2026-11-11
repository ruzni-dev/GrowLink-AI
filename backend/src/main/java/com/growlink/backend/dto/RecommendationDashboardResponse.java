package com.growlink.backend.dto;

import java.util.List;

public record RecommendationDashboardResponse(
        String personalizedFor,
        List<OpportunityRecommendationResponse> opportunities,
        List<MentorRecommendationResponse> mentors,
        List<TalentRecommendationResponse> talents,
        List<LearningRecommendationResponse> learningAreas) {}