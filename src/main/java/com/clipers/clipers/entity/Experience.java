package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "experience")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String company;

    @NotBlank
    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "experience_skills", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "skill")
    private List<String> skills;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ats_profile_id", nullable = false)
    private ATSProfile atsProfile;

    // Constructors
    public Experience() {}

    public Experience(String company, String position, LocalDate startDate, String description, ATSProfile atsProfile) {
        this.company = company;
        this.position = position;
        this.startDate = startDate;
        this.description = description;
        this.atsProfile = atsProfile;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public ATSProfile getAtsProfile() { return atsProfile; }
    public void setAtsProfile(ATSProfile atsProfile) { this.atsProfile = atsProfile; }
}
