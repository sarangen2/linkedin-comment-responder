package com.example.linkedin.client;

import com.example.linkedin.model.Comment;
import com.example.linkedin.model.LinkedInCredentials;
import com.example.linkedin.model.Post;
import com.example.linkedin.model.PostResult;
import com.example.linkedin.resilience.CircuitBreaker;
import com.example.linkedin.resilience.CircuitBreakerRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client for interacting with LinkedIn API.
 * Implements OAuth 2.0 authentication, rate limiting, retry logic, and error handling.
 */
@Component
public class LinkedInApiClient {
    private static final Logger logger = LoggerFactory.getLogger(LinkedInApiClient.class);
    
    private static final String LINKEDIN_API_BASE_URL = "https://api.linkedin.com/v2";
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;
    
    // Token bucket rate limiter
    private final TokenBucket rateLimiter;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final CircuitBreaker circuitBreaker;
    private LinkedInCredentials credentials;

    public LinkedInApiClient(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.webClient = WebClient.builder()
                .baseUrl(LINKEDIN_API_BASE_URL)
                .build();
        this.objectMapper = new ObjectMapper();
        this.rateLimiter = new TokenBucket(100, 60); // 100 requests per 60 seconds
        this.circuitBreaker = circuitBreakerRegistry.getOrCreate("linkedin-api");
    }

    /**
     * Authenticate with LinkedIn using OAuth 2.0.
     * 
     * @param credentials LinkedIn API credentials
     * @return Access token
     */
    public String authenticate(LinkedInCredentials credentials) {
        logger.info("Authenticating with LinkedIn API");
        this.credentials = credentials;
        
        // If token is valid, return it
        if (credentials.getAccessToken() != null && !credentials.isTokenExpired()) {
            logger.debug("Using existing valid access token");
            return credentials.getAccessToken();
        }
        
        // If we have a refresh token, use it to get a new access token
        if (credentials.getRefreshToken() != null) {
            logger.info("Refreshing access token");
            return refreshAccessToken(credentials);
        }
        
        // Otherwise, return the provided access token (assuming it was obtained externally)
        logger.warn("No refresh token available, using provided access token");
        return credentials.getAccessToken();
    }

