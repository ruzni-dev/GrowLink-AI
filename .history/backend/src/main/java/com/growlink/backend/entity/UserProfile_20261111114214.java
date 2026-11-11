package com.growlink.backend.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(length = 500) private String profileImage;
    @Column(length = 2000) private String bio;
    private LocalDate dateOfBirth;
    @Column(length = 30) private String phone;
    @Column(length = 120) private String location;
    @Column(length = 120) private String country;
    @Column(length = 300) private String website;
    @Column(length = 300) private String linkedinUrl;
    @Column(length = 300) private String githubUrl;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public User getUser() { return user; } public String getProfileImage() { return profileImage; }
    public String getBio() { return bio; } public LocalDate getDateOfBirth() { return dateOfBirth; } public String getPhone() { return phone; }
    public String getLocation() { return location; } public String getCountry() { return country; } public String getWebsite() { return website; }
    public String getLinkedinUrl() { return linkedinUrl; } public String getGithubUrl() { return githubUrl; } public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
    public void setUser(User user) { this.user = user; } public void setProfileImage(String value) { profileImage = value; } public void setBio(String value) { bio = value; }
    public void setDateOfBirth(LocalDate value) { dateOfBirth = value; } public void setPhone(String value) { phone = value; } public void setLocation(String value) { location = value; }
    public void setCountry(String value) { country = value; } public void setWebsite(String value) { website = value; } public void setLinkedinUrl(String value) { linkedinUrl = value; } public void setGithubUrl(String value) { githubUrl = value; }
}
