package com.clipers.clipers.dto;

import com.clipers.clipers.entity.JobMatch;

import java.time.LocalDateTime;
import java.util.List;

public class JobApplicationDTO {
    private String id;
    private String jobId;
    private JobDTO job;
    private String userId;
    private UserDTO user;
    private ATSProfileDTO atsProfile;
    private Double score;
    private String explanation;
    private List<String> matchedSkills;
    private String status;
    private String applicationMessage;
    private LocalDateTime createdAt;

    public JobApplicationDTO() {}

    public JobApplicationDTO(JobMatch jobMatch) {
        this.id = jobMatch.getId();
        this.jobId = jobMatch.getJobId();
        this.userId = jobMatch.getUserId();
        this.score = jobMatch.getScore();
        this.explanation = jobMatch.getExplanation();
        this.matchedSkills = jobMatch.getMatchedSkills();
        this.status = jobMatch.getStatus() != null ? jobMatch.getStatus().name() : "PENDING";
        this.applicationMessage = jobMatch.getApplicationMessage();
        this.createdAt = jobMatch.getCreatedAt();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public JobDTO getJob() { return job; }
    public void setJob(JobDTO job) { this.job = job; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public ATSProfileDTO getAtsProfile() { return atsProfile; }
    public void setAtsProfile(ATSProfileDTO atsProfile) { this.atsProfile = atsProfile; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApplicationMessage() { return applicationMessage; }
    public void setApplicationMessage(String applicationMessage) { this.applicationMessage = applicationMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