    /**
     * Refresh the access token using the refresh token.
     */
    private String refreshAccessToken(LinkedInCredentials credentials) {
        logger.info("Refreshing LinkedIn access token");
        
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("grant_type", "refresh_token");
            requestBody.put("refresh_token", credentials.getRefreshToken());
            requestBody.put("client_id", credentials.getClientId());
            requestBody.put("client_secret", credentials.getClientSecret());
            
            String response = webClient.post()
                    .uri("https://www.linkedin.com/oauth/v2/accessToken")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode jsonNode = objectMapper.readTree(response);
            String newAccessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            
            credentials.setAccessToken(newAccessToken);
            credentials.setTokenExpiry(Instant.now().plusSeconds(expiresIn));
            
            logger.info("Access token refreshed successfully");
            return newAccessToken;
            
        } catch (Exception e) {
            logger.error("Failed to refresh access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    /**
     * Fetch all comments for a given post.
     * 
     * @param postId LinkedIn post ID
     * @return List of comments
     */
    public List<Comment> fetchComments(String postId) {
        logger.info("Fetching comments for post: {}", postId);
        
        // Wait for rate limiter
        rateLimiter.acquire();
        
        try {
            // Use circuit breaker for external API call
            String response = circuitBreaker.execute(() -> 
                executeWithRetry(() -> 
                    webClient.get()
                        .uri("/socialActions/{postId}/comments", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getValidAccessToken())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
                )
            );
            
            logger.debug("Received response: {}", response);
            
            List<Comment> comments = parseComments(response, postId);
            logger.info("Fetched {} comments for post {}", comments.size(), postId);
            
            return comments;
            
        } catch (CircuitBreaker.CircuitBreakerOpenException e) {
            logger.error("Circuit breaker is open for LinkedIn API", e);
            throw new RuntimeException("LinkedIn API is currently unavailable", e);
        } catch (WebClientResponseException e) {
            handleApiError(e);
            throw new RuntimeException("Failed to fetch comments", e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching comments", e);
            throw new RuntimeException("Failed to fetch comments", e);
        }
    }

    /**
     * Fetch post details.
     * 
     * @param postId LinkedIn post ID
     * @return Post object
     */
    public Post fetchPost(String postId) {
        logger.info("Fetching post: {}", postId);
        
        // Wait for rate limiter
        rateLimiter.acquire();
        
        try {
            // Use circuit breaker for external API call
            String response = circuitBreaker.execute(() ->
                executeWithRetry(() ->
                    webClient.get()
                        .uri("/ugcPosts/{postId}", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getValidAccessToken())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
                )
            );
            
            logger.debug("Received response: {}", response);
            
            Post post = parsePost(response);
            logger.info("Fetched post {}", postId);
            
            return post;
            
        } catch (CircuitBreaker.CircuitBreakerOpenException e) {
            logger.error("Circuit breaker is open for LinkedIn API", e);
            throw new RuntimeException("LinkedIn API is currently unavailable", e);
        } catch (WebClientResponseException e) {
            handleApiError(e);
            throw new RuntimeException("Failed to fetch post", e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching post", e);
            throw new RuntimeException("Failed to fetch post", e);
        }
    }

    /**
     * Post a reply to a comment.
     * 
     * @param commentId Comment ID to reply to
     * @param responseText Response text
     * @return PostResult indicating success or failure
     */
    public PostResult postReply(String commentId, String responseText) {
        logger.info("Posting reply to comment: {}", commentId);
        
        // Wait for rate limiter
        rateLimiter.acquire();
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("actor", "urn:li:person:" + credentials.getClientId());
            requestBody.put("object", commentId);
            
            Map<String, String> message = new HashMap<>();
            message.put("text", responseText);
            requestBody.put("message", message);
            
            String response = executeWithRetry(() ->
                webClient.post()
                    .uri("/socialActions/{commentId}/comments", commentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getValidAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block()
            );
            
            logger.info("Successfully posted reply to comment {}", commentId);
            logger.debug("Response: {}", response);
            
            // Extract response ID from the response
            JsonNode jsonNode = objectMapper.readTree(response);
            String responseId = jsonNode.has("id") ? jsonNode.get("id").asText() : null;
            
            return new PostResult(true, responseId);
            
        } catch (WebClientResponseException e) {
            logger.error("Failed to post reply to comment {}: {} - {}", 
                    commentId, e.getStatusCode(), e.getMessage());
            handleApiError(e);
            return new PostResult(false, e.getMessage(), e.getStatusCode().value());
        } catch (Exception e) {
            logger.error("Failed to post reply to comment {}", commentId, e);
            return new PostResult(false, e.getMessage(), 500);
        }
    }

    /**
     * Execute an operation with retry logic and exponential backoff.
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation) {
        int attempt = 0;
        long backoffMs = INITIAL_BACKOFF_MS;
        
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (WebClientResponseException e) {
                attempt++;
                
                // Don't retry on client errors (except 429)
                if (e.getStatusCode().is4xxClientError() && 
                    e.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
                    throw e;
                }
                
                if (attempt >= MAX_RETRIES) {
                    logger.error("Max retries ({}) exceeded", MAX_RETRIES);
                    throw e;
                }
                
                logger.warn("Request failed (attempt {}/{}): {} - {}. Retrying in {}ms", 
                        attempt, MAX_RETRIES, e.getStatusCode(), e.getMessage(), backoffMs);
                
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during backoff", ie);
                }
                
                // Exponential backoff with jitter
                backoffMs = (long) (backoffMs * 2 * (0.5 + Math.random() * 0.5));
            }
        }
        
        throw new RuntimeException("Failed after " + MAX_RETRIES + " retries");
    }

    /**
     * Get a valid access token, refreshing if necessary.
     */
    private String getValidAccessToken() {
        if (credentials == null || credentials.getAccessToken() == null) {
            throw new IllegalStateException("Not authenticated. Call authenticate() first.");
        }
        
        if (credentials.isTokenExpired() && credentials.getRefreshToken() != null) {
            return refreshAccessToken(credentials);
        }
        
        return credentials.getAccessToken();
    }

    /**
     * Handle API errors based on status code.
     */
    private void handleApiError(WebClientResponseException e) {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        
        switch (status) {
            case UNAUTHORIZED:
                logger.error("Authentication failed (401): Invalid or expired access token");
                break;
            case FORBIDDEN:
                logger.error("Access forbidden (403): Insufficient permissions");
                break;
            case NOT_FOUND:
                logger.error("Resource not found (404)");
                break;
            case TOO_MANY_REQUESTS:
                logger.warn("Rate limit exceeded (429). Backing off...");
                rateLimiter.handleRateLimit();
                break;
            case INTERNAL_SERVER_ERROR:
            case BAD_GATEWAY:
            case SERVICE_UNAVAILABLE:
                logger.error("LinkedIn API server error ({}): {}", status.value(), e.getMessage());
                break;
            default:
                logger.error("API error ({}): {}", status.value(), e.getMessage());
        }
    }

    /**
     * Parse comments from LinkedIn API response.
     */
    private List<Comment> parseComments(String response, String postId) {
        List<Comment> comments = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode elements = root.get("elements");
            
            if (elements != null && elements.isArray()) {
                for (JsonNode element : elements) {
                    Comment comment = new Comment();
                    comment.setId(element.get("id").asText());
                    comment.setPostId(postId);
                    
                    JsonNode actor = element.get("actor");
                    if (actor != null) {
                        comment.setAuthorId(actor.asText());
                        // In real implementation, we'd fetch the author name separately
                        comment.setAuthorName("User " + actor.asText().substring(0, 8));
                    }
                    
                    JsonNode message = element.get("message");
                    if (message != null && message.has("text")) {
                        comment.setText(message.get("text").asText());
                    }
                    
                    JsonNode created = element.get("created");
                    if (created != null && created.has("time")) {
                        long timestamp = created.get("time").asLong();
                        comment.setTimestamp(Instant.ofEpochMilli(timestamp));
                    }
                    
                    comments.add(comment);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse comments", e);
            throw new RuntimeException("Failed to parse comments", e);
        }
        
        return comments;
    }

    /**
     * Parse post from LinkedIn API response.
     */
    private Post parsePost(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            Post post = new Post();
            post.setId(root.get("id").asText());
            
            JsonNode author = root.get("author");
            if (author != null) {
                post.setAuthorId(author.asText());
            }
            
            JsonNode specificContent = root.get("specificContent");
            if (specificContent != null) {
                JsonNode shareContent = specificContent.get("com.linkedin.ugc.ShareContent");
                if (shareContent != null) {
                    JsonNode shareCommentary = shareContent.get("shareCommentary");
                    if (shareCommentary != null && shareCommentary.has("text")) {
                        post.setContent(shareCommentary.get("text").asText());
                    }
                    
                    // Extract media URLs
                    JsonNode media = shareContent.get("media");
                    if (media != null && media.isArray()) {
                        List<String> mediaUrls = new ArrayList<>();
                        for (JsonNode mediaItem : media) {
                            if (mediaItem.has("originalUrl")) {
                                mediaUrls.add(mediaItem.get("originalUrl").asText());
                                
                                // Add media metadata
                                post.getMetadata().put("media_type", 
                                        mediaItem.has("mediaType") ? mediaItem.get("mediaType").asText() : "unknown");
                            }
                        }
                        post.setMediaUrls(mediaUrls);
                    }
                }
            }
            
            JsonNode created = root.get("created");
            if (created != null && created.has("time")) {
                long timestamp = created.get("time").asLong();
                post.setCreatedAt(Instant.ofEpochMilli(timestamp));
            }
            
            return post;
            
        } catch (Exception e) {
            logger.error("Failed to parse post", e);
            throw new RuntimeException("Failed to parse post", e);
        }
    }

    /**
     * Token bucket rate limiter implementation.
     */
    private static class TokenBucket {
        private final int capacity;
        private final int refillRatePerSecond;
        private final AtomicInteger tokens;
        private long lastRefillTime;
        
        public TokenBucket(int capacity, int refillPeriodSeconds) {
            this.capacity = capacity;
            this.refillRatePerSecond = capacity / refillPeriodSeconds;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        public synchronized void acquire() {
            refill();
            
            while (tokens.get() <= 0) {
                try {
                    logger.debug("Rate limit reached, waiting for tokens...");
                    Thread.sleep(1000);
                    refill();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for rate limit", e);
                }
            }
            
            tokens.decrementAndGet();
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            int tokensToAdd = (int) (timePassed / 1000 * refillRatePerSecond);
            
            if (tokensToAdd > 0) {
                int newTokens = Math.min(capacity, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }
        
        public void handleRateLimit() {
            // When we hit a rate limit, reduce tokens to zero
            tokens.set(0);
            logger.info("Rate limit hit, tokens reset to 0");
        }
    }
}
