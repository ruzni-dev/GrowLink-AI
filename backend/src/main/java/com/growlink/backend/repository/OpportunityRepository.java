package com.growlink.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.growlink.backend.entity.Opportunity;
import com.growlink.backend.entity.OpportunityStatus;
import java.util.Collection;
import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {
    Page<Opportunity> findByCreatedByEmailIgnoreCase(String email, Pageable pageable);
    Page<Opportunity> findByCreatedByEmailIgnoreCaseAndStatus(String email, OpportunityStatus status, Pageable pageable);

    @Query("select distinct opportunity from Opportunity opportunity left join fetch opportunity.requiredSkills where opportunity.id in :ids")
    List<Opportunity> findWithRequiredSkillsByIdIn(@Param("ids") Collection<Long> ids);
}