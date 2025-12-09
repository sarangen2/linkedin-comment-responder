package com.example.linkedin.agent;

import com.example.linkedin.model.Comment;
import com.example.linkedin.model.GeneratedResponse;
import com.example.linkedin.model.Post;

/**
 * Interface for LLM-based response generation.
 * Handles generating contextual, comedic responses to comments.
 */
public interface LLMAgent {
    
    /**
     * Generates a response to a comment based on the original post context.
     * @param post The original post
     * @param comment The comment to respond to
     * @param tonePreference The desired tone (e.g., "witty", "sarcastic", "wholesome")
     * @return Generated response with metadata
     */
    GeneratedResponse generateResponse(Post post, Comment comment, String tonePreference);
    
    /**
     * Analyzes the theme and tone of a post.
     * @param post The post to analyze
     * @return Theme description
     */
    String analyzePostTheme(Post post);
    
    /**
     * Validates a generated response for quality and appropriateness.
     * @param response The response to validate
     * @return true if valid, false otherwise
     */
    boolean validateResponse(String response);
}
