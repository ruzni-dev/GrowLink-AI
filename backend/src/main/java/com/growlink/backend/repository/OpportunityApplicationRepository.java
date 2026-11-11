package com.growlink.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.growlink.backend.entity.OpportunityApplication;

public interface OpportunityApplicationRepository extends JpaRepository<OpportunityApplication, Long> {
    Optional<OpportunityApplication> findByUserEmailIgnoreCaseAndOpportunityId(String email, Long opportunityId);
    boolean existsByUserEmailIgnoreCaseAndOpportunityId(String email, Long opportunityId);
    Page<OpportunityApplication> findByUserEmailIgnoreCase(String email, Pageable pageable);
    Optional<OpportunityApplication> findByIdAndUserEmailIgnoreCase(Long id, String email);
}