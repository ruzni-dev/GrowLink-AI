package com.growlink.backend.dto;

import com.growlink.backend.entity.Education;

public record EducationResponse(Long id, String institutionName, String qualification, String fieldOfStudy, Integer startYear, Integer endYear, boolean currentlyStudying, String description) {
    public static EducationResponse from(Education e) { return new EducationResponse(e.getId(), e.getInstitutionName(), e.getQualification(), e.getFieldOfStudy(), e.getStartYear(), e.getEndYear(), e.isCurrentlyStudying(), e.getDescription()); }
}
