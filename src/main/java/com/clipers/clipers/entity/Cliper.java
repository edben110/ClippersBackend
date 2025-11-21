package com.clipers.clipers.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cliper entity representing a video profile
 * Processing is handled by external microservice
 */
@Document(collection = "clipers")
public class Cliper {

    @Id
    private String id;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String videoUrl;

    private String thumbnailUrl;

    @NotNull
    private Integer duration; // in seconds

    private Status status = Status.UPLOADED;

    private String transcription;

    private List<String> skills = new ArrayList<>();

    private String userId; // Referencia al usuario

    private List<String> likedBy = new ArrayList<>(); // User IDs who liked
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Cliper() {}

    public Cliper(String title, String description, String videoUrl, Integer duration, String userId) {
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.userId = userId;
        this.likedBy = new ArrayList<>();
        this.comments = new ArrayList<>();
    }



    // State Pattern implicit for state management
    public boolean canBeEdited() {
        return this.status == Status.UPLOADED || this.status == Status.FAILED || this.status == Status.DONE;
    }

    public boolean isProcessingComplete() {
        return this.status == Status.DONE;
    }

    public boolean hasProcessingFailed() {
        return this.status == Status.FAILED;
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

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<String> getLikedBy() { 
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        return likedBy; 
    }
    
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public List<Comment> getComments() { 
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments; 
    }
    
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public int getLikesCount() { 
        return getLikedBy().size(); 
    }
    
    public int getCommentsCount() { 
        return getComments().size(); 
    }

    public boolean isLikedBy(String userId) {
        return getLikedBy().contains(userId);
    }

    public void addLike(String userId) {
        if (!getLikedBy().contains(userId)) {
            getLikedBy().add(userId);
        }
    }

    public void removeLike(String userId) {
        getLikedBy().remove(userId);
    }

    public void addComment(Comment comment) {
        getComments().add(comment);
    }

    public enum Status {
        UPLOADED, PROCESSING, DONE, FAILED
    }

    public static class Comment {
        private String id;
        private String userId;
        private String userName;
        private String text;
        private LocalDateTime createdAt;

        public Comment() {
            this.id = java.util.UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
        }

        public Comment(String userId, String userName, String text) {
            this();
            this.userId = userId;
            this.userName = userName;
            this.text = text;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}