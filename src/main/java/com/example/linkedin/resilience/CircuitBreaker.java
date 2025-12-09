package com.example.linkedin.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Circuit breaker implementation for protecting external service calls.
 * Implements the circuit breaker pattern with three states: CLOSED, OPEN, HALF_OPEN.
 */
public class CircuitBreaker {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);
    
    private final String name;
    private final int failureThreshold;
    private final long resetTimeoutMs;
    private final int successThreshold;
    
    private final AtomicInteger failureCount;
    private final AtomicInteger successCount;
    private final AtomicReference<CircuitState> state;
    private final AtomicReference<Instant> lastFailureTime;
    
    public CircuitBreaker(String name, int failureThreshold, long resetTimeoutMs, int successThreshold) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
        this.successThreshold = successThreshold;
        
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.state = new AtomicReference<>(CircuitState.CLOSED);
        this.lastFailureTime = new AtomicReference<>(Instant.now());
        
        logger.info("Circuit breaker '{}' initialized. Failure threshold: {}, Reset timeout: {}ms, Success threshold: {}", 
                   name, failureThreshold, resetTimeoutMs, successThreshold);
    }
    
    /**
     * Executes an operation with circuit breaker protection.
     * 
     * @param operation The operation to execute
     * @return The result of the operation
     * @throws CircuitBreakerOpenException if the circuit is open
     * @throws Exception if the operation fails
     */
    public <T> T execute(Supplier<T> operation) throws Exception {
        CircuitState currentState = state.get();
        
        if (currentState == CircuitState.OPEN) {
            if (shouldAttemptReset()) {
                logger.info("Circuit breaker '{}' transitioning to HALF_OPEN", name);
                state.set(CircuitState.HALF_OPEN);
                successCount.set(0);
            } else {
                logger.warn("Circuit breaker '{}' is OPEN, rejecting call", name);
                throw new CircuitBreakerOpenException("Circuit breaker '" + name + "' is OPEN");
            }
        }
        
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    /**
     * Records a successful operation.
     */
    private void onSuccess() {
        CircuitState currentState = state.get();
        
        if (currentState == CircuitState.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            logger.debug("Circuit breaker '{}' success count in HALF_OPEN: {}/{}", 
                        name, successes, successThreshold);
            
            if (successes >= successThreshold) {
                logger.info("Circuit breaker '{}' transitioning to CLOSED after {} successes", 
                           name, successes);
                state.set(CircuitState.CLOSED);
                failureCount.set(0);
                successCount.set(0);
            }
        } else if (currentState == CircuitState.CLOSED) {
            // Reset failure count on success in CLOSED state
            if (failureCount.get() > 0) {
                failureCount.set(0);
                logger.debug("Circuit breaker '{}' reset failure count after success", name);
            }
        }
    }
    
    /**
     * Records a failed operation.
     */
    private void onFailure() {
        lastFailureTime.set(Instant.now());
        CircuitState currentState = state.get();
        
        if (currentState == CircuitState.HALF_OPEN) {
            logger.warn("Circuit breaker '{}' failed in HALF_OPEN, transitioning to OPEN", name);
            state.set(CircuitState.OPEN);
            failureCount.set(0);
            successCount.set(0);
        } else if (currentState == CircuitState.CLOSED) {
            int failures = failureCount.incrementAndGet();
            logger.debug("Circuit breaker '{}' failure count: {}/{}", name, failures, failureThreshold);
            
            if (failures >= failureThreshold) {
                logger.warn("Circuit breaker '{}' transitioning to OPEN after {} consecutive failures", 
                           name, failures);
                state.set(CircuitState.OPEN);
            }
        }
    }
    
    /**
     * Checks if enough time has passed to attempt resetting the circuit.
     */
    private boolean shouldAttemptReset() {
        Instant lastFailure = lastFailureTime.get();
        long timeSinceLastFailure = Instant.now().toEpochMilli() - lastFailure.toEpochMilli();
        return timeSinceLastFailure >= resetTimeoutMs;
    }
    
    /**
     * Gets the current state of the circuit breaker.
     */
    public CircuitState getState() {
        return state.get();
    }
    
    /**
     * Gets the current failure count.
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * Manually resets the circuit breaker to CLOSED state.
     */
    public void reset() {
        logger.info("Manually resetting circuit breaker '{}'", name);
        state.set(CircuitState.CLOSED);
        failureCount.set(0);
        successCount.set(0);
    }
    
    /**
     * Circuit breaker states.
     */
    public enum CircuitState {
        /**
         * Circuit is closed, requests flow normally.
         */
        CLOSED,
        
        /**
         * Circuit is open, requests are rejected.
         */
        OPEN,
        
        /**
         * Circuit is testing if the service has recovered.
         */
        HALF_OPEN
    }
    
    /**
     * Exception thrown when circuit breaker is open.
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
