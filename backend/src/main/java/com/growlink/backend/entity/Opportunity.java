package com.growlink.backend.entity;

import java.time.Instant;
import java.time.LocalDate;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "opportunities", indexes = {
        @Index(name = "idx_opportunity_status_deadline", columnList = "status, deadline"),
        @Index(name = "idx_opportunity_category_status", columnList = "category, status"),
        @Index(name = "idx_opportunity_creator_status", columnList = "created_by, status"),
        @Index(name = "idx_opportunity_location", columnList = "location")
})
public class Opportunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 6000)
    private String description;

    @Column(nullable = false, length = 180)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OpportunityCategory category;

    @Column(length = 100)
    private String opportunityType;

    @Column(length = 160)
    private String location;

    @Column(nullable = false)
    private boolean remote;

    @Column(length = 2500)
    private String eligibility;

    @ElementCollection
    @CollectionTable(name = "opportunity_required_skills",
            joinColumns = @JoinColumn(name = "opportunity_id", nullable = false),
            indexes = @Index(name = "idx_opportunity_required_skill_owner", columnList = "opportunity_id"))
    @Column(name = "skill", nullable = false, length = 120)
    private Set<String> requiredSkills = new LinkedHashSet<>();

    @Column(length = 2000)
    private String qualification;

    @Column(length = 2048)
    private String applicationUrl;

    @Column(length = 1000)
    private String contactInformation;

    private LocalDate startDate;
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OpportunityStatus status = OpportunityStatus.DRAFT;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

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
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOrganizationName() { return organizationName; }
    public OpportunityCategory getCategory() { return category; }
    public String getOpportunityType() { return opportunityType; }
    public String getLocation() { return location; }
    public boolean isRemote() { return remote; }
    public String getEligibility() { return eligibility; }
    public Set<String> getRequiredSkills() { return requiredSkills; }
    public String getQualification() { return qualification; }
    public String getApplicationUrl() { return applicationUrl; }
    public String getContactInformation() { return contactInformation; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDeadline() { return deadline; }
    public OpportunityStatus getStatus() { return status; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public void setCategory(OpportunityCategory category) { this.category = category; }
    public void setOpportunityType(String opportunityType) { this.opportunityType = opportunityType; }
    public void setLocation(String location) { this.location = location; }
    public void setRemote(boolean remote) { this.remote = remote; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }
    public void setRequiredSkills(Set<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }
    public void setContactInformation(String contactInformation) { this.contactInformation = contactInformation; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setStatus(OpportunityStatus status) { this.status = status; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}