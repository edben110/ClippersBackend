package com.clipers.clipers.entity;

import jakarta.validation.constraints.NotBlank;

// Embedded document - no necesita @Document
public class Language {

    @NotBlank
    private String name;

    private LanguageLevel level;

    // Constructors
    public Language() {}

    public Language(String name, LanguageLevel level) {
        this.name = name;
        this.level = level;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LanguageLevel getLevel() { return level; }
    public void setLevel(LanguageLevel level) { this.level = level; }

    public enum LanguageLevel {
        BASIC, INTERMEDIATE, ADVANCED, NATIVE
    }
}
