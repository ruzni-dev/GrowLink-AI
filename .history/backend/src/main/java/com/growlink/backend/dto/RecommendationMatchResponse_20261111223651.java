package com.growlink.backend.dto;

import java.util.List;

public record RecommendationMatchResponse(
        String targetType,
        Long targetId,
        int matchPercentage,
        List<String> matchedSkills,
        List<String> reasons) {}