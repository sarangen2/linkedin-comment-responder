package com.example.linkedin.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Handles graceful degradation for non-critical failures.
 * Provides fallback mechanisms when primary operations fail.
 */
@Component
public class GracefulDegradationHandler {
    private static final Logger logger = LoggerFactory.getLogger(GracefulDegradationHandler.class);
    
    /**
     * Executes an operation with a fallback if it fails.
     * 
     * @param primary Primary operation to execute
     * @param fallback Fallback operation if primary fails
     * @param operationName Name of the operation for logging
     * @return Result from primary or fallback operation
     */
    public <T> T executeWithFallback(Supplier<T> primary, Supplier<T> fallback, String operationName) {
        try {
            logger.debug("Executing primary operation: {}", operationName);
            return primary.get();
        } catch (Exception e) {
            logger.warn("Primary operation '{}' failed, using fallback: {}", operationName, e.getMessage());
            try {
                T result = fallback.get();
                logger.info("Fallback operation '{}' succeeded", operationName);
                return result;
            } catch (Exception fallbackError) {
                logger.error("Fallback operation '{}' also failed", operationName, fallbackError);
                throw new RuntimeException("Both primary and fallback operations failed for: " + operationName, fallbackError);
            }
        }
    }
    
    /**
     * Executes an operation with optional result, returning empty on failure.
     * 
     * @param operation Operation to execute
     * @param operationName Name of the operation for logging
     * @return Optional result, empty if operation fails
     */
    public <T> Optional<T> executeOptional(Supplier<T> operation, String operationName) {
        try {
            logger.debug("Executing optional operation: {}", operationName);
            T result = operation.get();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            logger.warn("Optional operation '{}' failed, returning empty: {}", operationName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Executes an operation with a default value on failure.
     * 
     * @param operation Operation to execute
     * @param defaultValue Default value to return on failure
     * @param operationName Name of the operation for logging
     * @return Result from operation or default value
     */
    public <T> T executeWithDefault(Supplier<T> operation, T defaultValue, String operationName) {
        try {
            logger.debug("Executing operation with default: {}", operationName);
            return operation.get();
        } catch (Exception e) {
            logger.warn("Operation '{}' failed, using default value: {}", operationName, e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Executes an operation, logging but not throwing on failure.
     * 
     * @param operation Operation to execute
     * @param operationName Name of the operation for logging
     */
    public void executeNonCritical(Runnable operation, String operationName) {
        try {
            logger.debug("Executing non-critical operation: {}", operationName);
            operation.run();
        } catch (Exception e) {
            logger.warn("Non-critical operation '{}' failed, continuing: {}", operationName, e.getMessage());
        }
    }
}
