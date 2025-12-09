package com.example.linkedin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for approving or rejecting a generated response.
 */
@Schema(description = "Request to approve or reject a pending response")
public class ApprovalRequest {
    
    @NotNull
    @Schema(description = "Whether to approve the response", example = "true")
    private Boolean approve;
    
    @Schema(description = "Optional reason for rejection", example = "Response tone is too casual")
    private String reason;

    // Getters and Setters
    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
