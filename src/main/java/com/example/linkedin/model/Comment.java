package com.example.linkedin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Represents a comment on a LinkedIn post.
 */
public class Comment {
    @NotBlank
    private String id;
    
    @NotBlank
    private String postId;
    
    @NotBlank
    private String authorId;
    
    @NotBlank
    private String authorName;
    
    @NotBlank
    private String text;
    
    @NotNull
    private Instant timestamp;
    
    private boolean isProcessed;

    // Constructors
    public Comment() {
    }

    public Comment(String id, String postId, String authorId, String authorName, String text, Instant timestamp) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = timestamp;
        this.isProcessed = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }
}
