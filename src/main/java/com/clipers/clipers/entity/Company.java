package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "companies")
public class Company {

    @Id
    private String id;

    @NotBlank
    private String name;

    private String description;

    private String industry;

    private String size;

    private String website;

    private String logo;

    private String location;

    private Integer foundedYear; // Año de fundación

    private String mission; // Misión de la empresa

    private String vision; // Visión de la empresa

    private List<String> benefits; // Beneficios que ofrece

    private List<String> values; // Valores corporativos

    private String culture; // Cultura organizacional

    private Integer employeeCount; // Número de empleados

    private List<String> socialMedia; // Redes sociales (LinkedIn, Twitter, etc.)

    @JsonIgnore
    private String userId; // Referencia al usuario

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Relationships
    private List<String> jobIds;

    // Constructors
    public Company() {}

    public Company(String name, String description, String industry, String location, String userId) {
        this.name = name;
        this.description = description;
        this.industry = industry;
        this.location = location;
        this.userId = userId;
    }

    // Getters and Setters
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

    public List<String> getJobIds() { return jobIds; }
    public void setJobIds(List<String> jobIds) { this.jobIds = jobIds; }

    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }

    public String getMission() { return mission; }
    public void setMission(String mission) { this.mission = mission; }

    public String getVision() { return vision; }
    public void setVision(String vision) { this.vision = vision; }

    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }

    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }

    public String getCulture() { return culture; }
    public void setCulture(String culture) { this.culture = culture; }

    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }

    public List<String> getSocialMedia() { return socialMedia; }
    public void setSocialMedia(List<String> socialMedia) { this.socialMedia = socialMedia; }
}
