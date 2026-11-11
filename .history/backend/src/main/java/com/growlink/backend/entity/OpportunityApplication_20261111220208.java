package com.growlink.backend.entity;

import java.time.Instant;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "opportunity_applications",
        uniqueConstraints = @UniqueConstraint(name = "uk_opportunity_application_user_opportunity", columnNames = {"user_id", "opportunity_id"}),
        indexes = @Index(name = "idx_opportunity_application_user_status", columnList = "user_id, status, updated_at"))
public class OpportunityApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OpportunityApplicationStatus status = OpportunityApplicationStatus.INTERESTED;

    @Column(length = 4000)
    private String notes;

    private Instant appliedAt;

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
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Opportunity getOpportunity() { return opportunity; }
    public OpportunityApplicationStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public Instant getAppliedAt() { return appliedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUser(User user) { this.user = user; }
    public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }
    public void setStatus(OpportunityApplicationStatus status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }
}