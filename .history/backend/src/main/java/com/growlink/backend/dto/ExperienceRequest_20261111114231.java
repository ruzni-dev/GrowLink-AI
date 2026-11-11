package com.growlink.backend.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record ExperienceRequest(
        @NotBlank @Size(max = 160) String jobTitle,
        @NotBlank @Size(max = 180) String organization,
        @Size(max = 60) String employmentType,
        @PastOrPresent LocalDate startDate,
        @PastOrPresent LocalDate endDate,
        boolean currentlyWorking,
        @Size(max = 2000) String description
) {}
