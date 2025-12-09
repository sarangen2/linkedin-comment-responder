package com.example.linkedin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for starting polling workflow.
 */
@Schema(description = "Request to start polling for comments on a LinkedIn post")
public class StartPollingRequest {
    
    @NotBlank
    @Schema(description = "LinkedIn post ID to monitor", example = "urn:li:share:1234567890")
    private String postId;
    
    @Min(1)
    @Schema(description = "Polling interval in seconds", example = "300", minimum = "1")
    private int pollingIntervalSeconds = 300;
    
    @NotNull
    @Schema(description = "Whether manual approval is required before posting", example = "false")
    private Boolean requireManualApproval = false;
    
    @NotBlank
    @Schema(description = "Tone preference for generated responses", example = "witty", allowableValues = {"witty", "sarcastic", "wholesome", "professional"})
    private String tonePreference = "witty";
    
    @Schema(description = "Keywords that trigger manual review", example = "[\"urgent\", \"complaint\", \"refund\"]")
    private List<String> manualReviewKeywords = new ArrayList<>();
    
    @Min(1)
    @Schema(description = "Maximum number of retry attempts", example = "3", minimum = "1")
    private int maxRetries = 3;
    
    @Min(1)
    @Schema(description = "Initial retry backoff in seconds", example = "2", minimum = "1")
    private int retryBackoffSeconds = 2;

    // Getters and Setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public int getPollingIntervalSeconds() {
        return pollingIntervalSeconds;
    }

    public void setPollingIntervalSeconds(int pollingIntervalSeconds) {
        this.pollingIntervalSeconds = pollingIntervalSeconds;
    }

    public Boolean getRequireManualApproval() {
        return requireManualApproval;
    }

    public void setRequireManualApproval(Boolean requireManualApproval) {
        this.requireManualApproval = requireManualApproval;
    }

    public String getTonePreference() {
        return tonePreference;
    }

    public void setTonePreference(String tonePreference) {
        this.tonePreference = tonePreference;
    }

    public List<String> getManualReviewKeywords() {
        return manualReviewKeywords;
    }

    public void setManualReviewKeywords(List<String> manualReviewKeywords) {
        this.manualReviewKeywords = manualReviewKeywords;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryBackoffSeconds() {
        return retryBackoffSeconds;
    }

    public void setRetryBackoffSeconds(int retryBackoffSeconds) {
        this.retryBackoffSeconds = retryBackoffSeconds;
    }
}
