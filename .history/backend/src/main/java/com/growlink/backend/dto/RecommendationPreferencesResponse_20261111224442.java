package com.growlink.backend.dto;

import java.time.Instant;
import java.util.Set;

import com.growlink.backend.entity.RemotePreference;

public record RecommendationPreferencesResponse(
        Set<String> preferredCategories,
        Set<String> preferredLocations,
        Set<String> interestedSkills,
        Set<String> learningInterests,
        Set<String> learningGoals,
        RemotePreference remotePreference,
        Instant updatedAt) {}