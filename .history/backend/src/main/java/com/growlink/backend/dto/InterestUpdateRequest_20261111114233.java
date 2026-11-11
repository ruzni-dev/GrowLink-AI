package com.growlink.backend.dto;

import java.util.Set;
import jakarta.validation.constraints.NotNull;

public record InterestUpdateRequest(@NotNull Set<Long> interestIds) {}
