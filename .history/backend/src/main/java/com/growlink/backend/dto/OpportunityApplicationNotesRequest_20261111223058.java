package com.growlink.backend.dto;

import jakarta.validation.constraints.Size;

public record OpportunityApplicationNotesRequest(@Size(max = 4000) String notes) {}