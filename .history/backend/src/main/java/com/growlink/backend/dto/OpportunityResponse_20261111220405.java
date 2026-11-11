package com.growlink.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import com.growlink.backend.entity.OpportunityCategory;
import com.growlink.backend.entity.OpportunityStatus;

public record OpportunityResponse(
        Long id,
        String title,
        String description,
        String organizationName,
        OpportunityCategory category,
        String opportunityType,
        String location,
        boolean remote,
        String eligibility,
        Set<String> requiredSkills,
        String qualification,
        String applicationUrl,
        String contactInformation,
        LocalDate startDate,
        LocalDate deadline,
        OpportunityStatus status,
        Instant createdAt,
        Instant updatedAt) {}