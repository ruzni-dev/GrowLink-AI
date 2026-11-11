package com.growlink.backend.entity;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "work_experience")
public class WorkExperience {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, length = 160) private String jobTitle;
    @Column(nullable = false, length = 180) private String organization;
    @Column(length = 60) private String employmentType;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false) private boolean currentlyWorking;
    @Column(length = 2000) private String description;
    public Long getId() { return id; } public User getUser() { return user; } public String getJobTitle() { return jobTitle; } public String getOrganization() { return organization; } public String getEmploymentType() { return employmentType; } public LocalDate getStartDate() { return startDate; } public LocalDate getEndDate() { return endDate; } public boolean isCurrentlyWorking() { return currentlyWorking; } public String getDescription() { return description; }
    public void setUser(User value) { user = value; } public void setJobTitle(String value) { jobTitle = value; } public void setOrganization(String value) { organization = value; } public void setEmploymentType(String value) { employmentType = value; } public void setStartDate(LocalDate value) { startDate = value; } public void setEndDate(LocalDate value) { endDate = value; } public void setCurrentlyWorking(boolean value) { currentlyWorking = value; } public void setDescription(String value) { description = value; }
}
