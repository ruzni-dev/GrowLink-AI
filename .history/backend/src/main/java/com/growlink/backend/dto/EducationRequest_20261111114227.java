package com.growlink.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EducationRequest(
        @NotBlank @Size(max = 200) String institutionName,
        @NotBlank @Size(max = 160) String qualification,
        @Size(max = 160) String fieldOfStudy,
        @Min(1900) @Max(2200) Integer startYear,
        @Min(1900) @Max(2200) Integer endYear,
        boolean currentlyStudying,
        @Size(max = 2000) String description
) {}
