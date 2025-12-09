package com.example.linkedin.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing circuit breakers for different services.
 */
@Component
public class CircuitBreakerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerRegistry.class);
    
    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers;
    
    // Default configuration
    private static final int DEFAULT_FAILURE_THRESHOLD = 5;
    private static final long DEFAULT_RESET_TIMEOUT_MS = 60000; // 60 seconds
    private static final int DEFAULT_SUCCESS_THRESHOLD = 3;
    
    public CircuitBreakerRegistry() {
        this.circuitBreakers = new ConcurrentHashMap<>();
        logger.info("Circuit breaker registry initialized");
    }
    
    /**
     * Gets or creates a circuit breaker for a service.
     * 
     * @param serviceName Name of the service
     * @return Circuit breaker instance
     */
    public CircuitBreaker getOrCreate(String serviceName) {
        return circuitBreakers.computeIfAbsent(serviceName, name -> {
            logger.info("Creating new circuit breaker for service: {}", name);
            return new CircuitBreaker(
                name,
                DEFAULT_FAILURE_THRESHOLD,
                DEFAULT_RESET_TIMEOUT_MS,
                DEFAULT_SUCCESS_THRESHOLD
            );
        });
    }
    
    /**
     * Gets or creates a circuit breaker with custom configuration.
     */
    public CircuitBreaker getOrCreate(String serviceName, int failureThreshold, 
                                     long resetTimeoutMs, int successThreshold) {
        return circuitBreakers.computeIfAbsent(serviceName, name -> {
            logger.info("Creating new circuit breaker for service: {} with custom config", name);
            return new CircuitBreaker(name, failureThreshold, resetTimeoutMs, successThreshold);
        });
    }
    
    /**
     * Gets an existing circuit breaker.
     */
    public CircuitBreaker get(String serviceName) {
        return circuitBreakers.get(serviceName);
    }
    
    /**
     * Resets all circuit breakers.
     */
    public void resetAll() {
        logger.info("Resetting all circuit breakers");
        circuitBreakers.values().forEach(CircuitBreaker::reset);
    }
    
    /**
     * Gets the status of all circuit breakers.
     */
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Circuit Breaker Status:\n");
        
        circuitBreakers.forEach((name, breaker) -> {
            sb.append(String.format("  %s: %s (failures: %d)\n", 
                                   name, breaker.getState(), breaker.getFailureCount()));
        });
        
        return sb.toString();
    }
}
