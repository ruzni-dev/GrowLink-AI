package com.growlink.backend.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "mentorship_offers", indexes = {
        @Index(name = "idx_mentorship_offer_status_created", columnList = "status, created_at"),
        @Index(name = "idx_mentorship_offer_mentor_status", columnList = "mentor_id, status")
})
public class MentorshipOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TalentCategory category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mentorship_offer_skills",
            joinColumns = @JoinColumn(name = "offer_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "skill_id", nullable = false),
            indexes = {
                    @Index(name = "idx_mentorship_offer_skill_offer", columnList = "offer_id"),
                    @Index(name = "idx_mentorship_offer_skill_skill", columnList = "skill_id")
            })
    private Set<Skill> skills = new LinkedHashSet<>();

    @Column(length = 1500)
    private String experience;

    @Column(nullable = false, length = 500)
    private String availability;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MentorshipMode preferredMode;

    @Column(length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MentorshipOfferStatus status = MentorshipOfferStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getMentor() { return mentor; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TalentCategory getCategory() { return category; }
    public Set<Skill> getSkills() { return skills; }
    public String getExperience() { return experience; }
    public String getAvailability() { return availability; }
    public MentorshipMode getPreferredMode() { return preferredMode; }
    public String getLocation() { return location; }
    public MentorshipOfferStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setMentor(User mentor) { this.mentor = mentor; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(TalentCategory category) { this.category = category; }
    public void setSkills(Set<Skill> skills) { this.skills = skills; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setPreferredMode(MentorshipMode preferredMode) { this.preferredMode = preferredMode; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(MentorshipOfferStatus status) { this.status = status; }
}