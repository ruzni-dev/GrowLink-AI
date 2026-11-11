package com.growlink.backend.dto;

import java.time.LocalDate;
import java.util.Set;

import com.growlink.backend.entity.OpportunityCategory;
import com.growlink.backend.entity.OpportunityStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpportunityRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(min = 30, max = 6000) String description,
        @NotBlank @Size(max = 180) String organizationName,
        @NotNull OpportunityCategory category,
        @Size(max = 100) String opportunityType,
        @Size(max = 160) String location,
        @NotNull Boolean remote,
        @Size(max = 2500) String eligibility,
        @Size(max = 30) Set<@NotBlank @Size(max = 120) String> requiredSkills,
        @Size(max = 2000) String qualification,
        @Size(max = 2048) String applicationUrl,
        @Size(max = 1000) String contactInformation,
        LocalDate startDate,
        LocalDate deadline,
        OpportunityStatus status) {}