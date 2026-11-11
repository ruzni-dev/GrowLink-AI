package com.growlink.backend.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.growlink.backend.entity.RemotePreference;
import com.growlink.backend.entity.User;

record RecommendationContext(
        User user,
        String location,
        Map<String, SkillFact> skills,
        List<String> educationTerms,
        List<String> experienceTerms,
        Set<String> categories,
        Set<String> preferredLocations,
        Set<String> interests,
        Set<String> learningGoals,
        RemotePreference remotePreference) {

    Set<String> currentSkills() {
        return skills.values().stream().map(SkillFact::name).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    Set<String> desiredSkills() {
        Set<String> desired = new LinkedHashSet<>(skills.values().stream()
                .filter(skill -> "BEGINNER".equals(skill.proficiencyLevel()) || "INTERMEDIATE".equals(skill.proficiencyLevel()))
                .map(SkillFact::name).toList());
        return desired;
    }

    record SkillFact(String name, String category, String proficiencyLevel, Integer yearsOfExperience) {}
}