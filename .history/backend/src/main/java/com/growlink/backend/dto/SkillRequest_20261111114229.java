package com.growlink.backend.dto;

import com.growlink.backend.entity.ProficiencyLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String skillDescription,
        @Size(max = 100) String category,
        @NotNull ProficiencyLevel proficiencyLevel,
        @NotNull @Min(0) @Max(80) Integer yearsOfExperience,
        @Size(max = 1000) String description
) {}
