package com.growlink.backend.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum TalentCategory {
    EDUCATION_LEARNING("Education & Learning", "education", "learning"),
    TECHNOLOGY_DIGITAL_SKILLS("Technology & Digital Skills", "technology", "digital skills"),
    CAREER_PROFESSIONAL_DEVELOPMENT("Career & Professional Development", "career development", "professional development"),
    MENTORSHIP_GUIDANCE("Mentorship & Guidance", "mentorship", "guidance"),
    SPORTS_COACHING("Sports & Coaching", "sports", "coaching"),
    VOLUNTEERING_SOCIAL_SERVICES("Volunteering & Social Services", "volunteering", "volunteer", "social services", "community development"),
    ENTREPRENEURSHIP("Entrepreneurship"),
    ARTS_CULTURE("Arts & Culture", "arts and culture", "arts", "culture");

    private final String label;
    private final List<String> legacyLabels;

    TalentCategory(String label, String... legacyLabels) {
        this.label = label;
        this.legacyLabels = Arrays.asList(legacyLabels);
    }

    public String getLabel() {
        return label;
    }

    public List<String> getStoredLabels() {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(name(), label), legacyLabels.stream()).toList();
    }

    public static TalentCategory fromValue(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(category -> category.getStoredLabels().stream()
                        .anyMatch(label -> label.toLowerCase(Locale.ROOT).equals(normalized)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown talent category"));
    }
}