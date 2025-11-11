package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    private SkillLevel level;

    @Enumerated(EnumType.STRING)
    private SkillCategory category;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ats_profile_id", nullable = false)
    private ATSProfile atsProfile;

    // Constructors
    public Skill() {}

    public Skill(String name, SkillLevel level, SkillCategory category, ATSProfile atsProfile) {
        this.name = name;
        this.level = level;
        this.category = category;
        this.atsProfile = atsProfile;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SkillLevel getLevel() { return level; }
    public void setLevel(SkillLevel level) { this.level = level; }

    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }

    public ATSProfile getAtsProfile() { return atsProfile; }
    public void setAtsProfile(ATSProfile atsProfile) { this.atsProfile = atsProfile; }

    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum SkillCategory {
        TECHNICAL, SOFT, LANGUAGE
    }
}
