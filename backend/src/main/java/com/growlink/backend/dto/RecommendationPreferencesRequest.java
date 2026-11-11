package com.growlink.backend.dto;

import java.util.Set;

import com.growlink.backend.entity.RemotePreference;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecommendationPreferencesRequest(
        @Size(max = 18) Set<@NotBlank @Size(max = 50) String> preferredCategories,
        @Size(max = 20) Set<@Size(max = 160) String> preferredLocations,
        @Size(max = 30) Set<@Size(max = 120) String> interestedSkills,
        @Size(max = 20) Set<@Size(max = 120) String> learningInterests,
        @Size(max = 20) Set<@Size(max = 240) String> learningGoals,
        @NotNull RemotePreference remotePreference) {}