package com.example.linkedin.error;

/**
 * Categories of errors that can occur in the system.
 * Used for error classification and handling strategies.
 */
public enum ErrorCategory {
    /**
     * Authentication and authorization errors (401, 403)
     */
    AUTHENTICATION,
    
    /**
     * Rate limiting errors (429)
     */
    RATE_LIMIT,
    
    /**
     * Resource not found errors (404)
     */
    NOT_FOUND,
    
    /**
     * Server errors from external services (500, 502, 503)
     */
    EXTERNAL_SERVICE,
    
    /**
     * LLM-specific errors (model unavailable, token limit, timeout)
     */
    LLM_ERROR,
    
    /**
     * Storage and persistence errors
     */
    STORAGE_ERROR,
    
    /**
     * Configuration and validation errors
     */
    CONFIGURATION_ERROR,
    
    /**
     * Network and connectivity errors
     */
    NETWORK_ERROR,
    
    /**
     * Unknown or uncategorized errors
     */
    UNKNOWN
}
