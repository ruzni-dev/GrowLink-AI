package com.growlink.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.growlink.backend.entity.SavedOpportunity;

public interface SavedOpportunityRepository extends JpaRepository<SavedOpportunity, Long> {
    Optional<SavedOpportunity> findByUserEmailIgnoreCaseAndOpportunityId(String email, Long opportunityId);
    boolean existsByUserEmailIgnoreCaseAndOpportunityId(String email, Long opportunityId);
    void deleteByUserEmailIgnoreCaseAndOpportunityId(String email, Long opportunityId);
    Page<SavedOpportunity> findByUserEmailIgnoreCase(String email, Pageable pageable);
}