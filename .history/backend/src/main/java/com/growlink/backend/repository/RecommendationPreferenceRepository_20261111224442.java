package com.growlink.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.growlink.backend.entity.RecommendationPreference;

public interface RecommendationPreferenceRepository extends JpaRepository<RecommendationPreference, Long> {
    Optional<RecommendationPreference> findByUserEmailIgnoreCase(String email);
}