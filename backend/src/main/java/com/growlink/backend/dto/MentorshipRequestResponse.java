package com.growlink.backend.dto;

import java.time.Instant;

import com.growlink.backend.entity.MentorshipMode;
import com.growlink.backend.entity.MentorshipRequestStatus;

public record MentorshipRequestResponse(
        Long id,
        Long offerId,
        String offerTitle,
        String mentorName,
        String menteeName,
        String message,
        String goals,
        MentorshipMode preferredMode,
        MentorshipRequestStatus status,
        Instant createdAt,
        Instant updatedAt) {}