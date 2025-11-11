package com.clipers.clipers.dto;

import com.clipers.clipers.entity.Job;

import java.time.LocalDateTime;
import java.util.List;

public class JobDTO {
    private String id;
    private String title;
    private String description;
    private List<String> requirements;
    private List<String> skills;
    private String location;
    private String type;
    private Integer salaryMin;
    private Integer salaryMax;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CompanyDTO company;

    public JobDTO() {}

    public JobDTO(Job job) {
        this.id = job.getId();
        this.title = job.getTitle();
        this.description = job.getDescription();
        this.requirements = job.getRequirements();
        this.skills = job.getSkills();
        this.location = job.getLocation();
        this.type = job.getType().name();
        this.salaryMin = job.getSalaryMin();
        this.salaryMax = job.getSalaryMax();
        this.isActive = job.getIsActive();
        this.createdAt = job.getCreatedAt();
        this.updatedAt = job.getUpdatedAt();
        if (job.getCompany() != null) {
            this.company = new CompanyDTO(job.getCompany());
        }
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }

    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public CompanyDTO getCompany() { return company; }
    public void setCompany(CompanyDTO company) { this.company = company; }
}