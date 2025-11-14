package com.clipers.clipers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "post_likes")
@CompoundIndex(name = "user_post_idx", def = "{'userId': 1, 'postId': 1}", unique = true)
public class PostLike {

    @Id
    private String id;

    @JsonIgnore
    private String userId;

    @JsonIgnore
    private String postId;

    @CreatedDate
    private LocalDateTime createdAt;

    // Constructors
    public PostLike() {}

    public PostLike(String userId, String postId) {
        this.userId = userId;
        this.postId = postId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
