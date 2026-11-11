package com.growlink.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_skills", uniqueConstraints = @UniqueConstraint(name = "uk_user_skills_user_skill", columnNames = {"user_id", "skill_id"}))
public class UserSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "skill_id", nullable = false) private Skill skill;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ProficiencyLevel proficiencyLevel;
    @Column(nullable = false) private Integer yearsOfExperience;
    @Column(length = 1000) private String description;
    public Long getId() { return id; } public User getUser() { return user; } public Skill getSkill() { return skill; } public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; } public Integer getYearsOfExperience() { return yearsOfExperience; } public String getDescription() { return description; }
    public void setUser(User value) { user = value; } public void setSkill(Skill value) { skill = value; } public void setProficiencyLevel(ProficiencyLevel value) { proficiencyLevel = value; } public void setYearsOfExperience(Integer value) { yearsOfExperience = value; } public void setDescription(String value) { description = value; }
}
