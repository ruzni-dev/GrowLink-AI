package com.growlink.backend.dto;

import java.util.List;

import com.growlink.backend.entity.MentorshipMode;
import com.growlink.backend.entity.TalentCategory;

public record MentorRecommendationResponse(
        Long offerId,
        String mentorName,
        String profileImage,
        String location,
        String title,
        String description,
        TalentCategory category,
        List<String> skills,
        String experience,
        String availability,
        MentorshipMode preferredMode,
        int matchPercentage,
        List<String> matchedSkills,
        List<String> reasons) {}