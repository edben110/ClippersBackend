package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@Table(name = "education")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String institution;

    @NotBlank
    private String degree;

    @NotBlank
    private String field;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ats_profile_id", nullable = false)
    private ATSProfile atsProfile;

    // Constructors
    public Education() {}

    public Education(String institution, String degree, String field, LocalDate startDate, ATSProfile atsProfile) {
        this.institution = institution;
        this.degree = degree;
        this.field = field;
        this.startDate = startDate;
        this.atsProfile = atsProfile;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ATSProfile getAtsProfile() { return atsProfile; }
    public void setAtsProfile(ATSProfile atsProfile) { this.atsProfile = atsProfile; }
}
