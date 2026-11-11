package com.growlink.backend.dto;

import com.growlink.backend.entity.MentorshipMode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MentorshipRequestCreateRequest(
        @NotNull @Positive Long offerId,
        @NotBlank @Size(max = 2000) String message,
        @NotBlank @Size(max = 2000) String goals,
        @NotNull MentorshipMode preferredMode) {}