package com.growlink.backend.dto;

import java.time.LocalDate;
import com.growlink.backend.entity.WorkExperience;

public record ExperienceResponse(Long id, String jobTitle, String organization, String employmentType, LocalDate startDate, LocalDate endDate, boolean currentlyWorking, String description) {
    public static ExperienceResponse from(WorkExperience e) { return new ExperienceResponse(e.getId(), e.getJobTitle(), e.getOrganization(), e.getEmploymentType(), e.getStartDate(), e.getEndDate(), e.isCurrentlyWorking(), e.getDescription()); }
}
