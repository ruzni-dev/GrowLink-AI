package com.growlink.backend.dto;

import com.growlink.backend.entity.Interest;

public record InterestResponse(Long id, String name) {
    public static InterestResponse from(Interest interest) { return new InterestResponse(interest.getId(), interest.getName()); }
}
