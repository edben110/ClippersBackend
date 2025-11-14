package com.clipers.clipers.entity;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

// Embedded document - no necesita @Document
public class Experience {

    @NotBlank
    private String company;

    @NotBlank
    private String position;

    private String startDate; // Format: YYYY-MM

    private String endDate; // Format: YYYY-MM

    private String description;

    private List<String> skills;

    // Constructors
    public Experience() {}

    public Experience(String company, String position, String startDate, String description) {
        this.company = company;
        this.position = position;
        this.startDate = startDate;
        this.description = description;
    }

    // Getters and Setters
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { 
        this.endDate = (endDate == null || endDate.trim().isEmpty()) ? null : endDate; 
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
