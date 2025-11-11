package com.clipers.clipers.dto;

import com.clipers.clipers.entity.Company;

import java.time.LocalDateTime;

public class CompanyDTO {
    private String id;
    private String name;
    private String description;
    private String industry;
    private String size;
    private String website;
    private String logo;
    private String location;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CompanyDTO() {}

    public CompanyDTO(Company company) {
        this.id = company.getId();
        this.name = company.getName();
        this.description = company.getDescription();
        this.industry = company.getIndustry();
        this.size = company.getSize();
        this.website = company.getWebsite();
        this.logo = company.getLogo();
        this.location = company.getLocation();
        this.userId = company.getUser().getId();
        this.createdAt = company.getCreatedAt();
        this.updatedAt = company.getUpdatedAt();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}