package com.example.linkedin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * Request DTO for updating workflow configuration.
 */
@Schema(description = "Request to update workflow configuration")
public class ConfigUpdateRequest {
    
    @Min(1)
    @Schema(description = "Polling interval in seconds", example = "300", minimum = "1")
    private Integer pollingIntervalSeconds;
    
    @Schema(description = "Whether manual approval is required", example = "false")
    private Boolean requireManualApproval;
    
    @Schema(description = "Tone preference for responses", example = "witty")
    private String tonePreference;
    
    @Schema(description = "Keywords that trigger manual review", example = "[\"urgent\", \"complaint\"]")
    private List<String> manualReviewKeywords;
    
    @Min(1)
    @Schema(description = "Maximum retry attempts", example = "3", minimum = "1")
    private Integer maxRetries;
    
    @Min(1)
    @Schema(description = "Retry backoff in seconds", example = "2", minimum = "1")
    private Integer retryBackoffSeconds;

    // Getters and Setters
    public Integer getPollingIntervalSeconds() {
        return pollingIntervalSeconds;
    }

    public void setPollingIntervalSeconds(Integer pollingIntervalSeconds) {
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

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getRetryBackoffSeconds() {
        return retryBackoffSeconds;
    }

    public void setRetryBackoffSeconds(Integer retryBackoffSeconds) {
        this.retryBackoffSeconds = retryBackoffSeconds;
    }
}
