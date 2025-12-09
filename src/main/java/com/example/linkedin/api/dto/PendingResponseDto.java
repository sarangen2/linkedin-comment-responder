package com.example.linkedin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO for pending response information.
 */
@Schema(description = "Information about a pending response awaiting approval")
public class PendingResponseDto {
    
    @Schema(description = "Comment ID", example = "comment-123")
    private String commentId;
    
    @Schema(description = "Commenter name", example = "John Doe")
    private String commenterName;
    
    @Schema(description = "Comment text", example = "Great post! Very insightful.")
    private String commentText;
    
    @Schema(description = "Generated response text", example = "Thanks John! Glad you found it helpful.")
    private String generatedResponse;
    
    @Schema(description = "Confidence score of the generated response", example = "0.95")
    private double confidenceScore;
    
    @Schema(description = "Reasoning behind the generated response")
    private String reasoning;
    
    @Schema(description = "Any warnings about the generated response")
    private List<String> warnings;

    // Getters and Setters
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

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
