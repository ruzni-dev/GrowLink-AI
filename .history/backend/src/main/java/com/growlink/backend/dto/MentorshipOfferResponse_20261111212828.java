package com.growlink.backend.dto;

import java.time.Instant;
import java.util.List;

import com.growlink.backend.entity.MentorshipMode;
import com.growlink.backend.entity.MentorshipOfferStatus;
import com.growlink.backend.entity.TalentCategory;

public record MentorshipOfferResponse(
        Long id,
        String mentorName,
        String mentorProfileImage,
        String mentorBio,
        String mentorLocation,
        String title,
        String description,
        TalentCategory category,
        String categoryLabel,
        List<MentorshipOfferSkillResponse> skills,
        String experience,
        String availability,
        MentorshipMode preferredMode,
        String location,
        MentorshipOfferStatus status,
        Instant createdAt,
        Instant updatedAt) {}