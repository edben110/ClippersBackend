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
    private Integer foundedYear;
    private String mission;
    private String vision;
    private java.util.List<String> benefits;
    private java.util.List<String> values;
    private String culture;
    private Integer employeeCount;
    private java.util.List<String> socialMedia;
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
        this.foundedYear = company.getFoundedYear();
        this.mission = company.getMission();
        this.vision = company.getVision();
        this.benefits = company.getBenefits();
        this.values = company.getValues();
        this.culture = company.getCulture();
        this.employeeCount = company.getEmployeeCount();
        this.socialMedia = company.getSocialMedia();
        this.userId = company.getUserId();
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

    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }

    public String getMission() { return mission; }
    public void setMission(String mission) { this.mission = mission; }

    public String getVision() { return vision; }
    public void setVision(String vision) { this.vision = vision; }

    public java.util.List<String> getBenefits() { return benefits; }
    public void setBenefits(java.util.List<String> benefits) { this.benefits = benefits; }

    public java.util.List<String> getValues() { return values; }
    public void setValues(java.util.List<String> values) { this.values = values; }

    public String getCulture() { return culture; }
    public void setCulture(String culture) { this.culture = culture; }

    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }

    public java.util.List<String> getSocialMedia() { return socialMedia; }
    public void setSocialMedia(java.util.List<String> socialMedia) { this.socialMedia = socialMedia; }
}