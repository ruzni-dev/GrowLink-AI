package com.growlink.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.growlink.backend.entity.Opportunity;
import com.growlink.backend.entity.OpportunityStatus;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {
    Page<Opportunity> findByCreatedByEmailIgnoreCase(String email, Pageable pageable);
    Page<Opportunity> findByCreatedByEmailIgnoreCaseAndStatus(String email, OpportunityStatus status, Pageable pageable);
}