package com.growlink.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skills", uniqueConstraints = @UniqueConstraint(name = "uk_skills_name", columnNames = "name"))
public class Skill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(length = 500) private String description;
    @Column(length = 100) private String category;
    public Long getId() { return id; } public String getName() { return name; } public String getDescription() { return description; } public String getCategory() { return category; }
    public void setName(String value) { name = value; } public void setDescription(String value) { description = value; } public void setCategory(String value) { category = value; }
}
