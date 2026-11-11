package com.growlink.backend.dto;

import com.growlink.backend.entity.UserSkill;

public record SkillResponse(Long id, Long skillId, String name, String skillDescription, String category, String proficiencyLevel, Integer yearsOfExperience, String description) {
    public static SkillResponse from(UserSkill s) { return new SkillResponse(s.getId(), s.getSkill().getId(), s.getSkill().getName(), s.getSkill().getDescription(), s.getSkill().getCategory(), s.getProficiencyLevel().name(), s.getYearsOfExperience(), s.getDescription()); }
}
