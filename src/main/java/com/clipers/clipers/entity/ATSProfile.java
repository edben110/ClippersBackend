package com.clipers.clipers.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad ATSProfile que implementa Builder Pattern implícitamente
 * para la construcción de perfiles complejos paso a paso
 */
@Entity
@Table(name = "ats_profiles")
public class ATSProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String cliperId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "atsProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Education> education = new ArrayList<>();

    @OneToMany(mappedBy = "atsProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Experience> experience = new ArrayList<>();

    @OneToMany(mappedBy = "atsProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "atsProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Language> languages = new ArrayList<>();

    // Constructors
    public ATSProfile() {}

    public ATSProfile(User user) {
        this.user = user;
    }

    // Builder Pattern implementado implícitamente como métodos fluidos
    public ATSProfile withSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public ATSProfile withCliper(String cliperId) {
        this.cliperId = cliperId;
        return this;
    }

    public ATSProfile addEducation(String institution, String degree, String field) {
        Education education = new Education();
        education.setInstitution(institution);
        education.setDegree(degree);
        education.setField(field);
        education.setAtsProfile(this);
        this.education.add(education);
        return this;
    }

    public ATSProfile addExperience(String company, String position, String description) {
        Experience experience = new Experience();
        experience.setCompany(company);
        experience.setPosition(position);
        experience.setDescription(description);
        experience.setAtsProfile(this);
        this.experience.add(experience);
        return this;
    }

    public ATSProfile addSkill(String name, Skill.SkillLevel level, Skill.SkillCategory category) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setLevel(level);
        skill.setCategory(category);
        skill.setAtsProfile(this);
        this.skills.add(skill);
        return this;
    }

    public ATSProfile addLanguage(String name, Language.LanguageLevel level) {
        Language language = new Language();
        language.setName(name);
        language.setLevel(level);
        language.setAtsProfile(this);
        this.languages.add(language);
        return this;
    }

    // Template Method para validar completitud del perfil
    public boolean isComplete() {
        return hasBasicInfo() && hasRequiredSections();
    }

    private boolean hasBasicInfo() {
        return summary != null && !summary.trim().isEmpty();
    }

    private boolean hasRequiredSections() {
        return !education.isEmpty() || !experience.isEmpty() || !skills.isEmpty();
    }

    // Método para generar perfil automáticamente desde Cliper (Strategy implícito)
    public void generateFromCliperData(String transcription, List<String> detectedSkills) {
        if (transcription != null && !transcription.isEmpty()) {
            this.summary = generateSummaryFromTranscription(transcription);
        }
        
        if (detectedSkills != null && !detectedSkills.isEmpty()) {
            for (String skillName : detectedSkills) {
                addSkill(skillName, determineSkillLevel(skillName), categorizeSkill(skillName));
            }
        }
    }

    private String generateSummaryFromTranscription(String transcription) {
        // Estrategia simple de generación de resumen
        if (transcription.length() > 200) {
            return transcription.substring(0, 200) + "...";
        }
        return transcription;
    }

    private Skill.SkillLevel determineSkillLevel(String skillName) {
        // Estrategia simple para determinar nivel
        return Skill.SkillLevel.INTERMEDIATE; // Por defecto
    }

    private Skill.SkillCategory categorizeSkill(String skillName) {
        String lowerSkill = skillName.toLowerCase();
        if (lowerSkill.contains("java") || lowerSkill.contains("python") || 
            lowerSkill.contains("javascript") || lowerSkill.contains("sql")) {
            return Skill.SkillCategory.TECHNICAL;
        } else if (lowerSkill.contains("comunicación") || lowerSkill.contains("liderazgo") ||
                   lowerSkill.contains("trabajo en equipo")) {
            return Skill.SkillCategory.SOFT;
        }
        return Skill.SkillCategory.SOFT; // Por defecto
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCliperId() { return cliperId; }
    public void setCliperId(String cliperId) { this.cliperId = cliperId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Education> getEducation() { return education; }
    public void setEducation(List<Education> education) { this.education = education; }

    public List<Experience> getExperience() { return experience; }
    public void setExperience(List<Experience> experience) { this.experience = experience; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }

    public List<Language> getLanguages() { return languages; }
    public void setLanguages(List<Language> languages) { this.languages = languages; }
}