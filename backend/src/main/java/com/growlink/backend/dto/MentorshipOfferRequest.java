package com.growlink.backend.dto;

import java.util.Set;

import com.growlink.backend.entity.MentorshipMode;
import com.growlink.backend.entity.TalentCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MentorshipOfferRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 3000) String description,
        @NotNull TalentCategory category,
        @NotEmpty @Size(max = 20) Set<@NotNull @Positive Long> skillIds,
        @Size(max = 1500) String experience,
        @NotBlank @Size(max = 500) String availability,
        @NotNull MentorshipMode preferredMode,
        @Size(max = 120) String location) {}