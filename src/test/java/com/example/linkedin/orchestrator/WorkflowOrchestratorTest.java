package com.example.linkedin.orchestrator;

import com.example.linkedin.agent.LLMAgent;
import com.example.linkedin.client.LinkedInApiClient;
import com.example.linkedin.error.ErrorHandler;
import com.example.linkedin.model.*;
import com.example.linkedin.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkflowOrchestrator.
 */
class WorkflowOrchestratorTest {
    
    private LinkedInApiClient apiClient;
    private LLMAgent llmAgent;
    private StorageRepository storageRepository;
    private ErrorHandler errorHandler;
    private WorkflowOrchestrator orchestrator;
    
    @BeforeEach
    void setUp() {
        apiClient = mock(LinkedInApiClient.class);
        llmAgent = mock(LLMAgent.class);
        storageRepository = mock(StorageRepository.class);
        errorHandler = mock(ErrorHandler.class);
        orchestrator = new WorkflowOrchestrator(apiClient, llmAgent, storageRepository, errorHandler);
    }
    
    @Test
    void testStartPolling_ValidConfig() {
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("test-post-123");
        config.setPollingIntervalSeconds(60);
        
        orchestrator.startPolling(config);
        
        assertTrue(orchestrator.isPolling());
        assertEquals(config, orchestrator.getConfig());
    }
    
    @Test
    void testStartPolling_InvalidConfig() {
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("");
        
        assertThrows(IllegalArgumentException.class, () -> orchestrator.startPolling(config));
    }
    
    @Test
    void testStopPolling() {
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("test-post-123");
        
        orchestrator.startPolling(config);
        assertTrue(orchestrator.isPolling());
        
        orchestrator.stopPolling();
        assertFalse(orchestrator.isPolling());
        assertNull(orchestrator.getConfig());
    }
    
    @Test
    void testProcessComment_AutomaticMode_Success() {
        // Setup
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("post-123");
        config.setRequireManualApproval(false);
        config.setTonePreference("witty");
        config.setMaxRetries(3);
        config.setRetryBackoffSeconds(1);
        orchestrator.startPolling(config);
        
        Comment comment = new Comment();
        comment.setId("comment-123");
        comment.setPostId("post-123");
        comment.setAuthorName("John Doe");
        comment.setText("Great post!");
        comment.setTimestamp(Instant.now());
        
        Post post = new Post();
        post.setId("post-123");
        post.setContent("My awesome post");
        post.setCreatedAt(Instant.now());
        
        GeneratedResponse response = new GeneratedResponse();
        response.setText("Thanks John! Glad you enjoyed it!");
        response.setConfidenceScore(0.95);
        
        PostResult postResult = new PostResult(true, "response-123");
        
        when(apiClient.fetchPost("post-123")).thenReturn(post);
        when(llmAgent.generateResponse(any(Post.class), any(Comment.class), eq("witty")))
                .thenReturn(response);
        when(apiClient.postReply("comment-123", response.getText())).thenReturn(postResult);
        when(storageRepository.isCommentProcessed("comment-123")).thenReturn(false);
        
        // Execute
        orchestrator.processComment(comment);
        
        // Verify
        verify(apiClient).fetchPost("post-123");
        verify(llmAgent).generateResponse(post, comment, "witty");
        verify(apiClient).postReply("comment-123", response.getText());
        verify(storageRepository).markCommentProcessed("comment-123");
        verify(storageRepository, times(2)).saveInteraction(any(Interaction.class));
    }
    
