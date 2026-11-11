package com.growlink.backend.dto;

import com.growlink.backend.entity.OpportunityStatus;

import jakarta.validation.constraints.NotNull;

public record OpportunityStatusRequest(@NotNull OpportunityStatus status) {}