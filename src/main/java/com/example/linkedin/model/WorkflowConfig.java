package com.example.linkedin.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the LinkedIn Comment Responder workflow.
 */
public class WorkflowConfig {
    @NotBlank
    private String postId;
    
    @Min(1)
    private int pollingIntervalSeconds;
    
    @NotNull
    private boolean requireManualApproval;
    
    @NotBlank
    private String tonePreference;
    
    private List<String> manualReviewKeywords;
    
    @Min(1)
    private int maxRetries;
    
    @Min(1)
    private int retryBackoffSeconds;

    // Constructors
    public WorkflowConfig() {
        this.manualReviewKeywords = new ArrayList<>();
        this.pollingIntervalSeconds = 300; // Default 5 minutes
        this.maxRetries = 3;
        this.retryBackoffSeconds = 2;
        this.requireManualApproval = false;
        this.tonePreference = "witty";
    }

    public WorkflowConfig(String postId, int pollingIntervalSeconds, boolean requireManualApproval, 
                         String tonePreference, int maxRetries, int retryBackoffSeconds) {
        this.postId = postId;
        this.pollingIntervalSeconds = pollingIntervalSeconds;
        this.requireManualApproval = requireManualApproval;
        this.tonePreference = tonePreference;
        this.maxRetries = maxRetries;
        this.retryBackoffSeconds = retryBackoffSeconds;
        this.manualReviewKeywords = new ArrayList<>();
    }

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

    public boolean isRequireManualApproval() {
        return requireManualApproval;
    }

    public void setRequireManualApproval(boolean requireManualApproval) {
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
