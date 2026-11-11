package com.growlink.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "education")
public class Education {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, length = 200) private String institutionName;
    @Column(nullable = false, length = 160) private String qualification;
    @Column(length = 160) private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    @Column(nullable = false) private boolean currentlyStudying;
    @Column(length = 2000) private String description;
    public Long getId() { return id; } public User getUser() { return user; } public String getInstitutionName() { return institutionName; } public String getQualification() { return qualification; } public String getFieldOfStudy() { return fieldOfStudy; } public Integer getStartYear() { return startYear; } public Integer getEndYear() { return endYear; } public boolean isCurrentlyStudying() { return currentlyStudying; } public String getDescription() { return description; }
    public void setUser(User value) { user = value; } public void setInstitutionName(String value) { institutionName = value; } public void setQualification(String value) { qualification = value; } public void setFieldOfStudy(String value) { fieldOfStudy = value; } public void setStartYear(Integer value) { startYear = value; } public void setEndYear(Integer value) { endYear = value; } public void setCurrentlyStudying(boolean value) { currentlyStudying = value; } public void setDescription(String value) { description = value; }
}
