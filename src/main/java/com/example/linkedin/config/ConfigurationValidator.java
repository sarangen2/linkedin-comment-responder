package com.example.linkedin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates required configuration parameters on application startup.
 * Fails fast with clear error messages if configuration is invalid.
 * 
 * Validates:
 * - LinkedIn API credentials and configuration
 * - LLM provider settings
 * - Workflow configuration
 * - Storage paths and capacity
 * - Error notification settings
 * - Circuit breaker parameters
 */
@Component
public class ConfigurationValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);
    
    // LinkedIn API Configuration
    @Value("${linkedin.api.client-id:#{null}}")
    private String linkedInClientId;
    
    @Value("${linkedin.api.client-secret:#{null}}")
    private String linkedInClientSecret;
    
    @Value("${linkedin.api.access-token:#{null}}")
    private String linkedInAccessToken;
    
    @Value("${linkedin.api.refresh-token:#{null}}")
    private String linkedInRefreshToken;
    
    @Value("${linkedin.api.base-url:https://api.linkedin.com/v2}")
    private String linkedInBaseUrl;
    
    @Value("${linkedin.api.rate-limit-per-minute:100}")
    private int rateLimitPerMinute;
    
    // LLM Configuration
    @Value("${llm.provider:openai}")
    private String llmProvider;
    
    @Value("${llm.api-key:#{null}}")
    private String llmApiKey;
    
    @Value("${llm.model:gpt-4}")
    private String llmModel;
    
    @Value("${llm.temperature:0.7}")
    private double llmTemperature;
    
    @Value("${llm.max-tokens:500}")
    private int llmMaxTokens;
    
    // Workflow Configuration
    @Value("${workflow.polling-interval-seconds:300}")
    private int pollingIntervalSeconds;
    
    @Value("${workflow.require-manual-approval:false}")
    private boolean requireManualApproval;
    
    @Value("${workflow.tone-preference:witty}")
    private String tonePreference;
    
    @Value("${workflow.max-retries:3}")
    private int maxRetries;
    
    @Value("${workflow.retry-backoff-seconds:2}")
    private int retryBackoffSeconds;
    
    @Value("${workflow.manual-review-keywords:}")
    private String manualReviewKeywords;
    
    // Storage Configuration
    @Value("${storage.directory:./data}")
    private String storageDirectory;
    
    @Value("${storage.interactions.file:interactions.json}")
    private String interactionsFile;
    
    @Value("${storage.processed.file:processed-comments.json}")
    private String processedFile;
    
    @Value("${storage.max.capacity:1000}")
    private int maxCapacity;
    
    @Value("${storage.archive.directory:./data/archive}")
    private String archiveDirectory;
    
    // Error Notification Configuration
    @Value("${error.notification.enabled:false}")
    private boolean notificationsEnabled;
    
    @Value("${error.notification.channel:log}")
    private String notificationChannel;
    
    @Value("${error.notification.email:#{null}}")
    private String notificationEmail;
    
    @Value("${error.notification.slack-webhook:#{null}}")
    private String slackWebhook;
    
    // Circuit Breaker Configuration
    @Value("${circuit-breaker.failure-threshold:5}")
    private int circuitBreakerFailureThreshold;
    
    @Value("${circuit-breaker.reset-timeout-ms:60000}")
    private long circuitBreakerResetTimeout;
    
    @Value("${circuit-breaker.success-threshold:3}")
    private int circuitBreakerSuccessThreshold;
    
    /**
     * Validates configuration on application startup.
     * Throws exception if required configuration is missing or invalid.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        logger.info("Validating application configuration...");
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validate LinkedIn API configuration
        validateLinkedInConfig(errors, warnings);
        
        // Validate LLM configuration
        validateLLMConfig(errors, warnings);
        
        // Validate workflow configuration
        validateWorkflowConfig(errors, warnings);
        
        // Validate storage configuration
        validateStorageConfig(errors, warnings);
        
        // Validate notification configuration
        validateNotificationConfig(errors, warnings);
        
        // Validate circuit breaker configuration
        validateCircuitBreakerConfig(errors, warnings);
        
        // Log warnings
        if (!warnings.isEmpty()) {
            logger.warn("Configuration warnings:");
            warnings.forEach(warning -> logger.warn("  - {}", warning));
        }
        
        // Fail if there are errors
        if (!errors.isEmpty()) {
            logger.error("Configuration validation failed:");
            errors.forEach(error -> logger.error("  - {}", error));
            
            throw new IllegalStateException(
                "Configuration validation failed. Missing or invalid parameters: " + 
                String.join(", ", errors)
            );
        }
        
        logger.info("Configuration validation passed");
        logConfigurationSummary();
    }
    
    /**
     * Validates LinkedIn API configuration.
     */
    private void validateLinkedInConfig(List<String> errors, List<String> warnings) {
        if (isPlaceholderValue(linkedInClientId)) {
            errors.add("LinkedIn Client ID is required (linkedin.api.client-id). Set via LINKEDIN_CLIENT_ID environment variable.");
        }
        
        if (isPlaceholderValue(linkedInClientSecret)) {
            errors.add("LinkedIn Client Secret is required (linkedin.api.client-secret). Set via LINKEDIN_CLIENT_SECRET environment variable.");
        }
        
        if (isPlaceholderValue(linkedInAccessToken)) {
            errors.add("LinkedIn Access Token is required (linkedin.api.access-token). Set via LINKEDIN_ACCESS_TOKEN environment variable.");
        }
        
        if (linkedInBaseUrl == null || linkedInBaseUrl.trim().isEmpty()) {
            errors.add("LinkedIn API base URL is required (linkedin.api.base-url)");
        } else if (!linkedInBaseUrl.startsWith("https://")) {
            warnings.add("LinkedIn API base URL should use HTTPS");
        }
        
        if (rateLimitPerMinute <= 0) {
            errors.add("Rate limit must be positive (linkedin.api.rate-limit-per-minute)");
        } else if (rateLimitPerMinute < 10) {
            warnings.add("Rate limit is very low (" + rateLimitPerMinute + "/min), may impact performance");
        }
    }
    
    /**
     * Validates LLM configuration.
     */
    private void validateLLMConfig(List<String> errors, List<String> warnings) {
        if (isPlaceholderValue(llmApiKey)) {
            errors.add("LLM API key is required (llm.api-key). Set via OPENAI_API_KEY environment variable.");
        }
        
        List<String> validProviders = Arrays.asList("openai", "anthropic", "bedrock");
        if (!validProviders.contains(llmProvider.toLowerCase())) {
            warnings.add("Unknown LLM provider: " + llmProvider + ". Supported: " + String.join(", ", validProviders));
        }
        
        if (llmModel == null || llmModel.trim().isEmpty()) {
            warnings.add("LLM model not specified, using default: gpt-4");
        }
        
        if (llmTemperature < 0.0 || llmTemperature > 2.0) {
            warnings.add("LLM temperature should be between 0.0 and 2.0 (current: " + llmTemperature + ")");
        }
        
        if (llmMaxTokens <= 0) {
            errors.add("LLM max tokens must be positive (llm.max-tokens)");
        } else if (llmMaxTokens < 100) {
            warnings.add("LLM max tokens is very low (" + llmMaxTokens + "), responses may be truncated");
        }
    }
    
    /**
     * Validates workflow configuration.
     */
    private void validateWorkflowConfig(List<String> errors, List<String> warnings) {
        if (pollingIntervalSeconds <= 0) {
            errors.add("Polling interval must be positive (workflow.polling-interval-seconds)");
        } else if (pollingIntervalSeconds < 30) {
            warnings.add("Polling interval is very short (" + pollingIntervalSeconds + "s), may cause rate limiting");
        } else if (pollingIntervalSeconds > 3600) {
            warnings.add("Polling interval is very long (" + pollingIntervalSeconds + "s), responses may be delayed");
        }
        
        List<String> validTones = Arrays.asList("witty", "sarcastic", "wholesome", "professional", "casual");
        if (!validTones.contains(tonePreference.toLowerCase())) {
            warnings.add("Unknown tone preference: " + tonePreference + ". Suggested: " + String.join(", ", validTones));
        }
        
        if (maxRetries < 0) {
            errors.add("Max retries cannot be negative (workflow.max-retries)");
        } else if (maxRetries > 10) {
            warnings.add("Max retries is very high (" + maxRetries + "), may cause long delays");
        }
        
        if (retryBackoffSeconds <= 0) {
            errors.add("Retry backoff must be positive (workflow.retry-backoff-seconds)");
        }
        
        if (manualReviewKeywords != null && !manualReviewKeywords.trim().isEmpty()) {
            logger.info("Manual review keywords configured: {}", manualReviewKeywords);
        }
    }
    
    /**
     * Validates storage configuration.
     */
    private void validateStorageConfig(List<String> errors, List<String> warnings) {
        if (storageDirectory == null || storageDirectory.trim().isEmpty()) {
            errors.add("Storage directory is required (storage.directory)");
        } else {
            File dir = new File(storageDirectory);
            if (!dir.exists()) {
                warnings.add("Storage directory does not exist, will be created: " + storageDirectory);
            } else if (!dir.isDirectory()) {
                errors.add("Storage path exists but is not a directory: " + storageDirectory);
            } else if (!dir.canWrite()) {
                errors.add("Storage directory is not writable: " + storageDirectory);
            }
        }
        
        if (interactionsFile == null || interactionsFile.trim().isEmpty()) {
            errors.add("Interactions file name is required (storage.interactions.file)");
        }
        
        if (processedFile == null || processedFile.trim().isEmpty()) {
            errors.add("Processed comments file name is required (storage.processed.file)");
        }
        
        if (maxCapacity <= 0) {
            errors.add("Storage max capacity must be positive (storage.max.capacity)");
        } else if (maxCapacity < 100) {
            warnings.add("Storage capacity is very low (" + maxCapacity + "), may archive frequently");
        }
        
        if (archiveDirectory == null || archiveDirectory.trim().isEmpty()) {
            errors.add("Archive directory is required (storage.archive.directory)");
        }
    }
    
    /**
     * Validates notification configuration.
     */
    private void validateNotificationConfig(List<String> errors, List<String> warnings) {
        if (notificationsEnabled) {
            if (!isValidNotificationChannel(notificationChannel)) {
                warnings.add("Unknown notification channel: " + notificationChannel + 
                           ". Supported: log, email, slack");
            }
            
            if ("email".equalsIgnoreCase(notificationChannel)) {
                if (isPlaceholderValue(notificationEmail)) {
                    errors.add("Email notification enabled but email address not configured (error.notification.email)");
                } else if (!isValidEmail(notificationEmail)) {
                    warnings.add("Email address may be invalid: " + notificationEmail);
                }
            }
            
            if ("slack".equalsIgnoreCase(notificationChannel)) {
                if (isPlaceholderValue(slackWebhook)) {
                    errors.add("Slack notification enabled but webhook URL not configured (error.notification.slack-webhook)");
                } else if (!slackWebhook.startsWith("https://hooks.slack.com/")) {
                    warnings.add("Slack webhook URL format may be invalid");
                }
            }
        }
    }
    
    /**
     * Validates circuit breaker configuration.
     */
    private void validateCircuitBreakerConfig(List<String> errors, List<String> warnings) {
        if (circuitBreakerFailureThreshold <= 0) {
            errors.add("Circuit breaker failure threshold must be positive (circuit-breaker.failure-threshold)");
        } else if (circuitBreakerFailureThreshold < 3) {
            warnings.add("Circuit breaker failure threshold is very low (" + circuitBreakerFailureThreshold + "), may trip frequently");
        }
        
        if (circuitBreakerResetTimeout <= 0) {
            errors.add("Circuit breaker reset timeout must be positive (circuit-breaker.reset-timeout-ms)");
        } else if (circuitBreakerResetTimeout < 10000) {
            warnings.add("Circuit breaker reset timeout is very short (" + circuitBreakerResetTimeout + "ms)");
        }
        
        if (circuitBreakerSuccessThreshold <= 0) {
            errors.add("Circuit breaker success threshold must be positive (circuit-breaker.success-threshold)");
        }
    }
    
    /**
     * Checks if a value is a placeholder (null, empty, or default placeholder text).
     */
    private boolean isPlaceholderValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        String lower = value.toLowerCase();
        return lower.startsWith("your-") || 
               lower.equals("placeholder") || 
               lower.equals("changeme") ||
               lower.equals("todo");
    }
    
    /**
     * Checks if the notification channel is valid.
     */
    private boolean isValidNotificationChannel(String channel) {
        return channel != null && 
               (channel.equalsIgnoreCase("log") || 
                channel.equalsIgnoreCase("email") || 
                channel.equalsIgnoreCase("slack"));
    }
    
    /**
     * Basic email validation.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Logs a summary of the current configuration.
     */
    private void logConfigurationSummary() {
        logger.info("=== Configuration Summary ===");
        logger.info("LinkedIn API Base URL: {}", linkedInBaseUrl);
        logger.info("LinkedIn Client ID: {}***", maskSecret(linkedInClientId));
        logger.info("Rate Limit: {}/min", rateLimitPerMinute);
        logger.info("");
        logger.info("LLM Provider: {}", llmProvider);
        logger.info("LLM Model: {}", llmModel);
        logger.info("LLM API Key: {}***", maskSecret(llmApiKey));
        logger.info("LLM Temperature: {}", llmTemperature);
        logger.info("LLM Max Tokens: {}", llmMaxTokens);
        logger.info("");
        logger.info("Polling Interval: {}s", pollingIntervalSeconds);
        logger.info("Manual Approval Required: {}", requireManualApproval);
        logger.info("Tone Preference: {}", tonePreference);
        logger.info("Max Retries: {}", maxRetries);
        logger.info("Retry Backoff: {}s", retryBackoffSeconds);
        if (manualReviewKeywords != null && !manualReviewKeywords.trim().isEmpty()) {
            logger.info("Manual Review Keywords: {}", manualReviewKeywords);
        }
        logger.info("");
        logger.info("Storage Directory: {}", storageDirectory);
        logger.info("Max Capacity: {}", maxCapacity);
        logger.info("Archive Directory: {}", archiveDirectory);
        logger.info("");
        logger.info("Error Notifications Enabled: {}", notificationsEnabled);
        if (notificationsEnabled) {
            logger.info("Notification Channel: {}", notificationChannel);
        }
        logger.info("");
        logger.info("Circuit Breaker Failure Threshold: {}", circuitBreakerFailureThreshold);
        logger.info("Circuit Breaker Reset Timeout: {}ms", circuitBreakerResetTimeout);
        logger.info("Circuit Breaker Success Threshold: {}", circuitBreakerSuccessThreshold);
        logger.info("============================");
    }
    
    /**
     * Masks a secret value for logging.
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() < 4) {
            return "****";
        }
        return secret.substring(0, Math.min(4, secret.length()));
    }
}
