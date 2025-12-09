package com.example.linkedin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an interaction history entry for a comment and response.
 */
public class Interaction {
    @NotBlank
    private String id;
    
    @NotBlank
    private String postId;
    
    @NotBlank
    private String commentId;
    
    @NotBlank
    private String commenterName;
    
    @NotBlank
    private String commentText;
    
    private String generatedResponse;
    
    private String postedResponse;
    
    @NotNull
    private Instant timestamp;
    
    @NotNull
    private ResponseStatus status;
    
    private Map<String, String> metadata;

    // Constructors
    public Interaction() {
        this.metadata = new HashMap<>();
    }

    public Interaction(String id, String postId, String commentId, String commenterName, 
                      String commentText, Instant timestamp, ResponseStatus status) {
        this.id = id;
        this.postId = postId;
        this.commentId = commentId;
        this.commenterName = commenterName;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.status = status;
        this.metadata = new HashMap<>();
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

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getGeneratedResponse() {
        return generatedResponse;
    }

    public void setGeneratedResponse(String generatedResponse) {
        this.generatedResponse = generatedResponse;
    }

    public String getPostedResponse() {
        return postedResponse;
    }

    public void setPostedResponse(String postedResponse) {
        this.postedResponse = postedResponse;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
