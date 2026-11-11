package com.growlink.backend.dto;

import com.growlink.backend.entity.OpportunityApplicationStatus;

import jakarta.validation.constraints.Size;

public record OpportunityApplicationRequest(
        OpportunityApplicationStatus status,
        @Size(max = 4000) String notes) {}