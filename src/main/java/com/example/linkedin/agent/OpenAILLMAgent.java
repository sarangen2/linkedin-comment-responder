package com.example.linkedin.agent;

import com.example.linkedin.model.Comment;
import com.example.linkedin.model.GeneratedResponse;
import com.example.linkedin.model.Post;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI-based implementation of the LLM Agent.
 * Generates contextual, comedic responses to LinkedIn comments.
 */
@Component
public class OpenAILLMAgent implements LLMAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAILLMAgent.class);
    private static final int MAX_RESPONSE_LENGTH = 1250;
    
    private final OpenAiService openAiService;
    private final String primaryModel;
    private final String fallbackModel;
    private final double temperature;
    private final int maxTokens;
    
    public OpenAILLMAgent(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.model:gpt-4}") String primaryModel,
            @Value("${llm.temperature:0.7}") double temperature,
            @Value("${llm.max-tokens:500}") int maxTokens) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        this.primaryModel = primaryModel;
        this.fallbackModel = "gpt-3.5-turbo"; // Simpler fallback model
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        
        logger.info("Initialized OpenAI LLM Agent with model: {}, temperature: {}", 
                    primaryModel, temperature);
    }
    
    @Override
    public GeneratedResponse generateResponse(Post post, Comment comment, String tonePreference) {
        logger.info("Generating response for comment {} on post {}", comment.getId(), post.getId());
        
        try {
            // Analyze post theme first
            String theme = analyzePostTheme(post);
            
            // Build the prompt
            String prompt = buildPrompt(post, comment, tonePreference, theme);
            
            // Try with primary model
            String responseText = callLLM(prompt, primaryModel);
            
            // Validate the response
            if (!validateResponse(responseText)) {
                logger.warn("Generated response failed validation, retrying...");
                responseText = callLLM(prompt, primaryModel);
            }
            
            // Create response object
            GeneratedResponse response = new GeneratedResponse(
                responseText,
                0.85, // Confidence score - could be enhanced with actual scoring
                "Generated using " + primaryModel + " with theme: " + theme
            );
            
            // Add warnings if needed
            if (responseText.length() > MAX_RESPONSE_LENGTH * 0.9) {
                response.addWarning("Response is close to maximum length");
            }
            
            if (containsSensitiveKeywords(comment.getText())) {
                response.addWarning("Comment contains potentially sensitive keywords");
            }
            
            logger.info("Successfully generated response with {} characters", responseText.length());
            return response;
            
        } catch (Exception e) {
            logger.error("Error generating response with primary model, trying fallback", e);
            return generateResponseWithFallback(post, comment, tonePreference);
        }
    }
    
    @Override
    public String analyzePostTheme(Post post) {
        logger.debug("Analyzing theme for post {}", post.getId());
        
        try {
            String analysisPrompt = buildThemeAnalysisPrompt(post);
            String theme = callLLM(analysisPrompt, primaryModel);
            
            logger.debug("Identified theme: {}", theme);
            return theme;
            
        } catch (Exception e) {
            logger.error("Error analyzing post theme", e);
            return "general discussion"; // Default theme
        }
    }
    
    @Override
    public boolean validateResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            logger.warn("Validation failed: Response is null or empty");
            return false;
        }
        
        if (response.length() > MAX_RESPONSE_LENGTH) {
            logger.warn("Validation failed: Response exceeds maximum length of {} characters", 
                       MAX_RESPONSE_LENGTH);
            return false;
        }
        
        // Check for minimum content
        if (response.length() < 10) {
            logger.warn("Validation failed: Response is too short");
            return false;
        }
        
        // Check for inappropriate content markers (basic check)
        String lowerResponse = response.toLowerCase();
        if (lowerResponse.contains("[error]") || 
            lowerResponse.contains("[failed]") ||
            lowerResponse.startsWith("i cannot") ||
            lowerResponse.startsWith("i can't")) {
            logger.warn("Validation failed: Response contains error markers or refusal");
            return false;
        }
        
        logger.debug("Response validation passed");
        return true;
    }
    
    /**
     * Generates response using fallback model when primary model fails.
     */
    private GeneratedResponse generateResponseWithFallback(Post post, Comment comment, String tonePreference) {
        logger.info("Attempting response generation with fallback model: {}", fallbackModel);
        
        try {
            String theme = "general"; // Simplified theme for fallback
            String prompt = buildPrompt(post, comment, tonePreference, theme);
            String responseText = callLLM(prompt, fallbackModel);
            
            GeneratedResponse response = new GeneratedResponse(
                responseText,
                0.65, // Lower confidence for fallback
                "Generated using fallback model " + fallbackModel
            );
            response.addWarning("Primary model unavailable, used fallback model");
            
            return response;
            
        } catch (Exception e) {
            logger.error("Fallback model also failed", e);
            // Return a template-based response as last resort
            return createTemplateResponse(comment);
        }
    }
    
    /**
     * Calls the LLM API with the given prompt and model.
     */
    private String callLLM(String prompt, String model) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), getSystemPrompt()));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(model)
            .messages(messages)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();
        
        ChatCompletionResult result = openAiService.createChatCompletion(request);
        
        if (result.getChoices() == null || result.getChoices().isEmpty()) {
            throw new RuntimeException("No response from LLM");
        }
        
        return result.getChoices().get(0).getMessage().getContent().trim();
    }
    
    /**
     * Builds the system prompt for the LLM.
     */
    private String getSystemPrompt() {
        return "You are a witty LinkedIn engagement assistant. Your task is to generate " +
               "responses to comments that:\n" +
               "1. Acknowledge the commenter's specific point\n" +
               "2. Add comedic value aligned with the post's theme\n" +
               "3. Match the specified tone\n" +
               "4. Stay under 1250 characters\n" +
               "5. Remain professional yet entertaining\n\n" +
               "Generate only the response text, without any meta-commentary or explanations.";
    }
    
    /**
     * Builds the user prompt for response generation.
     */
    private String buildPrompt(Post post, Comment comment, String tonePreference, String theme) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Original Post: ").append(post.getContent()).append("\n\n");
        
        // Add media context if available
        if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            prompt.append("Post includes media: ").append(post.getMediaUrls().size())
                  .append(" item(s)\n\n");
        }
        
        // Add metadata context if available
        if (post.getMetadata() != null && !post.getMetadata().isEmpty()) {
            prompt.append("Post metadata: ").append(post.getMetadata()).append("\n\n");
        }
        
        prompt.append("Post Theme: ").append(theme).append("\n\n");
        prompt.append("Desired Tone: ").append(tonePreference).append("\n\n");
        prompt.append("Comment from ").append(comment.getAuthorName()).append(": ")
              .append(comment.getText()).append("\n\n");
        prompt.append("Generate an appropriate response:");
        
        return prompt.toString();
    }
    
    /**
     * Builds the prompt for theme analysis.
     */
    private String buildThemeAnalysisPrompt(Post post) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze the following LinkedIn post and identify its main theme and tone.\n\n");
        prompt.append("Post: ").append(post.getContent()).append("\n\n");
        
        if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            prompt.append("The post includes ").append(post.getMediaUrls().size())
                  .append(" media item(s).\n\n");
        }
        
        prompt.append("Provide a brief description of the theme and tone (1-2 sentences):");
        
        return prompt.toString();
    }
    
    /**
     * Creates a template-based response as a last resort.
     */
    private GeneratedResponse createTemplateResponse(Comment comment) {
        String templateResponse = String.format(
            "Thanks for your comment, %s! I appreciate you taking the time to share your thoughts.",
            comment.getAuthorName()
        );
        
        GeneratedResponse response = new GeneratedResponse(
            templateResponse,
            0.3, // Very low confidence
            "Template-based fallback response"
        );
        response.addWarning("LLM unavailable, using template response");
        
        return response;
    }
    
    /**
     * Checks if the text contains sensitive keywords.
     */
    private boolean containsSensitiveKeywords(String text) {
        String lowerText = text.toLowerCase();
        String[] sensitiveKeywords = {
            "urgent", "complaint", "refund", "legal", "lawsuit", 
            "discrimination", "harassment", "offensive"
        };
        
        for (String keyword : sensitiveKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
}
