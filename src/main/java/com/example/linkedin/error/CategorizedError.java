package com.example.linkedin.error;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a categorized error with context information.
 */
public class CategorizedError {
    private final String correlationId;
    private final ErrorCategory category;
    private final String message;
    private final Throwable cause;
    private final Instant timestamp;
    private final Map<String, String> context;
    private final boolean isCritical;
    
    public CategorizedError(String correlationId, ErrorCategory category, String message, 
                           Throwable cause, Map<String, String> context, boolean isCritical) {
        this.correlationId = correlationId;
        this.category = category;
        this.message = message;
        this.cause = cause;
        this.timestamp = Instant.now();
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
        this.isCritical = isCritical;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public ErrorCategory getCategory() {
        return category;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Map<String, String> getContext() {
        return new HashMap<>(context);
    }
    
    public boolean isCritical() {
        return isCritical;
    }
    
    public String getStackTrace() {
        if (cause == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append("\n");
        
        for (StackTraceElement element : cause.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        
        return sb.toString();
    }
}
