package com.growlink.backend.dto;

import java.util.List;

public record OpportunityRecommendationResponse(
        OpportunityResponse opportunity,
        int matchPercentage,
        List<String> matchedSkills,
        List<String> reasons) {}