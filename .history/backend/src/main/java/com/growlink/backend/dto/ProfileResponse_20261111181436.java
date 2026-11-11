package com.growlink.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import com.growlink.backend.entity.UserProfile;

public record ProfileResponse(Long id, String fullName, String email, String profileImage, String bio, LocalDate dateOfBirth, String phone, String location, String country, String website, String linkedinUrl, String githubUrl, Instant createdAt, Instant updatedAt) {
    public static ProfileResponse from(UserProfile p) {
        return new ProfileResponse(p.getId(), p.getUser().getFullName(), p.getUser().getEmail(), p.getProfileImage(), p.getBio(), p.getDateOfBirth(), p.getPhone(), p.getLocation(), p.getCountry(), p.getWebsite(), p.getLinkedinUrl(), p.getGithubUrl(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
