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

@Entity
@Table(name = "mentorship_requests", indexes = {
        @Index(name = "idx_mentorship_request_mentor_status", columnList = "mentor_id, status, created_at"),
        @Index(name = "idx_mentorship_request_mentee_status", columnList = "mentee_id, status, created_at"),
        @Index(name = "idx_mentorship_request_offer_mentee_status", columnList = "offer_id, mentee_id, status")
})
public class MentorshipRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private MentorshipOffer offer;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, length = 2000)
    private String goals;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MentorshipMode preferredMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MentorshipRequestStatus status = MentorshipRequestStatus.PENDING;

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
    public User getMentee() { return mentee; }
    public MentorshipOffer getOffer() { return offer; }
    public String getMessage() { return message; }
    public String getGoals() { return goals; }
    public MentorshipMode getPreferredMode() { return preferredMode; }
    public MentorshipRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setMentor(User mentor) { this.mentor = mentor; }
    public void setMentee(User mentee) { this.mentee = mentee; }
    public void setOffer(MentorshipOffer offer) { this.offer = offer; }
    public void setMessage(String message) { this.message = message; }
    public void setGoals(String goals) { this.goals = goals; }
    public void setPreferredMode(MentorshipMode preferredMode) { this.preferredMode = preferredMode; }
    public void setStatus(MentorshipRequestStatus status) { this.status = status; }
}