package com.growlink.backend.dto;

import com.growlink.backend.entity.OpportunityApplicationStatus;

import jakarta.validation.constraints.NotNull;

public record OpportunityApplicationStatusRequest(@NotNull OpportunityApplicationStatus status) {}