package com.growlink.backend.dto;

public record PublicSkillResponse(
        String name,
        String category,
        String proficiencyLevel,
        Integer yearsOfExperience,
        String description) {}