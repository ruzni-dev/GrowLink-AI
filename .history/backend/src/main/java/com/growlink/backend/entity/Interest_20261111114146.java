package com.growlink.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interests", uniqueConstraints = @UniqueConstraint(name = "uk_interests_name", columnNames = "name"))
public class Interest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    public Interest() {} public Interest(String name) { this.name = name; }
    public Long getId() { return id; } public String getName() { return name; }
    public void setName(String value) { name = value; }
}
