package com.example.linkedin.api;

import com.example.linkedin.api.dto.*;
import com.example.linkedin.model.Comment;
import com.example.linkedin.model.GeneratedResponse;
import com.example.linkedin.model.Interaction;
import com.example.linkedin.model.WorkflowConfig;
import com.example.linkedin.orchestrator.WorkflowOrchestrator;
import com.example.linkedin.repository.StorageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * REST controller for managing the LinkedIn Comment Responder workflow.
 */
@RestController
@RequestMapping("/api/management")
@Tag(name = "Management", description = "APIs for managing the LinkedIn Comment Responder workflow")
public class ManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(ManagementController.class);
    
    private final WorkflowOrchestrator orchestrator;
    private final StorageRepository storageRepository;

    public ManagementController(WorkflowOrchestrator orchestrator, StorageRepository storageRepository) {
        this.orchestrator = orchestrator;
        this.storageRepository = storageRepository;
    }

    /**
     * Start polling for comments on a LinkedIn post.
     */
    @PostMapping("/polling/start")
    @Operation(
        summary = "Start polling for comments",
        description = "Starts the automated polling workflow for a specific LinkedIn post"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Polling started successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Polling is already active",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> startPolling(
            @Valid @RequestBody StartPollingRequest request) {
        
        logger.info("Received request to start polling for post: {}", request.getPostId());
        
        try {
            // Check if already polling
            if (orchestrator.isPolling()) {
                logger.warn("Polling is already active");
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Polling is already active", "ALREADY_POLLING"));
            }
            
            // Create workflow config from request
            WorkflowConfig config = new WorkflowConfig();
            config.setPostId(request.getPostId());
            config.setPollingIntervalSeconds(request.getPollingIntervalSeconds());
            config.setRequireManualApproval(request.getRequireManualApproval());
            config.setTonePreference(request.getTonePreference());
            config.setManualReviewKeywords(request.getManualReviewKeywords());
            config.setMaxRetries(request.getMaxRetries());
            config.setRetryBackoffSeconds(request.getRetryBackoffSeconds());
            
            // Start polling
            orchestrator.startPolling(config);
            
            logger.info("Polling started successfully for post: {}", request.getPostId());
            return ResponseEntity.ok(ApiResponse.success("Polling started successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid configuration: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid configuration", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to start polling", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to start polling", e.getMessage()));
        }
    }

    /**
     * Stop polling for comments.
     */
    @PostMapping("/polling/stop")
    @Operation(
        summary = "Stop polling for comments",
        description = "Stops the automated polling workflow"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Polling stopped successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Polling is not active",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> stopPolling() {
        logger.info("Received request to stop polling");
        
        try {
            if (!orchestrator.isPolling()) {
                logger.warn("Polling is not active");
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("Polling is not active", "NOT_POLLING"));
            }
            
            orchestrator.stopPolling();
            
            logger.info("Polling stopped successfully");
            return ResponseEntity.ok(ApiResponse.success("Polling stopped successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to stop polling", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to stop polling", e.getMessage()));
        }
    }

    /**
     * Get polling status.
     */
    @GetMapping("/polling/status")
    @Operation(
        summary = "Get polling status",
        description = "Returns the current polling status and configuration"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Status retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<WorkflowConfig>> getPollingStatus() {
        logger.debug("Received request for polling status");
        
        boolean isPolling = orchestrator.isPolling();
        WorkflowConfig config = orchestrator.getConfig();
        
        if (isPolling && config != null) {
            return ResponseEntity.ok(ApiResponse.success("Polling is active", config));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Polling is not active", null));
        }
    }

    /**
     * Get pending response awaiting approval.
     */
    @GetMapping("/approval/pending")
    @Operation(
        summary = "Get pending response",
        description = "Returns the pending response awaiting manual approval, if any"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pending response retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No pending response",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<PendingResponseDto>> getPendingResponse() {
        logger.debug("Received request for pending response");
        
        GeneratedResponse pendingResponse = orchestrator.getPendingResponse();
        Comment pendingComment = orchestrator.getPendingComment();
        
        if (pendingResponse == null || pendingComment == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No pending response", "NO_PENDING_RESPONSE"));
        }
        
        PendingResponseDto dto = new PendingResponseDto();
        dto.setCommentId(pendingComment.getId());
        dto.setCommenterName(pendingComment.getAuthorName());
        dto.setCommentText(pendingComment.getText());
        dto.setGeneratedResponse(pendingResponse.getText());
        dto.setConfidenceScore(pendingResponse.getConfidenceScore());
        dto.setReasoning(pendingResponse.getReasoning());
        dto.setWarnings(pendingResponse.getWarnings());
        
        return ResponseEntity.ok(ApiResponse.success("Pending response retrieved", dto));
    }

    /**
     * Approve or reject a pending response.
     */
    @PostMapping("/approval/decision")
    @Operation(
        summary = "Approve or reject pending response",
        description = "Approves or rejects the pending response awaiting manual approval"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Decision processed successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No pending response",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Failed to post approved response",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> processApprovalDecision(
            @Valid @RequestBody ApprovalRequest request) {
        
        logger.info("Received approval decision: {}", request.getApprove());
        
        try {
            if (orchestrator.getPendingResponse() == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No pending response", "NO_PENDING_RESPONSE"));
            }
            
            if (request.getApprove()) {
                boolean success = orchestrator.approveResponse();
                if (success) {
                    logger.info("Response approved and posted successfully");
                    return ResponseEntity.ok(ApiResponse.success("Response approved and posted"));
                } else {
                    logger.error("Failed to post approved response");
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to post approved response", "POST_FAILED"));
                }
            } else {
                orchestrator.rejectResponse();
                logger.info("Response rejected. Reason: {}", request.getReason());
                return ResponseEntity.ok(ApiResponse.success("Response rejected"));
            }
            
        } catch (Exception e) {
            logger.error("Failed to process approval decision", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process decision", e.getMessage()));
        }
    }

    /**
     * Query interaction history.
     */
    @GetMapping("/history")
    @Operation(
        summary = "Query interaction history",
        description = "Retrieves interaction history with optional filters"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "History retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid date format",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<List<Interaction>>> queryHistory(
            @Parameter(description = "Filter by post ID") @RequestParam(required = false) String postId,
            @Parameter(description = "Start date (ISO-8601)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (ISO-8601)") @RequestParam(required = false) String endDate) {
        
        logger.info("Received history query request - postId: {}, startDate: {}, endDate: {}", 
                postId, startDate, endDate);
        
        try {
            Instant start = null;
            Instant end = null;
            
            if (startDate != null && !startDate.isBlank()) {
                start = Instant.parse(startDate);
            }
            
            if (endDate != null && !endDate.isBlank()) {
                end = Instant.parse(endDate);
            }
            
            List<Interaction> history = storageRepository.getInteractionHistory(postId, start, end);
            
            logger.info("Retrieved {} interactions", history.size());
            return ResponseEntity.ok(ApiResponse.success(
                    String.format("Retrieved %d interactions", history.size()), 
                    history
            ));
            
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid date format", "Use ISO-8601 format (e.g., 2024-01-01T00:00:00Z)"));
        } catch (Exception e) {
            logger.error("Failed to query history", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to query history", e.getMessage()));
        }
    }

    /**
     * Export interaction history.
     */
    @GetMapping("/history/export")
    @Operation(
        summary = "Export interaction history",
        description = "Exports interaction history to JSON or CSV format"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "History exported successfully",
            content = @Content(mediaType = "application/octet-stream")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid format",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> exportHistory(
            @Parameter(description = "Export format (json or csv)") 
            @RequestParam(defaultValue = "json") String format) {
        
        logger.info("Received history export request - format: {}", format);
        
        try {
            if (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv")) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("Invalid format", "Format must be 'json' or 'csv'"));
            }
            
            String filePath = storageRepository.exportHistory(format.toLowerCase());
            Resource resource = new FileSystemResource(filePath);
            
            if (!resource.exists()) {
                logger.error("Export file not found: {}", filePath);
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Export failed", "File not found"));
            }
            
            String contentType = format.equalsIgnoreCase("json") 
                    ? "application/json" 
                    : "text/csv";
            
            String filename = "interaction_history_" + Instant.now().getEpochSecond() + "." + format.toLowerCase();
            
            logger.info("Exporting history file: {}", filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
            
        } catch (Exception e) {
            logger.error("Failed to export history", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to export history", e.getMessage()));
        }
    }

    /**
     * Update workflow configuration.
     */
    @PatchMapping("/config")
    @Operation(
        summary = "Update workflow configuration",
        description = "Updates the workflow configuration while polling is active"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Configuration updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Polling is not active or invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<WorkflowConfig>> updateConfig(
            @Valid @RequestBody ConfigUpdateRequest request) {
        
        logger.info("Received config update request");
        
        try {
            if (!orchestrator.isPolling()) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("Cannot update config", "Polling is not active"));
            }
            
            WorkflowConfig currentConfig = orchestrator.getConfig();
            if (currentConfig == null) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("Cannot update config", "No active configuration"));
            }
            
            // Update only provided fields
            if (request.getPollingIntervalSeconds() != null) {
                currentConfig.setPollingIntervalSeconds(request.getPollingIntervalSeconds());
            }
            if (request.getRequireManualApproval() != null) {
                currentConfig.setRequireManualApproval(request.getRequireManualApproval());
            }
            if (request.getTonePreference() != null) {
                currentConfig.setTonePreference(request.getTonePreference());
            }
            if (request.getManualReviewKeywords() != null) {
                currentConfig.setManualReviewKeywords(request.getManualReviewKeywords());
            }
            if (request.getMaxRetries() != null) {
                currentConfig.setMaxRetries(request.getMaxRetries());
            }
            if (request.getRetryBackoffSeconds() != null) {
                currentConfig.setRetryBackoffSeconds(request.getRetryBackoffSeconds());
            }
            
            logger.info("Configuration updated successfully");
            return ResponseEntity.ok(ApiResponse.success("Configuration updated", currentConfig));
            
        } catch (Exception e) {
            logger.error("Failed to update configuration", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update configuration", e.getMessage()));
        }
    }
}
