package com.example.linkedin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a LinkedIn post.
 */
public class Post {
    @NotBlank
    private String id;
    
    @NotBlank
    private String authorId;
    
    @NotBlank
    private String content;
    
    private List<String> mediaUrls;
    
    private Map<String, String> metadata;
    
    @NotNull
    private Instant createdAt;

    // Constructors
    public Post() {
        this.mediaUrls = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public Post(String id, String authorId, String content, Instant createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
        this.mediaUrls = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