    @Test
    void testProcessComment_ManualApprovalMode() {
        // Setup
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("post-123");
        config.setRequireManualApproval(true);
        config.setTonePreference("witty");
        orchestrator.startPolling(config);
        
        Comment comment = new Comment();
        comment.setId("comment-123");
        comment.setPostId("post-123");
        comment.setAuthorName("Jane Doe");
        comment.setText("Interesting perspective!");
        comment.setTimestamp(Instant.now());
        
        Post post = new Post();
        post.setId("post-123");
        post.setContent("My thought-provoking post");
        post.setCreatedAt(Instant.now());
        
        GeneratedResponse response = new GeneratedResponse();
        response.setText("Thanks Jane! I appreciate your feedback!");
        response.setConfidenceScore(0.88);
        
        when(apiClient.fetchPost("post-123")).thenReturn(post);
        when(llmAgent.generateResponse(any(Post.class), any(Comment.class), eq("witty")))
                .thenReturn(response);
        
        // Execute
        orchestrator.processComment(comment);
        
        // Verify - should NOT post automatically
        verify(apiClient).fetchPost("post-123");
        verify(llmAgent).generateResponse(post, comment, "witty");
        verify(apiClient, never()).postReply(anyString(), anyString());
        verify(storageRepository, never()).markCommentProcessed(anyString());
        
        // Verify pending items are set
        assertNotNull(orchestrator.getPendingResponse());
        assertNotNull(orchestrator.getPendingComment());
    }
    
    @Test
    void testApproveResponse_Success() {
        // Setup - first process a comment in manual mode
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("post-123");
        config.setRequireManualApproval(true);
        config.setTonePreference("witty");
        config.setMaxRetries(3);
        config.setRetryBackoffSeconds(1);
        orchestrator.startPolling(config);
        
        Comment comment = new Comment();
        comment.setId("comment-123");
        comment.setPostId("post-123");
        comment.setAuthorName("Jane Doe");
        comment.setText("Great work!");
        comment.setTimestamp(Instant.now());
        
        Post post = new Post();
        post.setId("post-123");
        post.setContent("My post");
        post.setCreatedAt(Instant.now());
        
        GeneratedResponse response = new GeneratedResponse();
        response.setText("Thanks Jane!");
        response.setConfidenceScore(0.9);
        
        PostResult postResult = new PostResult(true, "response-123");
        
        when(apiClient.fetchPost("post-123")).thenReturn(post);
        when(llmAgent.generateResponse(any(Post.class), any(Comment.class), eq("witty")))
                .thenReturn(response);
        when(apiClient.postReply("comment-123", response.getText())).thenReturn(postResult);
        
        orchestrator.processComment(comment);
        
        // Execute approval
        boolean result = orchestrator.approveResponse();
        
        // Verify
        assertTrue(result);
        verify(apiClient).postReply("comment-123", response.getText());
        verify(storageRepository).markCommentProcessed("comment-123");
        assertNull(orchestrator.getPendingResponse());
        assertNull(orchestrator.getPendingComment());
    }
    
    @Test
    void testRejectResponse() {
        // Setup - first process a comment in manual mode
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("post-123");
        config.setRequireManualApproval(true);
        config.setTonePreference("witty");
        orchestrator.startPolling(config);
        
        Comment comment = new Comment();
        comment.setId("comment-123");
        comment.setPostId("post-123");
        comment.setAuthorName("Jane Doe");
        comment.setText("Not sure about this");
        comment.setTimestamp(Instant.now());
        
        Post post = new Post();
        post.setId("post-123");
        post.setContent("Controversial post");
        post.setCreatedAt(Instant.now());
        
        GeneratedResponse response = new GeneratedResponse();
        response.setText("Let me explain...");
        response.setConfidenceScore(0.7);
        
        when(apiClient.fetchPost("post-123")).thenReturn(post);
        when(llmAgent.generateResponse(any(Post.class), any(Comment.class), eq("witty")))
                .thenReturn(response);
        
        orchestrator.processComment(comment);
        
        // Execute rejection
        orchestrator.rejectResponse();
        
        // Verify
        verify(apiClient, never()).postReply(anyString(), anyString());
        verify(storageRepository, never()).markCommentProcessed(anyString());
        
        ArgumentCaptor<Interaction> captor = ArgumentCaptor.forClass(Interaction.class);
        verify(storageRepository, times(3)).saveInteraction(captor.capture());
        
        List<Interaction> interactions = captor.getAllValues();
        // First save is in processComment, second in handleManualApprovalWorkflow, third in rejectResponse
        assertEquals(ResponseStatus.REJECTED, interactions.get(2).getStatus());
        
        assertNull(orchestrator.getPendingResponse());
        assertNull(orchestrator.getPendingComment());
    }
    
