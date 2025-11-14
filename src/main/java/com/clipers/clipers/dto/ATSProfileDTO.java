package com.clipers.clipers.dto;

import com.clipers.clipers.entity.ATSProfile;
import com.clipers.clipers.entity.Education;
import com.clipers.clipers.entity.Experience;
import com.clipers.clipers.entity.Language;
import com.clipers.clipers.entity.Skill;

import java.time.LocalDateTime;
import java.util.List;

public class ATSProfileDTO {
    private String id;
    private String summary;
    private List<Education> education;
    private List<Experience> experience;
    private List<Skill> skills;
    private List<Language> languages;
    private String userId;
    private String cliperId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ATSProfileDTO() {}

    public ATSProfileDTO(ATSProfile atsProfile) {
        this.id = atsProfile.getId();
        this.summary = atsProfile.getSummary();
        this.education = atsProfile.getEducation();
        this.experience = atsProfile.getExperience();
        this.skills = atsProfile.getSkills();
        this.languages = atsProfile.getLanguages();
        this.userId = atsProfile.getUserId();
        this.cliperId = atsProfile.getCliperId();
        this.createdAt = atsProfile.getCreatedAt();
        this.updatedAt = atsProfile.getUpdatedAt();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<Education> getEducation() { return education; }
    public void setEducation(List<Education> education) { this.education = education; }

    public List<Experience> getExperience() { return experience; }
    public void setExperience(List<Experience> experience) { this.experience = experience; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }

    public List<Language> getLanguages() { return languages; }
    public void setLanguages(List<Language> languages) { this.languages = languages; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCliperId() { return cliperId; }
    public void setCliperId(String cliperId) { this.cliperId = cliperId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
