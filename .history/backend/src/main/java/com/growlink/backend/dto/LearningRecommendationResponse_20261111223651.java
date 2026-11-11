package com.growlink.backend.dto;

import java.util.List;

public record LearningRecommendationResponse(
        String skillArea,
        int matchPercentage,
        int requiredByOpportunities,
        List<String> reasons) {}