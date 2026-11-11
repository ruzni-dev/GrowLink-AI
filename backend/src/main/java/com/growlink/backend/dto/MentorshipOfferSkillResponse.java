package com.growlink.backend.dto;

public record MentorshipOfferSkillResponse(
        Long id,
        String name,
        String category,
        String proficiencyLevel,
        Integer yearsOfExperience) {}