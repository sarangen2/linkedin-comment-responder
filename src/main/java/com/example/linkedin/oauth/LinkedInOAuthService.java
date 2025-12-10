package com.example.linkedin.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling LinkedIn OAuth 2.0 authentication flow.
 * Generates, caches, and refreshes LinkedIn access tokens.
 */
@Service
public class LinkedInOAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkedInOAuthService.class);
    
    private static final String LINKEDIN_AUTH_URL = "https://www.linkedin.com/oauth/v2/authorization";
    private static final String LINKEDIN_TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String REDIRECT_URI = "http://localhost:8080/auth/linkedin/callback";
    private static final String SCOPE = "r_liteprofile r_emailaddress w_member_social";
    
    @Value("${linkedin.api.client-id}")
    private String clientId;
    
    @Value("${linkedin.api.client-secret}")
    private String clientSecret;
    
    @Value("${linkedin.oauth.token-cache-file:./data/linkedin-token.json}")
    private String tokenCacheFile;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    // In-memory cache
    private LinkedInToken cachedToken;
    
    public LinkedInOAuthService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Generate the LinkedIn authorization URL for OAuth flow.
     */
    public String getAuthorizationUrl() {
        String state = generateState();
        
        return LINKEDIN_AUTH_URL + 
               "?response_type=code" +
               "&client_id=" + clientId +
               "&redirect_uri=" + REDIRECT_URI +
               "&state=" + state +
               "&scope=" + SCOPE.replace(" ", "%20");
    }
    
    /**
     * Exchange authorization code for access token.
     */
    public LinkedInToken exchangeCodeForToken(String authorizationCode, String state) {
        logger.info("Exchanging authorization code for access token");
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("code", authorizationCode);
            formData.add("redirect_uri", REDIRECT_URI);
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            
            String response = webClient.post()
                    .uri(LINKEDIN_TOKEN_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            logger.debug("Token exchange response: {}", response);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            LinkedInToken token = new LinkedInToken();
            token.setAccessToken(jsonNode.get("access_token").asText());
            token.setTokenType(jsonNode.get("token_type").asText());
            token.setExpiresIn(jsonNode.get("expires_in").asInt());
            token.setScope(jsonNode.has("scope") ? jsonNode.get("scope").asText() : SCOPE);
            
            // Set expiry time (LinkedIn tokens typically last 60 days)
            token.setExpiresAt(Instant.now().plusSeconds(token.getExpiresIn()));
            token.setCreatedAt(Instant.now());
            
            // Cache the token
            cacheToken(token);
            
            logger.info("Successfully obtained access token, expires at: {}", token.getExpiresAt());
            return token;
            
        } catch (Exception e) {
            logger.error("Failed to exchange authorization code for token", e);
            throw new RuntimeException("Failed to obtain access token", e);
        }
    }
    
    /**
     * Get a valid access token (from cache or refresh if needed).
     */
    public String getValidAccessToken() {
        LinkedInToken token = getCachedToken();
        
        if (token == null) {
            logger.warn("No cached token found. OAuth flow required.");
            return null;
        }
        
        if (token.isExpired()) {
            logger.info("Token is expired, attempting refresh");
            if (token.getRefreshToken() != null) {
                token = refreshToken(token.getRefreshToken());
            } else {
                logger.warn("Token expired and no refresh token available. Re-authentication required.");
                return null;
            }
        }
        
        return token != null ? token.getAccessToken() : null;
    }
    
    /**
     * Refresh an expired access token.
     */
    public LinkedInToken refreshToken(String refreshToken) {
        logger.info("Refreshing access token");
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("refresh_token", refreshToken);
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            
            String response = webClient.post()
                    .uri(LINKEDIN_TOKEN_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            LinkedInToken token = new LinkedInToken();
            token.setAccessToken(jsonNode.get("access_token").asText());
            token.setTokenType(jsonNode.get("token_type").asText());
            token.setExpiresIn(jsonNode.get("expires_in").asInt());
            token.setScope(jsonNode.has("scope") ? jsonNode.get("scope").asText() : SCOPE);
            
            // Keep the refresh token if provided, otherwise use the old one
            if (jsonNode.has("refresh_token")) {
                token.setRefreshToken(jsonNode.get("refresh_token").asText());
            } else {
                token.setRefreshToken(refreshToken);
            }
            
            token.setExpiresAt(Instant.now().plusSeconds(token.getExpiresIn()));
            token.setCreatedAt(Instant.now());
            
            // Cache the refreshed token
            cacheToken(token);
            
            logger.info("Successfully refreshed access token");
            return token;
            
        } catch (Exception e) {
            logger.error("Failed to refresh access token", e);
            return null;
        }
    }
    
    /**
     * Cache token to file and memory.
     */
    private void cacheToken(LinkedInToken token) {
        // Cache in memory
        this.cachedToken = token;
        
        // Cache to file
        try {
            // Ensure directory exists
            Path tokenPath = Paths.get(tokenCacheFile);
            Files.createDirectories(tokenPath.getParent());
            
            // Convert to JSON and save
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", token.getAccessToken());
            tokenData.put("tokenType", token.getTokenType());
            tokenData.put("expiresIn", token.getExpiresIn());
            tokenData.put("expiresAt", token.getExpiresAt().getEpochSecond());
            tokenData.put("createdAt", token.getCreatedAt().getEpochSecond());
            tokenData.put("scope", token.getScope());
            if (token.getRefreshToken() != null) {
                tokenData.put("refreshToken", token.getRefreshToken());
            }
            
            String json = objectMapper.writeValueAsString(tokenData);
            Files.write(tokenPath, json.getBytes());
            
            logger.info("Token cached to file: {}", tokenCacheFile);
            
        } catch (IOException e) {
            logger.error("Failed to cache token to file", e);
        }
    }
    
    /**
     * Load cached token from file or memory.
     */
    private LinkedInToken getCachedToken() {
        // Return memory cache if available
        if (cachedToken != null) {
            return cachedToken;
        }
        
        // Try to load from file
        try {
            Path tokenPath = Paths.get(tokenCacheFile);
            if (!Files.exists(tokenPath)) {
                logger.debug("No cached token file found: {}", tokenCacheFile);
                return null;
            }
            
            String json = Files.readString(tokenPath);
            JsonNode jsonNode = objectMapper.readTree(json);
            
            LinkedInToken token = new LinkedInToken();
            token.setAccessToken(jsonNode.get("accessToken").asText());
            token.setTokenType(jsonNode.get("tokenType").asText());
            token.setExpiresIn(jsonNode.get("expiresIn").asInt());
            token.setExpiresAt(Instant.ofEpochSecond(jsonNode.get("expiresAt").asLong()));
            token.setCreatedAt(Instant.ofEpochSecond(jsonNode.get("createdAt").asLong()));
            token.setScope(jsonNode.has("scope") ? jsonNode.get("scope").asText() : SCOPE);
            
            if (jsonNode.has("refreshToken")) {
                token.setRefreshToken(jsonNode.get("refreshToken").asText());
            }
            
            // Cache in memory
            this.cachedToken = token;
            
            logger.info("Loaded cached token from file, expires at: {}", token.getExpiresAt());
            return token;
            
        } catch (Exception e) {
            logger.error("Failed to load cached token from file", e);
            return null;
        }
    }
    
    /**
     * Clear cached token (for logout or re-authentication).
     */
    public void clearCachedToken() {
        this.cachedToken = null;
        
        try {
            Path tokenPath = Paths.get(tokenCacheFile);
            if (Files.exists(tokenPath)) {
                Files.delete(tokenPath);
                logger.info("Cleared cached token file");
            }
        } catch (IOException e) {
            logger.error("Failed to delete cached token file", e);
        }
    }
    
    /**
     * Get token status information.
     */
    public Map<String, Object> getTokenStatus() {
        LinkedInToken token = getCachedToken();
        Map<String, Object> status = new HashMap<>();
        
        if (token == null) {
            status.put("hasToken", false);
            status.put("authRequired", true);
            status.put("authUrl", getAuthorizationUrl());
        } else {
            status.put("hasToken", true);
            status.put("isExpired", token.isExpired());
            status.put("expiresAt", token.getExpiresAt().getEpochSecond());
            status.put("createdAt", token.getCreatedAt().getEpochSecond());
            status.put("scope", token.getScope());
            status.put("hasRefreshToken", token.getRefreshToken() != null);
            status.put("authRequired", token.isExpired() && token.getRefreshToken() == null);
            
            if (token.isExpired() && token.getRefreshToken() == null) {
                status.put("authUrl", getAuthorizationUrl());
            }
        }
        
        return status;
    }
    
    /**
     * Generate a random state parameter for OAuth security.
     */
    private String generateState() {
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Check if we have a valid token available.
     */
    public boolean hasValidToken() {
        String token = getValidAccessToken();
        return token != null && !token.isBlank();
    }
}