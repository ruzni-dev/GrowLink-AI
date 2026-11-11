package com.growlink.backend.dto;

import java.time.Instant;

import com.growlink.backend.entity.OpportunityApplicationStatus;

public record OpportunityApplicationResponse(
        Long id,
        OpportunityResponse opportunity,
        OpportunityApplicationStatus status,
        String notes,
        Instant appliedAt,
        Instant createdAt,
        Instant updatedAt) {}