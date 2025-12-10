package com.example.linkedin.api;

import com.example.linkedin.api.dto.ApiResponse;
import com.example.linkedin.client.LinkedInApiClient;
import com.example.linkedin.model.LinkedInCredentials;
import com.example.linkedin.oauth.LinkedInOAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for testing LinkedIn API connectivity and profile information.
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "LinkedIn Profile Test", description = "APIs for testing LinkedIn profile connectivity")
public class ProfileTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileTestController.class);
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final LinkedInOAuthService oauthService;
    
    @Value("${linkedin.api.access-token:}")
    private String configuredAccessToken;
    
    @Value("${linkedin.api.client-id:}")
    private String clientId;
    
    @Value("${linkedin.api.client-secret:}")
    private String clientSecret;

    public ProfileTestController(LinkedInOAuthService oauthService) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.linkedin.com/v2")
                .build();
        this.objectMapper = new ObjectMapper();
        this.oauthService = oauthService;
    }

    /**
     * Test LinkedIn API connectivity and get basic profile information.
     */
    @GetMapping("/profile")
    @Operation(
        summary = "Test LinkedIn profile access",
        description = "Tests LinkedIn API connectivity and retrieves basic profile information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile information retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication failed - invalid or expired token",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access forbidden - insufficient permissions",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> testProfile() {
        logger.info("Testing LinkedIn profile access");
        
        // Try to get access token from OAuth service first, then fall back to configured token
        String accessToken = oauthService.getValidAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            accessToken = configuredAccessToken;
        }
        
        if (accessToken == null || accessToken.isBlank()) {
            logger.error("No LinkedIn access token available");
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("hasOAuthToken", oauthService.hasValidToken());
            errorInfo.put("hasConfiguredToken", configuredAccessToken != null && !configuredAccessToken.isBlank());
            errorInfo.put("authorizationUrl", oauthService.getAuthorizationUrl());
            errorInfo.put("instructions", "Visit the authorization URL to authenticate with LinkedIn");
            
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No access token available - OAuth required", "MISSING_TOKEN", errorInfo));
        }
        
        try {
            // Call LinkedIn API to get profile information
            String response = webClient.get()
                    .uri("/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            logger.debug("LinkedIn API response: {}", response);
            
            // Parse the response
            JsonNode profileNode = objectMapper.readTree(response);
            
            // Extract key information
            Map<String, Object> profileInfo = new HashMap<>();
            profileInfo.put("id", profileNode.has("id") ? profileNode.get("id").asText() : "N/A");
            
            // Extract localized first name
            if (profileNode.has("localizedFirstName")) {
                profileInfo.put("firstName", profileNode.get("localizedFirstName").asText());
            }
            
            // Extract localized last name
            if (profileNode.has("localizedLastName")) {
                profileInfo.put("lastName", profileNode.get("localizedLastName").asText());
            }
            
            // Extract profile picture if available
            if (profileNode.has("profilePicture")) {
                JsonNode pictureNode = profileNode.get("profilePicture");
                if (pictureNode.has("displayImage")) {
                    profileInfo.put("profilePicture", pictureNode.get("displayImage").asText());
                }
            }
            
            // Add API test metadata
            profileInfo.put("apiTestStatus", "SUCCESS");
            profileInfo.put("tokenValid", true);
            profileInfo.put("tokenSource", accessToken.equals(configuredAccessToken) ? "CONFIGURED" : "OAUTH");
            profileInfo.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully retrieved profile for user: {} {}", 
                    profileInfo.get("firstName"), profileInfo.get("lastName"));
            
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profileInfo));
            
        } catch (WebClientResponseException e) {
            logger.error("LinkedIn API error: {} - {}", e.getStatusCode(), e.getMessage());
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("apiTestStatus", "FAILED");
            errorInfo.put("tokenValid", false);
            errorInfo.put("statusCode", e.getStatusCode().value());
            errorInfo.put("error", e.getMessage());
            errorInfo.put("timestamp", System.currentTimeMillis());
            
            String errorMessage = switch (e.getStatusCode().value()) {
                case 401 -> "Invalid or expired access token";
                case 403 -> "Insufficient permissions - check API product access";
                case 429 -> "Rate limit exceeded - try again later";
                default -> "LinkedIn API error: " + e.getMessage();
            };
            
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(ApiResponse.error(errorMessage, "LINKEDIN_API_ERROR", errorInfo));
            
        } catch (Exception e) {
            logger.error("Unexpected error testing profile", e);
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("apiTestStatus", "FAILED");
            errorInfo.put("error", e.getMessage());
            errorInfo.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to test profile", "INTERNAL_ERROR", errorInfo));
        }
    }

    /**
     * Test LinkedIn API connectivity with custom access token.
     */
    @PostMapping("/profile/custom")
    @Operation(
        summary = "Test LinkedIn profile with custom token",
        description = "Tests LinkedIn API connectivity using a provided access token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile information retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request - missing access token",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication failed - invalid token",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> testProfileWithToken(
            @RequestBody Map<String, String> request) {
        
        String customToken = request.get("accessToken");
        
        if (customToken == null || customToken.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Access token is required", "MISSING_TOKEN"));
        }
        
        logger.info("Testing LinkedIn profile with custom token");
        
        try {
            // Call LinkedIn API with custom token
            String response = webClient.get()
                    .uri("/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + customToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Parse and return profile information (same logic as above)
            JsonNode profileNode = objectMapper.readTree(response);
            
            Map<String, Object> profileInfo = new HashMap<>();
            profileInfo.put("id", profileNode.has("id") ? profileNode.get("id").asText() : "N/A");
            
            if (profileNode.has("localizedFirstName")) {
                profileInfo.put("firstName", profileNode.get("localizedFirstName").asText());
            }
            
            if (profileNode.has("localizedLastName")) {
                profileInfo.put("lastName", profileNode.get("localizedLastName").asText());
            }
            
            if (profileNode.has("profilePicture")) {
                JsonNode pictureNode = profileNode.get("profilePicture");
                if (pictureNode.has("displayImage")) {
                    profileInfo.put("profilePicture", pictureNode.get("displayImage").asText());
                }
            }
            
            profileInfo.put("apiTestStatus", "SUCCESS");
            profileInfo.put("tokenValid", true);
            profileInfo.put("tokenSource", "CUSTOM");
            profileInfo.put("timestamp", System.currentTimeMillis());
            
            logger.info("Successfully tested custom token for user: {} {}", 
                    profileInfo.get("firstName"), profileInfo.get("lastName"));
            
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved with custom token", profileInfo));
            
        } catch (WebClientResponseException e) {
            logger.error("LinkedIn API error with custom token: {} - {}", e.getStatusCode(), e.getMessage());
            
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("apiTestStatus", "FAILED");
            errorInfo.put("tokenValid", false);
            errorInfo.put("tokenSource", "CUSTOM");
            errorInfo.put("statusCode", e.getStatusCode().value());
            errorInfo.put("timestamp", System.currentTimeMillis());
            
            String errorMessage = switch (e.getStatusCode().value()) {
                case 401 -> "Invalid or expired custom access token";
                case 403 -> "Custom token has insufficient permissions";
                case 429 -> "Rate limit exceeded with custom token";
                default -> "LinkedIn API error with custom token: " + e.getMessage();
            };
            
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(ApiResponse.error(errorMessage, "CUSTOM_TOKEN_ERROR", errorInfo));
            
        } catch (Exception e) {
            logger.error("Unexpected error testing custom token", e);
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to test custom token", "INTERNAL_ERROR"));
        }
    }

    /**
     * Get current LinkedIn API configuration status.
     */
    @GetMapping("/config")
    @Operation(
        summary = "Get LinkedIn API configuration status",
        description = "Returns the current LinkedIn API configuration status (without exposing sensitive data)"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfigStatus() {
        logger.info("Checking LinkedIn API configuration status");
        
        Map<String, Object> configStatus = new HashMap<>();
        
        // Check OAuth token status
        String oauthToken = oauthService.getValidAccessToken();
        Map<String, Object> oauthStatus = oauthService.getTokenStatus();
        
        // Check configuration without exposing sensitive data
        configStatus.put("hasOAuthToken", oauthToken != null && !oauthToken.isBlank());
        configStatus.put("hasConfiguredToken", configuredAccessToken != null && !configuredAccessToken.isBlank());
        configStatus.put("hasClientId", clientId != null && !clientId.isBlank());
        configStatus.put("hasClientSecret", clientSecret != null && !clientSecret.isBlank());
        
        configStatus.put("oauthTokenStatus", oauthStatus);
        
        if (configuredAccessToken != null && configuredAccessToken.length() > 10) {
            configStatus.put("configuredTokenPreview", configuredAccessToken.substring(0, 10) + "...");
        }
        
        configStatus.put("clientIdPreview", clientId != null && clientId.length() > 10 
                ? clientId.substring(0, 10) + "..." : "Not configured");
        
        // Environment info
        configStatus.put("apiBaseUrl", "https://api.linkedin.com/v2");
        configStatus.put("timestamp", System.currentTimeMillis());
        
        boolean hasAnyToken = (oauthToken != null && !oauthToken.isBlank()) || 
                             (configuredAccessToken != null && !configuredAccessToken.isBlank());
        String message = hasAnyToken ? "LinkedIn API is configured" : "LinkedIn API is not configured - OAuth required";
        
        return ResponseEntity.ok(ApiResponse.success(message, configStatus));
    }
}