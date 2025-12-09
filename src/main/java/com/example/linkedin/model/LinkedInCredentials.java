package com.example.linkedin.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * Credentials for LinkedIn API authentication.
 */
public class LinkedInCredentials {
    @NotBlank
    private String clientId;
    
    @NotBlank
    private String clientSecret;
    
    private String accessToken;
    
    private String refreshToken;
    
    private Instant tokenExpiry;

    // Constructors
    public LinkedInCredentials() {
    }

    public LinkedInCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Instant tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public boolean isTokenExpired() {
        return tokenExpiry != null && Instant.now().isAfter(tokenExpiry);
    }
}
