package com.growlink.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_interests", uniqueConstraints = @UniqueConstraint(name = "uk_user_interests", columnNames = {"user_id", "interest_id"}))
public class UserInterest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "interest_id", nullable = false) private Interest interest;
    public Long getId() { return id; } public User getUser() { return user; } public Interest getInterest() { return interest; }
    public void setUser(User value) { user = value; } public void setInterest(Interest value) { interest = value; }
}
