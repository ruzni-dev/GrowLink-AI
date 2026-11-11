package com.growlink.backend.dto;

import java.util.List;

public record PublicTalentResponse(
        Long id,
        String fullName,
        String profileImage,
        String bio,
        String location,
        String country,
        List<EducationResponse> education,
        List<PublicSkillResponse> skills,
        List<ExperienceResponse> workExperience,
        List<InterestResponse> interests) {}