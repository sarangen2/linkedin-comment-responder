package com.example.linkedin.api;

import com.example.linkedin.api.dto.ApiResponse;
import com.example.linkedin.oauth.LinkedInOAuthService;
import com.example.linkedin.oauth.LinkedInToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for LinkedIn OAuth 2.0 authentication flow.
 */
@RestController
@RequestMapping("/auth/linkedin")
@Tag(name = "LinkedIn OAuth", description = "APIs for LinkedIn OAuth 2.0 authentication")
public class LinkedInOAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkedInOAuthController.class);
    
    private final LinkedInOAuthService oauthService;
    
    public LinkedInOAuthController(LinkedInOAuthService oauthService) {
        this.oauthService = oauthService;
    }
    
    /**
     * Get LinkedIn authorization URL to start OAuth flow.
     */
    @GetMapping("/authorize")
    @Operation(
        summary = "Get LinkedIn authorization URL",
        description = "Returns the LinkedIn authorization URL to start the OAuth 2.0 flow"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Authorization URL generated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthorizationUrl() {
        logger.info("Generating LinkedIn authorization URL");
        
        try {
            String authUrl = oauthService.getAuthorizationUrl();
            
            Map<String, Object> response = new HashMap<>();
            response.put("authorizationUrl", authUrl);
            response.put("instructions", "Visit this URL in your browser to authorize the application");
            response.put("callbackUrl", "http://localhost:8080/auth/linkedin/callback");
            
            return ResponseEntity.ok(ApiResponse.success("Authorization URL generated", response));
            
        } catch (Exception e) {
            logger.error("Failed to generate authorization URL", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate authorization URL", e.getMessage()));
        }
    }
    
    /**
     * Handle LinkedIn OAuth callback.
     */
    @GetMapping("/callback")
    @Operation(
        summary = "LinkedIn OAuth callback",
        description = "Handles the LinkedIn OAuth callback and exchanges authorization code for access token"
    )
    public ResponseEntity<String> handleCallback(
            @Parameter(description = "Authorization code from LinkedIn")
            @RequestParam("code") String code,
            @Parameter(description = "State parameter for security")
            @RequestParam("state") String state,
            @Parameter(description = "Error code if authorization failed")
            @RequestParam(value = "error", required = false) String error,
            @Parameter(description = "Error description")
            @RequestParam(value = "error_description", required = false) String errorDescription) {
        
        logger.info("Received LinkedIn OAuth callback - code: {}, state: {}, error: {}", 
                code != null ? "present" : "null", state, error);
        
        if (error != null) {
            logger.error("OAuth authorization failed: {} - {}", error, errorDescription);
            return ResponseEntity.ok(generateErrorPage(error, errorDescription));
        }
        
        try {
            LinkedInToken token = oauthService.exchangeCodeForToken(code, state);
            
            logger.info("Successfully obtained LinkedIn access token");
            return ResponseEntity.ok(generateSuccessPage(token));
            
        } catch (Exception e) {
            logger.error("Failed to exchange authorization code for token", e);
            return ResponseEntity.ok(generateErrorPage("token_exchange_failed", e.getMessage()));
        }
    }
    
    /**
     * Get current token status.
     */
    @GetMapping("/status")
    @Operation(
        summary = "Get OAuth token status",
        description = "Returns the current status of the LinkedIn OAuth token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token status retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTokenStatus() {
        logger.info("Checking LinkedIn OAuth token status");
        
        try {
            Map<String, Object> status = oauthService.getTokenStatus();
            
            String message = (Boolean) status.get("hasToken") 
                    ? "Token is available" 
                    : "No token available - authorization required";
            
            return ResponseEntity.ok(ApiResponse.success(message, status));
            
        } catch (Exception e) {
            logger.error("Failed to get token status", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get token status", e.getMessage()));
        }
    }
    
    /**
     * Clear cached token (logout).
     */
    @DeleteMapping("/token")
    @Operation(
        summary = "Clear cached token",
        description = "Clears the cached LinkedIn access token (logout)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token cleared successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Void>> clearToken() {
        logger.info("Clearing LinkedIn OAuth token");
        
        try {
            oauthService.clearCachedToken();
            return ResponseEntity.ok(ApiResponse.success("Token cleared successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to clear token", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to clear token", e.getMessage()));
        }
    }
    
    /**
     * Generate success page HTML.
     */
    private String generateSuccessPage(LinkedInToken token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>LinkedIn Authorization Successful</title>
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 50px auto; padding: 20px; }
                    .success { color: #28a745; }
                    .info { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .token-info { background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .next-steps { background: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    code { background: #f1f1f1; padding: 2px 4px; border-radius: 3px; }
                </style>
            </head>
            <body>
                <h1 class="success">✅ LinkedIn Authorization Successful!</h1>
                
                <div class="info">
                    <h3>What happened:</h3>
                    <ul>
                        <li>✅ Successfully connected to LinkedIn</li>
                        <li>✅ Access token obtained and cached</li>
                        <li>✅ Token will be automatically refreshed</li>
                        <li>✅ Valid for approximately 60 days</li>
                    </ul>
                </div>
                
                <div class="token-info">
                    <h3>Token Information:</h3>
                    <ul>
                        <li><strong>Expires:</strong> """ + token.getExpiresAt() + """</li>
                        <li><strong>Days until expiry:</strong> """ + token.getDaysUntilExpiry() + """</li>
                        <li><strong>Scope:</strong> """ + token.getScope() + """</li>
                        <li><strong>Has refresh token:</strong> """ + (token.getRefreshToken() != null ? "Yes" : "No") + """</li>
                    </ul>
                </div>
                
                <div class="next-steps">
                    <h3>Next Steps:</h3>
                    <ol>
                        <li>Close this browser tab</li>
                        <li>Test your LinkedIn profile:
                            <br><code>curl http://localhost:8080/api/test/profile</code>
                        </li>
                        <li>Start using the LinkedIn Comment Responder!</li>
                    </ol>
                </div>
                
                <p><a href="/api/test/profile">🔗 Test LinkedIn Profile Access</a></p>
                <p><a href="/swagger-ui.html">📚 View API Documentation</a></p>
            </body>
            </html>
            """;
    }
    
    /**
     * Generate error page HTML.
     */
    private String generateErrorPage(String error, String description) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>LinkedIn Authorization Failed</title>
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 50px auto; padding: 20px; }
                    .error { color: #dc3545; }
                    .info { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .troubleshooting { background: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    code { background: #f1f1f1; padding: 2px 4px; border-radius: 3px; }
                </style>
            </head>
            <body>
                <h1 class="error">❌ LinkedIn Authorization Failed</h1>
                
                <div class="info">
                    <h3>Error Details:</h3>
                    <ul>
                        <li><strong>Error:</strong> """ + error + """</li>
                        <li><strong>Description:</strong> """ + (description != null ? description : "No description provided") + """</li>
                    </ul>
                </div>
                
                <div class="troubleshooting">
                    <h3>Troubleshooting:</h3>
                    <ul>
                        <li>Check that your LinkedIn app is properly configured</li>
                        <li>Verify the redirect URI matches: <code>http://localhost:8080/auth/linkedin/callback</code></li>
                        <li>Ensure your app has the required API products enabled</li>
                        <li>Try the authorization process again</li>
                    </ul>
                </div>
                
                <p><a href="/auth/linkedin/authorize">🔄 Try Again</a></p>
                <p><a href="/auth/linkedin/status">📊 Check Token Status</a></p>
            </body>
            </html>
            """;
    }
}