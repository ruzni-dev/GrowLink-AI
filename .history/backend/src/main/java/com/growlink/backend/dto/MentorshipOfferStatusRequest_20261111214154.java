package com.growlink.backend.dto;

import com.growlink.backend.entity.MentorshipOfferStatus;

import jakarta.validation.constraints.NotNull;

public record MentorshipOfferStatusRequest(@NotNull MentorshipOfferStatus status) {}