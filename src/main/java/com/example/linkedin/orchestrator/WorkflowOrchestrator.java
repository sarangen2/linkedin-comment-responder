package com.example.linkedin.orchestrator;

import com.example.linkedin.agent.LLMAgent;
import com.example.linkedin.client.LinkedInApiClient;
import com.example.linkedin.error.ErrorHandler;
import com.example.linkedin.model.*;
import com.example.linkedin.repository.StorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Orchestrates the workflow for LinkedIn comment response automation.
 * Coordinates polling, comment processing, response generation, and posting.
 */
@Component
public class WorkflowOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowOrchestrator.class);
    
    private final LinkedInApiClient apiClient;
    private final LLMAgent llmAgent;
    private final StorageRepository storageRepository;
    private final ErrorHandler errorHandler;
    
    private WorkflowConfig config;
    private final AtomicBoolean isPolling = new AtomicBoolean(false);
    
    // For manual approval workflow
    private GeneratedResponse pendingResponse;
    private Comment pendingComment;
    private Post pendingPost;

    public WorkflowOrchestrator(LinkedInApiClient apiClient, 
                               LLMAgent llmAgent, 
                               StorageRepository storageRepository,
                               ErrorHandler errorHandler) {
        this.apiClient = apiClient;
        this.llmAgent = llmAgent;
        this.storageRepository = storageRepository;
        this.errorHandler = errorHandler;
    }

    /**
     * Starts polling for new comments on the configured post.
     * 
     * @param config Workflow configuration
     */
    public void startPolling(WorkflowConfig config) {
        if (config == null || config.getPostId() == null || config.getPostId().isBlank()) {
            throw new IllegalArgumentException("Invalid workflow configuration: postId is required");
        }
        
        logger.info("Starting polling for post: {}", config.getPostId());
        this.config = config;
        isPolling.set(true);
        logger.info("Polling started with interval: {} seconds", config.getPollingIntervalSeconds());
    }

    /**
     * Stops the polling process.
     */
    public void stopPolling() {
        logger.info("Stopping polling");
        isPolling.set(false);
        config = null;
        logger.info("Polling stopped");
    }

    /**
     * Scheduled method that polls for new comments.
     * Runs based on the configured polling interval.
     */
    @Scheduled(fixedDelayString = "${linkedin.polling.interval:300000}")
    public void pollForComments() {
        if (!isPolling.get() || config == null) {
            return;
        }
        
        logger.info("Polling for new comments on post: {}", config.getPostId());
        
        try {
            // Fetch all comments for the post
            List<Comment> allComments = apiClient.fetchComments(config.getPostId());
            logger.info("Fetched {} total comments", allComments.size());
            
            // Filter out already processed comments
            List<Comment> unprocessedComments = allComments.stream()
                    .filter(comment -> !storageRepository.isCommentProcessed(comment.getId()))
                    .collect(Collectors.toList());
            
            logger.info("Found {} unprocessed comments", unprocessedComments.size());
            
            // Process each unprocessed comment
            for (Comment comment : unprocessedComments) {
                try {
                    processComment(comment);
                } catch (Exception e) {
                    logger.error("Failed to process comment {}: {}", comment.getId(), e.getMessage(), e);
                    // Continue processing other comments even if one fails
                }
            }
            
        } catch (Exception e) {
            logger.error("Error during polling: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes a single comment through the complete workflow.
     * 
     * @param comment The comment to process
     */
    public void processComment(Comment comment) {
        // Set correlation ID for this workflow execution
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        
        logger.info("Processing comment: {} from {}", comment.getId(), comment.getAuthorName());
        
        try {
            // Step 1: Fetch the original post
            Post post = apiClient.fetchPost(comment.getPostId());
            logger.debug("Fetched post: {}", post.getId());
            
            // Step 2: Check if manual review is required based on keywords
            boolean requiresManualReview = checkManualReviewRequired(comment);
            
            // Step 3: Generate response using LLM
            GeneratedResponse generatedResponse = llmAgent.generateResponse(
                    post, 
                    comment, 
                    config.getTonePreference()
            );
            logger.info("Generated response for comment {}: {}", 
                    comment.getId(), generatedResponse.getText().substring(0, Math.min(50, generatedResponse.getText().length())));
            
            // Create interaction record
            Interaction interaction = createInteraction(post, comment, generatedResponse);
            interaction.setStatus(ResponseStatus.GENERATED);
            storageRepository.saveInteraction(interaction);
            
            // Step 4: Handle approval workflow
            if (config.isRequireManualApproval() || requiresManualReview) {
                handleManualApprovalWorkflow(post, comment, generatedResponse, interaction);
            } else {
                handleAutomaticPostingWorkflow(comment, generatedResponse, interaction);
            }
            
        } catch (Exception e) {
            // Use centralized error handler
            Map<String, String> context = new HashMap<>();
            context.put("commentId", comment.getId());
            context.put("postId", comment.getPostId());
            context.put("commenterName", comment.getAuthorName());
            errorHandler.handleError(e, context);
            
            // Save failed interaction
            Interaction failedInteraction = new Interaction();
            failedInteraction.setId(UUID.randomUUID().toString());
            failedInteraction.setCommentId(comment.getId());
            failedInteraction.setPostId(comment.getPostId());
            failedInteraction.setCommenterName(comment.getAuthorName());
            failedInteraction.setCommentText(comment.getText());
            failedInteraction.setTimestamp(Instant.now());
            failedInteraction.setStatus(ResponseStatus.FAILED);
            failedInteraction.getMetadata().put("error", e.getMessage());
            failedInteraction.getMetadata().put("correlationId", correlationId);
            storageRepository.saveInteraction(failedInteraction);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Handles the manual approval workflow.
     * Stores the pending response and waits for user approval.
     */
    private void handleManualApprovalWorkflow(Post post, Comment comment, 
                                             GeneratedResponse generatedResponse, 
                                             Interaction interaction) {
        logger.info("Manual approval required for comment: {}", comment.getId());
        
        // Store pending items for approval
        this.pendingPost = post;
        this.pendingComment = comment;
        this.pendingResponse = generatedResponse;
        
        // Log for user to review
        logger.info("=== MANUAL APPROVAL REQUIRED ===");
        logger.info("Comment from {}: {}", comment.getAuthorName(), comment.getText());
        logger.info("Generated Response: {}", generatedResponse.getText());
        logger.info("Confidence Score: {}", generatedResponse.getConfidenceScore());
        if (generatedResponse.getReasoning() != null) {
            logger.info("Reasoning: {}", generatedResponse.getReasoning());
        }
        if (generatedResponse.getWarnings() != null && !generatedResponse.getWarnings().isEmpty()) {
            logger.warn("Warnings: {}", String.join(", ", generatedResponse.getWarnings()));
        }
        logger.info("Call approveResponse() to approve or rejectResponse() to reject");
        logger.info("================================");
        
        // Update interaction status
        interaction.setStatus(ResponseStatus.GENERATED);
        storageRepository.saveInteraction(interaction);
    }

    /**
     * Handles the automatic posting workflow.
     * Posts the response immediately without user approval.
     */
    private void handleAutomaticPostingWorkflow(Comment comment, 
                                               GeneratedResponse generatedResponse, 
                                               Interaction interaction) {
        logger.info("Automatic posting enabled for comment: {}", comment.getId());
        
        // Post the response
        PostResult result = postResponse(comment, generatedResponse.getText());
        
        if (result.isSuccess()) {
            logger.info("Successfully posted response to comment: {}", comment.getId());
            
            // Mark comment as processed
            storageRepository.markCommentProcessed(comment.getId());
            
            // Update interaction
            interaction.setStatus(ResponseStatus.POSTED);
            interaction.setPostedResponse(generatedResponse.getText());
            interaction.getMetadata().put("response_id", result.getResponseId());
            storageRepository.saveInteraction(interaction);
        } else {
            logger.error("Failed to post response to comment {}: {}", 
                    comment.getId(), result.getErrorMessage());
            
            // Update interaction with failure
            interaction.setStatus(ResponseStatus.FAILED);
            interaction.getMetadata().put("error", result.getErrorMessage());
            interaction.getMetadata().put("status_code", String.valueOf(result.getStatusCode()));
            storageRepository.saveInteraction(interaction);
        }
    }

    /**
     * Approves and posts the pending response.
     * Used in manual approval workflow.
     * 
     * @return true if successfully posted, false otherwise
     */
    public boolean approveResponse() {
        if (pendingResponse == null || pendingComment == null) {
            logger.warn("No pending response to approve");
            return false;
        }
        
        logger.info("Approving response for comment: {}", pendingComment.getId());
        
        // Post the response
        PostResult result = postResponse(pendingComment, pendingResponse.getText());
        
        if (result.isSuccess()) {
            logger.info("Successfully posted approved response");
            
            // Mark comment as processed
            storageRepository.markCommentProcessed(pendingComment.getId());
            
            // Update interaction
            Interaction interaction = createInteraction(pendingPost, pendingComment, pendingResponse);
            interaction.setStatus(ResponseStatus.POSTED);
            interaction.setPostedResponse(pendingResponse.getText());
            interaction.getMetadata().put("response_id", result.getResponseId());
            interaction.getMetadata().put("manually_approved", "true");
            storageRepository.saveInteraction(interaction);
            
            // Clear pending items
            clearPendingItems();
            return true;
        } else {
            logger.error("Failed to post approved response: {}", result.getErrorMessage());
            
            // Update interaction with failure
            Interaction interaction = createInteraction(pendingPost, pendingComment, pendingResponse);
            interaction.setStatus(ResponseStatus.FAILED);
            interaction.getMetadata().put("error", result.getErrorMessage());
            interaction.getMetadata().put("manually_approved", "true");
            storageRepository.saveInteraction(interaction);
            
            // Clear pending items
            clearPendingItems();
            return false;
        }
    }

    /**
     * Rejects the pending response.
     * Used in manual approval workflow.
     */
    public void rejectResponse() {
        if (pendingResponse == null || pendingComment == null) {
            logger.warn("No pending response to reject");
            return;
        }
        
        logger.info("Rejecting response for comment: {}", pendingComment.getId());
        
        // Update interaction
        Interaction interaction = createInteraction(pendingPost, pendingComment, pendingResponse);
        interaction.setStatus(ResponseStatus.REJECTED);
        interaction.getMetadata().put("manually_rejected", "true");
        storageRepository.saveInteraction(interaction);
        
        // Clear pending items
        clearPendingItems();
    }

    /**
     * Posts a response to a comment with retry logic.
     */
    private PostResult postResponse(Comment comment, String responseText) {
        int attempt = 0;
        PostResult result = null;
        
        while (attempt < config.getMaxRetries()) {
            attempt++;
            logger.info("Posting response (attempt {}/{})", attempt, config.getMaxRetries());
            
            result = apiClient.postReply(comment.getId(), responseText);
            
            if (result.isSuccess()) {
                return result;
            }
            
            // Don't retry on client errors (except rate limits)
            if (result.getStatusCode() >= 400 && result.getStatusCode() < 500 
                    && result.getStatusCode() != 429) {
                logger.error("Client error, not retrying: {}", result.getStatusCode());
                return result;
            }
            
            if (attempt < config.getMaxRetries()) {
                long backoffMs = (long) (config.getRetryBackoffSeconds() * 1000 * Math.pow(2, attempt - 1));
                logger.info("Retrying in {} ms", backoffMs);
                
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted during retry backoff", e);
                    return result;
                }
            }
        }
        
        logger.error("Failed to post response after {} attempts", config.getMaxRetries());
        return result;
    }

    /**
     * Checks if a comment requires manual review based on configured keywords.
     */
    private boolean checkManualReviewRequired(Comment comment) {
        if (config.getManualReviewKeywords() == null || config.getManualReviewKeywords().isEmpty()) {
            return false;
        }
        
        String commentTextLower = comment.getText().toLowerCase();
        
        for (String keyword : config.getManualReviewKeywords()) {
            if (commentTextLower.contains(keyword.toLowerCase())) {
                logger.info("Comment contains manual review keyword '{}': {}", keyword, comment.getId());
                return true;
            }
        }
        
        return false;
    }

    /**
     * Creates an Interaction record from the workflow components.
     */
    private Interaction createInteraction(Post post, Comment comment, GeneratedResponse generatedResponse) {
        Interaction interaction = new Interaction();
        interaction.setId(UUID.randomUUID().toString());
        interaction.setPostId(post.getId());
        interaction.setCommentId(comment.getId());
        interaction.setCommenterName(comment.getAuthorName());
        interaction.setCommentText(comment.getText());
        interaction.setGeneratedResponse(generatedResponse.getText());
        interaction.setTimestamp(Instant.now());
        
        // Add metadata
        interaction.getMetadata().put("confidence_score", String.valueOf(generatedResponse.getConfidenceScore()));
        if (generatedResponse.getReasoning() != null) {
            interaction.getMetadata().put("reasoning", generatedResponse.getReasoning());
        }
        if (generatedResponse.getWarnings() != null && !generatedResponse.getWarnings().isEmpty()) {
            interaction.getMetadata().put("warnings", String.join("; ", generatedResponse.getWarnings()));
        }
        interaction.getMetadata().put("tone_preference", config.getTonePreference());
        
        return interaction;
    }

    /**
     * Clears pending items after approval or rejection.
     */
    private void clearPendingItems() {
        pendingResponse = null;
        pendingComment = null;
        pendingPost = null;
    }

    // Getters for testing and management
    public boolean isPolling() {
        return isPolling.get();
    }

    public WorkflowConfig getConfig() {
        return config;
    }

    public GeneratedResponse getPendingResponse() {
        return pendingResponse;
    }

    public Comment getPendingComment() {
        return pendingComment;
    }
}
