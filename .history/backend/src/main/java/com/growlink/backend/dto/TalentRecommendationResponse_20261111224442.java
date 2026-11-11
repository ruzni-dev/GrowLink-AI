package com.growlink.backend.dto;

import java.util.List;

public record TalentRecommendationResponse(
        PublicTalentResponse talent,
        int matchPercentage,
        List<String> matchedSkills,
        List<String> reasons) {}