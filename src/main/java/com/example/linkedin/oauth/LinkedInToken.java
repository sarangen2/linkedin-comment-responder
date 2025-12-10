package com.example.linkedin.oauth;

import java.time.Instant;

/**
 * Represents a LinkedIn OAuth 2.0 access token with metadata.
 */
public class LinkedInToken {
    
    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private Instant expiresAt;
    private Instant createdAt;
    private String scope;
    private String refreshToken;
    
    public LinkedInToken() {
    }
    
    public LinkedInToken(String accessToken, String tokenType, int expiresIn, String scope) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(expiresIn);
    }
    
    /**
     * Check if the token is expired.
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return true;
        }
        // Consider token expired 5 minutes before actual expiry for safety
        return Instant.now().isAfter(expiresAt.minusSeconds(300));
    }
    
    /**
     * Check if the token is valid (not null and not expired).
     */
    public boolean isValid() {
        return accessToken != null && !accessToken.isBlank() && !isExpired();
    }
    
    /**
     * Get time until expiry in seconds.
     */
    public long getSecondsUntilExpiry() {
        if (expiresAt == null) {
            return 0;
        }
        return expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
    }
    
    /**
     * Get days until expiry.
     */
    public long getDaysUntilExpiry() {
        return getSecondsUntilExpiry() / (24 * 60 * 60);
    }
    
    // Getters and Setters
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public int getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public Instant getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "LinkedInToken{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", scope='" + scope + '\'' +
                ", hasRefreshToken=" + (refreshToken != null) +
                ", isValid=" + isValid() +
                '}';
    }
}