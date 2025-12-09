package com.example.linkedin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request DTO for querying interaction history.
 */
@Schema(description = "Request to query interaction history with optional filters")
public class HistoryQueryRequest {
    
    @Schema(description = "Filter by post ID", example = "urn:li:share:1234567890")
    private String postId;
    
    @Schema(description = "Filter by start date (ISO-8601 format)", example = "2024-01-01T00:00:00Z")
    private String startDate;
    
    @Schema(description = "Filter by end date (ISO-8601 format)", example = "2024-12-31T23:59:59Z")
    private String endDate;

    // Getters and Setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
