package com.clipers.clipers.entity;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    @NotBlank
    private String content;

    private String imageUrl;

    private String videoUrl;

    private PostType type = PostType.TEXT;

    @org.springframework.data.mongodb.core.index.Indexed
    private String userId; // Referencia al usuario

    @org.springframework.data.annotation.Transient
    private User user; // Usuario completo (no se guarda en DB, solo para respuestas)

    private Integer likes = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Relationships - Referencias por ID
    private List<String> commentIds;

    private List<String> postLikeIds;

    // Constructors
    public Post() {}

    public Post(String content, PostType type, String userId) {
        this.content = content;
        this.type = type;
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public PostType getType() { return type; }
    public void setType(PostType type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<String> getCommentIds() { return commentIds; }
    public void setCommentIds(List<String> commentIds) { this.commentIds = commentIds; }

    public List<String> getPostLikeIds() { return postLikeIds; }
    public void setPostLikeIds(List<String> postLikeIds) { this.postLikeIds = postLikeIds; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public enum PostType {
        TEXT, IMAGE, VIDEO, CLIPER
    }
}