    @Test
    void testKeywordTriggeredManualReview() {
        // Setup
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("post-123");
        config.setRequireManualApproval(false); // Automatic mode
        config.setTonePreference("witty");
        config.setManualReviewKeywords(Arrays.asList("controversial", "offensive"));
        orchestrator.startPolling(config);
        
        Comment comment = new Comment();
        comment.setId("comment-123");
        comment.setPostId("post-123");
        comment.setAuthorName("Bob Smith");
        comment.setText("This is a controversial topic!");
        comment.setTimestamp(Instant.now());
        
        Post post = new Post();
        post.setId("post-123");
        post.setContent("Discussion post");
        post.setCreatedAt(Instant.now());
        
        GeneratedResponse response = new GeneratedResponse();
        response.setText("Let's discuss this respectfully");
        response.setConfidenceScore(0.85);
        
        when(apiClient.fetchPost("post-123")).thenReturn(post);
        when(llmAgent.generateResponse(any(Post.class), any(Comment.class), eq("witty")))
                .thenReturn(response);
        
        // Execute
        orchestrator.processComment(comment);
        
        // Verify - should require manual review despite automatic mode
        verify(apiClient).fetchPost("post-123");
        verify(llmAgent).generateResponse(post, comment, "witty");
        verify(apiClient, never()).postReply(anyString(), anyString());
        
        // Should have pending items
        assertNotNull(orchestrator.getPendingResponse());
        assertNotNull(orchestrator.getPendingComment());
    }
    
    @Test
    void testPollForComments_FiltersProcessedComments() {
        // Setup
        WorkflowConfig config = new WorkflowConfig();
        config.setPostId("post-123");
        config.setRequireManualApproval(false);
        config.setTonePreference("witty");
        config.setMaxRetries(3);
        config.setRetryBackoffSeconds(1);
        orchestrator.startPolling(config);
        
        Comment comment1 = new Comment();
        comment1.setId("comment-1");
        comment1.setPostId("post-123");
        comment1.setAuthorName("User 1");
        comment1.setText("First comment");
        comment1.setTimestamp(Instant.now());
        
        Comment comment2 = new Comment();
        comment2.setId("comment-2");
        comment2.setPostId("post-123");
        comment2.setAuthorName("User 2");
        comment2.setText("Second comment");
        comment2.setTimestamp(Instant.now());
        
        List<Comment> allComments = Arrays.asList(comment1, comment2);
        
        Post post = new Post();
        post.setId("post-123");
        post.setContent("Test post");
        post.setCreatedAt(Instant.now());
        
        GeneratedResponse response = new GeneratedResponse();
        response.setText("Thanks!");
        response.setConfidenceScore(0.9);
        
        PostResult postResult = new PostResult(true, "response-id");
        
        when(apiClient.fetchComments("post-123")).thenReturn(allComments);
        when(storageRepository.isCommentProcessed("comment-1")).thenReturn(true); // Already processed
        when(storageRepository.isCommentProcessed("comment-2")).thenReturn(false); // Not processed
        when(apiClient.fetchPost("post-123")).thenReturn(post);
        when(llmAgent.generateResponse(any(Post.class), any(Comment.class), anyString()))
                .thenReturn(response);
        when(apiClient.postReply(anyString(), anyString())).thenReturn(postResult);
        
        // Execute
        orchestrator.pollForComments();
        
        // Verify - should only process comment-2
        verify(apiClient).fetchComments("post-123");
        verify(storageRepository).isCommentProcessed("comment-1");
        verify(storageRepository).isCommentProcessed("comment-2");
        verify(apiClient, times(1)).fetchPost("post-123"); // Only once for comment-2
        verify(llmAgent, times(1)).generateResponse(any(), any(), anyString()); // Only once
    }
}
