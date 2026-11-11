package com.growlink.backend.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record TalentPageResponse(
        List<PublicTalentResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static TalentPageResponse from(Page<PublicTalentResponse> page) {
        return new TalentPageResponse(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}