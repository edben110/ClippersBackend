package com.clipers.clipers.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;

@Document(collection = "job_matches")
public class JobMatch {

    @Id
    private String id;

    @org.springframework.data.mongodb.core.index.Indexed
    private String jobId;

    @org.springframework.data.mongodb.core.index.Indexed
    private String userId;

    @NotNull
    private Double score; // 0.0 to 1.0

    private String explanation;

    private List<String> matchedSkills;

    private ApplicationStatus status = ApplicationStatus.PENDING;

    @CreatedDate
    private LocalDateTime createdAt;

    private String applicationMessage;

    // Constructors
    public JobMatch() {}

    public JobMatch(String jobId, String userId, Double score, String explanation) {
        this.jobId = jobId;
        this.userId = userId;
        this.score = score;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getApplicationMessage() { return applicationMessage; }
    public void setApplicationMessage(String applicationMessage) { this.applicationMessage = applicationMessage; }

    public enum ApplicationStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
