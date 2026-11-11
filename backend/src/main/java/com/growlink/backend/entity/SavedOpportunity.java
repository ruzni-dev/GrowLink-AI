package com.growlink.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "saved_opportunities",
        uniqueConstraints = @UniqueConstraint(name = "uk_saved_opportunity_user_opportunity", columnNames = {"user_id", "opportunity_id"}),
        indexes = @Index(name = "idx_saved_opportunity_user_created", columnList = "user_id, created_at"))
public class SavedOpportunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Opportunity getOpportunity() { return opportunity; }
    public Instant getCreatedAt() { return createdAt; }
    public void setUser(User user) { this.user = user; }
    public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }
}