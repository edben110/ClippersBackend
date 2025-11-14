package com.clipers.clipers.dto;

import com.clipers.clipers.entity.Cliper;
import com.clipers.clipers.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public class CliperDTO {
    private String id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer duration;
    private String status;
    private String transcription;
    private List<String> skills;
    private String userId;
    private UserDTO user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CliperDTO() {}

    public CliperDTO(Cliper cliper) {
        this.id = cliper.getId();
        this.title = cliper.getTitle();
        this.description = cliper.getDescription();
        this.videoUrl = cliper.getVideoUrl();
        this.thumbnailUrl = cliper.getThumbnailUrl();
        this.duration = cliper.getDuration();
        this.status = cliper.getStatus().toString();
        this.transcription = cliper.getTranscription();
        this.skills = cliper.getSkills();
        this.createdAt = cliper.getCreatedAt();
        this.updatedAt = cliper.getUpdatedAt();
        this.userId = cliper.getUserId();
        // Note: user object should be populated by service layer if needed
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}