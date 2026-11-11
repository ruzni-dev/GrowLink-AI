package com.growlink.backend.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @Size(max = 500) String profileImage,
        @Size(max = 2000) String bio,
        @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
        @Size(max = 30) String phone,
        @Size(max = 120) String location,
        @Size(max = 120) String country,
        @Size(max = 300) String website,
        @Size(max = 300) String linkedinUrl,
        @Size(max = 300) String githubUrl
) {}
