package com.example.linkedin.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized error handler that categorizes errors and provides structured logging.
 * Implements correlation ID tracking for distributed tracing.
 */
@Component
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    private static final String CORRELATION_ID_KEY = "correlationId";
    
    private final ErrorNotificationService notificationService;
    
    public ErrorHandler(ErrorNotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Handles an error with automatic categorization and logging.
     * 
     * @param error The throwable error
     * @param context Additional context information
     * @return Categorized error object
     */
    public CategorizedError handleError(Throwable error, Map<String, String> context) {
        String correlationId = getOrCreateCorrelationId();
        ErrorCategory category = categorizeError(error);
        boolean isCritical = isCriticalError(category, error);
        
        CategorizedError categorizedError = new CategorizedError(
            correlationId,
            category,
            error.getMessage(),
            error,
            context,
            isCritical
        );
        
        logError(categorizedError);
        
        if (isCritical) {
            notificationService.sendCriticalErrorNotification(categorizedError);
        }
        
        return categorizedError;
    }
    
    /**
     * Handles an error with a custom message.
     */
    public CategorizedError handleError(String message, Throwable error, 
                                       ErrorCategory category, Map<String, String> context) {
        String correlationId = getOrCreateCorrelationId();
        boolean isCritical = isCriticalError(category, error);
        
        CategorizedError categorizedError = new CategorizedError(
            correlationId,
            category,
            message,
            error,
            context,
            isCritical
        );
        
        logError(categorizedError);
        
        if (isCritical) {
            notificationService.sendCriticalErrorNotification(categorizedError);
        }
        
        return categorizedError;
    }
    
    /**
     * Categorizes an error based on its type and characteristics.
     */
    private ErrorCategory categorizeError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException webError = (WebClientResponseException) error;
            int statusCode = webError.getStatusCode().value();
            
            if (statusCode == 401 || statusCode == 403) {
                return ErrorCategory.AUTHENTICATION;
            } else if (statusCode == 429) {
                return ErrorCategory.RATE_LIMIT;
            } else if (statusCode == 404) {
                return ErrorCategory.NOT_FOUND;
            } else if (statusCode >= 500) {
                return ErrorCategory.EXTERNAL_SERVICE;
            }
        }
        
        if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
            return ErrorCategory.CONFIGURATION_ERROR;
        }
        
        if (error instanceof java.io.IOException || error instanceof java.net.SocketException) {
            return ErrorCategory.NETWORK_ERROR;
        }
        
        String errorMessage = error.getMessage() != null ? error.getMessage().toLowerCase() : "";
        
        if (errorMessage.contains("llm") || errorMessage.contains("openai") || 
            errorMessage.contains("token limit") || errorMessage.contains("model")) {
            return ErrorCategory.LLM_ERROR;
        }
        
        if (errorMessage.contains("storage") || errorMessage.contains("database") || 
            errorMessage.contains("file") || errorMessage.contains("disk")) {
            return ErrorCategory.STORAGE_ERROR;
        }
        
        return ErrorCategory.UNKNOWN;
    }
    
    /**
     * Determines if an error is critical based on category and characteristics.
     */
    private boolean isCriticalError(ErrorCategory category, Throwable error) {
        switch (category) {
            case AUTHENTICATION:
            case CONFIGURATION_ERROR:
                return true;
            case EXTERNAL_SERVICE:
            case LLM_ERROR:
            case STORAGE_ERROR:
                // Critical if it's not a transient error
                return !(error.getMessage() != null && 
                        (error.getMessage().contains("timeout") || 
                         error.getMessage().contains("temporary")));
            case RATE_LIMIT:
            case NOT_FOUND:
            case NETWORK_ERROR:
            case UNKNOWN:
            default:
                return false;
        }
    }
    
    /**
     * Logs the error with structured logging and correlation ID.
     */
    private void logError(CategorizedError error) {
        // Add correlation ID to MDC for structured logging
        MDC.put(CORRELATION_ID_KEY, error.getCorrelationId());
        
        try {
            String logMessage = String.format(
                "[%s] %s - %s",
                error.getCategory(),
                error.getMessage(),
                error.getContext()
            );
            
            if (error.isCritical()) {
                logger.error(logMessage, error.getCause());
            } else {
                logger.warn(logMessage, error.getCause());
            }
            
            // Log stack trace separately for better readability
            if (error.getCause() != null) {
                logger.debug("Stack trace for correlation ID {}: {}", 
                           error.getCorrelationId(), error.getStackTrace());
            }
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
    
    /**
     * Gets the current correlation ID from MDC or creates a new one.
     */
    private String getOrCreateCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
        return correlationId;
    }
    
    /**
     * Sets a correlation ID for the current thread context.
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
    }
    
    /**
     * Clears the correlation ID from the current thread context.
     */
    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }
    
    /**
     * Gets the current correlation ID.
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }
}
