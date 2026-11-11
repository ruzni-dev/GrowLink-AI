package com.growlink.backend.dto;

import java.time.Instant;

public record SavedOpportunityResponse(Long id, Instant savedAt, OpportunityResponse opportunity) {}