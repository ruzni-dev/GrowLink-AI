package com.growlink.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.growlink.backend.entity.MentorshipRequest;
import com.growlink.backend.entity.MentorshipRequestStatus;

public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {
    boolean existsByOfferIdAndMenteeIdAndStatus(Long offerId, Long menteeId, MentorshipRequestStatus status);

    @Query("select request from MentorshipRequest request where lower(request.mentor.email) = lower(:email) and (:status is null or request.status = :status)")
    Page<MentorshipRequest> findReceived(@Param("email") String email,
            @Param("status") MentorshipRequestStatus status, Pageable pageable);

    @Query("select request from MentorshipRequest request where lower(request.mentee.email) = lower(:email) and (:status is null or request.status = :status)")
    Page<MentorshipRequest> findSent(@Param("email") String email,
            @Param("status") MentorshipRequestStatus status, Pageable pageable);
}