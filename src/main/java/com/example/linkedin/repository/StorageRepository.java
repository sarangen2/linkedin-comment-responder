package com.example.linkedin.repository;

import com.example.linkedin.model.Interaction;
import java.time.Instant;
import java.util.List;

/**
 * Interface for storing and retrieving interaction history.
 */
public interface StorageRepository {
    
    /**
     * Saves an interaction to storage.
     * @param interaction The interaction to save
     */
    void saveInteraction(Interaction interaction);
    
    /**
     * Checks if a comment has been processed.
     * @param commentId The comment identifier
     * @return true if processed, false otherwise
     */
    boolean isCommentProcessed(String commentId);
    
    /**
     * Marks a comment as processed.
     * @param commentId The comment identifier
     */
    void markCommentProcessed(String commentId);
    
    /**
     * Retrieves interaction history with optional filtering.
     * @param postId Optional post ID filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of interactions matching the filters
     */
    List<Interaction> getInteractionHistory(String postId, Instant startDate, Instant endDate);
    
    /**
     * Exports interaction history to a file.
     * @param format The export format ("json" or "csv")
     * @return Path to the exported file
     */
    String exportHistory(String format);
}
