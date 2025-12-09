package com.example.linkedin.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for sending error notifications via configured channels.
 * Supports immediate notifications for critical errors and batched notifications for warnings.
 */
@Service
public class ErrorNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorNotificationService.class);
    
    private final String notificationChannel;
    private final boolean notificationsEnabled;
    private final ConcurrentLinkedQueue<CategorizedError> warningQueue;
    private final ScheduledExecutorService scheduler;
    
    public ErrorNotificationService(
            @Value("${error.notification.channel:log}") String notificationChannel,
            @Value("${error.notification.enabled:false}") boolean notificationsEnabled) {
        this.notificationChannel = notificationChannel;
        this.notificationsEnabled = notificationsEnabled;
        this.warningQueue = new ConcurrentLinkedQueue<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Schedule batched warning notifications every hour
        scheduler.scheduleAtFixedRate(this::sendBatchedWarnings, 1, 1, TimeUnit.HOURS);
        
        logger.info("Error notification service initialized. Channel: {}, Enabled: {}", 
                   notificationChannel, notificationsEnabled);
    }
    
    /**
     * Sends an immediate notification for critical errors.
     */
    public void sendCriticalErrorNotification(CategorizedError error) {
        if (!notificationsEnabled) {
            logger.debug("Notifications disabled, skipping critical error notification");
            return;
        }
        
        logger.info("Sending critical error notification for correlation ID: {}", 
                   error.getCorrelationId());
        
        String notification = formatErrorNotification(error);
        
        switch (notificationChannel.toLowerCase()) {
            case "email":
                sendEmailNotification(notification, true);
                break;
            case "slack":
                sendSlackNotification(notification, true);
                break;
            case "log":
            default:
                logger.error("CRITICAL ERROR NOTIFICATION: {}", notification);
                break;
        }
    }
    
    /**
     * Queues a warning-level error for batched notification.
     */
    public void queueWarningNotification(CategorizedError error) {
        if (!notificationsEnabled) {
            return;
        }
        
        warningQueue.offer(error);
        logger.debug("Queued warning notification for correlation ID: {}", error.getCorrelationId());
    }
    
    /**
     * Sends batched warning notifications.
     */
    private void sendBatchedWarnings() {
        if (warningQueue.isEmpty()) {
            return;
        }
        
        logger.info("Sending batched warning notifications for {} errors", warningQueue.size());
        
        List<CategorizedError> warnings = new ArrayList<>();
        CategorizedError error;
        while ((error = warningQueue.poll()) != null) {
            warnings.add(error);
        }
        
        String notification = formatBatchedWarnings(warnings);
        
        switch (notificationChannel.toLowerCase()) {
            case "email":
                sendEmailNotification(notification, false);
                break;
            case "slack":
                sendSlackNotification(notification, false);
                break;
            case "log":
            default:
                logger.warn("BATCHED WARNING NOTIFICATIONS: {}", notification);
                break;
        }
    }
    
    /**
     * Formats a single error notification.
     */
    private String formatErrorNotification(CategorizedError error) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CRITICAL ERROR ===\n");
        sb.append("Correlation ID: ").append(error.getCorrelationId()).append("\n");
        sb.append("Category: ").append(error.getCategory()).append("\n");
        sb.append("Timestamp: ").append(error.getTimestamp()).append("\n");
        sb.append("Message: ").append(error.getMessage()).append("\n");
        
        if (!error.getContext().isEmpty()) {
            sb.append("Context: ").append(error.getContext()).append("\n");
        }
        
        if (error.getCause() != null) {
            sb.append("Exception: ").append(error.getCause().getClass().getName()).append("\n");
        }
        
        sb.append("=====================");
        
        return sb.toString();
    }
    
    /**
     * Formats batched warning notifications.
     */
    private String formatBatchedWarnings(List<CategorizedError> warnings) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BATCHED WARNING NOTIFICATIONS ===\n");
        sb.append("Total Warnings: ").append(warnings.size()).append("\n");
        sb.append("Period: Last hour\n\n");
        
        for (CategorizedError warning : warnings) {
            sb.append("- [").append(warning.getCategory()).append("] ");
            sb.append(warning.getMessage());
            sb.append(" (").append(warning.getCorrelationId()).append(")\n");
        }
        
        sb.append("=====================================");
        
        return sb.toString();
    }
    
    /**
     * Sends an email notification (placeholder implementation).
     */
    private void sendEmailNotification(String message, boolean isCritical) {
        // Placeholder for email integration
        // In production, integrate with email service (e.g., AWS SES, SendGrid)
        logger.info("EMAIL NOTIFICATION ({}): {}", 
                   isCritical ? "CRITICAL" : "WARNING", message);
    }
    
    /**
     * Sends a Slack notification (placeholder implementation).
     */
    private void sendSlackNotification(String message, boolean isCritical) {
        // Placeholder for Slack integration
        // In production, integrate with Slack webhook API
        logger.info("SLACK NOTIFICATION ({}): {}", 
                   isCritical ? "CRITICAL" : "WARNING", message);
    }
    
    /**
     * Shuts down the notification service.
     */
    public void shutdown() {
        logger.info("Shutting down error notification service");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
