package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "languages")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    private LanguageLevel level;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ats_profile_id", nullable = false)
    private ATSProfile atsProfile;

    // Constructors
    public Language() {}

    public Language(String name, LanguageLevel level, ATSProfile atsProfile) {
        this.name = name;
        this.level = level;
        this.atsProfile = atsProfile;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LanguageLevel getLevel() { return level; }
    public void setLevel(LanguageLevel level) { this.level = level; }

    public ATSProfile getAtsProfile() { return atsProfile; }
    public void setAtsProfile(ATSProfile atsProfile) { this.atsProfile = atsProfile; }

    public enum LanguageLevel {
        BASIC, INTERMEDIATE, ADVANCED, NATIVE
    }
}
