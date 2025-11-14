package com.clipers.clipers.entity;

import jakarta.validation.constraints.NotBlank;

// Embedded document - no necesita @Document
public class Skill {

    @NotBlank
    private String name;

    private SkillLevel level;

    private SkillCategory category;

    // Constructors
    public Skill() {}

    public Skill(String name, SkillLevel level, SkillCategory category) {
        this.name = name;
        this.level = level;
        this.category = category;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SkillLevel getLevel() { return level; }
    public void setLevel(SkillLevel level) { this.level = level; }

    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }

    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum SkillCategory {
        TECHNICAL, SOFT, LANGUAGE
    }
}
