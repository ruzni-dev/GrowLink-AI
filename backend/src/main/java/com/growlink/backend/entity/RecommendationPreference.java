package com.growlink.backend.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_preferences")
public class RecommendationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection
    @CollectionTable(name = "recommendation_preferred_categories", joinColumns = @JoinColumn(name = "preference_id"),
            indexes = @Index(name = "idx_recommendation_category_owner", columnList = "preference_id"))
    @Column(name = "category", nullable = false, length = 50)
    private Set<String> preferredCategories = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "recommendation_preferred_locations", joinColumns = @JoinColumn(name = "preference_id"),
            indexes = @Index(name = "idx_recommendation_location_owner", columnList = "preference_id"))
    @Column(name = "location", nullable = false, length = 160)
    private Set<String> preferredLocations = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "recommendation_interested_skills", joinColumns = @JoinColumn(name = "preference_id"),
            indexes = @Index(name = "idx_recommendation_skill_owner", columnList = "preference_id"))
    @Column(name = "skill", nullable = false, length = 120)
    private Set<String> interestedSkills = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "recommendation_learning_interests", joinColumns = @JoinColumn(name = "preference_id"),
            indexes = @Index(name = "idx_recommendation_interest_owner", columnList = "preference_id"))
    @Column(name = "interest", nullable = false, length = 120)
    private Set<String> learningInterests = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "recommendation_learning_goals", joinColumns = @JoinColumn(name = "preference_id"),
            indexes = @Index(name = "idx_recommendation_goal_owner", columnList = "preference_id"))
    @Column(name = "goal", nullable = false, length = 240)
    private Set<String> learningGoals = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RemotePreference remotePreference = RemotePreference.ANY;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Set<String> getPreferredCategories() { return preferredCategories; }
    public Set<String> getPreferredLocations() { return preferredLocations; }
    public Set<String> getInterestedSkills() { return interestedSkills; }
    public Set<String> getLearningInterests() { return learningInterests; }
    public Set<String> getLearningGoals() { return learningGoals; }
    public RemotePreference getRemotePreference() { return remotePreference; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setUser(User user) { this.user = user; }
    public void setPreferredCategories(Set<String> values) { preferredCategories = values; }
    public void setPreferredLocations(Set<String> values) { preferredLocations = values; }
    public void setInterestedSkills(Set<String> values) { interestedSkills = values; }
    public void setLearningInterests(Set<String> values) { learningInterests = values; }
    public void setLearningGoals(Set<String> values) { learningGoals = values; }
    public void setRemotePreference(RemotePreference value) { remotePreference = value; }
}