package com.growlink.backend.dto;

import java.time.Instant;

import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserRole;
import com.growlink.backend.entity.UserStatus;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String location,
        UserRole role,
        UserRole requestedRole,
        UserStatus status,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getLocation(),
                user.getRole(),
                user.getRequestedRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
