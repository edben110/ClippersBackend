package com.clipers.clipers.entity;

import jakarta.validation.constraints.NotBlank;

// Embedded document - no necesita @Document
public class Education {

    @NotBlank
    private String institution;

    @NotBlank
    private String degree;

    @NotBlank
    private String field;

    private String startDate; // Format: YYYY-MM

    private String endDate; // Format: YYYY-MM

    private String description;

    // Constructors
    public Education() {}

    public Education(String institution, String degree, String field, String startDate) {
        this.institution = institution;
        this.degree = degree;
        this.field = field;
        this.startDate = startDate;
    }

    // Getters and Setters
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { 
        this.endDate = (endDate == null || endDate.trim().isEmpty()) ? null : endDate; 
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
