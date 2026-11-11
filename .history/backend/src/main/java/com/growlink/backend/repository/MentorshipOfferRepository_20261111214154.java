package com.growlink.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.growlink.backend.entity.MentorshipOffer;
import com.growlink.backend.entity.MentorshipOfferStatus;

import jakarta.persistence.LockModeType;

public interface MentorshipOfferRepository extends JpaRepository<MentorshipOffer, Long>, JpaSpecificationExecutor<MentorshipOffer> {
    List<MentorshipOffer> findByMentorEmailIgnoreCaseOrderByUpdatedAtDesc(String email);

    Page<MentorshipOffer> findByMentorEmailIgnoreCaseAndStatus(String email, MentorshipOfferStatus status, Pageable pageable);

    Page<MentorshipOffer> findByMentorEmailIgnoreCase(String email, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select offer from MentorshipOffer offer where offer.id = :id")
    Optional<MentorshipOffer> findByIdForUpdate(@Param("id") Long id);
}