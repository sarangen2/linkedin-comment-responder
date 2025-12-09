# Design Document: LinkedIn Comment Responder

## Overview

The LinkedIn Comment Responder is an agentic workflow system that automates the generation and posting of comedic, contextually-aware responses to LinkedIn post comments. The system integrates with LinkedIn's API to monitor comments, uses an LLM agent to generate appropriate responses based on the original post context, and automatically posts replies while maintaining a history of interactions.

The architecture follows a modular design with clear separation between API integration, AI-powered response generation, storage, and orchestration layers.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Workflow Orchestrator                     │
│  (Coordinates polling, generation, and posting workflow)     │
└───────────┬─────────────────────────────────────┬───────────┘
            │                                     │
            ▼                                     ▼
┌───────────────────────┐              ┌──────────────────────┐
│  LinkedIn API Client  │              │    LLM Agent         │
│  - Fetch comments     │              │  - Generate response │
│  - Post responses     │              │  - Context analysis  │
│  - Rate limiting      │              │  - Tone matching     │
└───────────┬───────────┘              └──────────┬───────────┘
            │                                     │
            └──────────────┬──────────────────────┘
                           ▼
                ┌──────────────────────┐
                │  Storage Repository  │
                │  - Interaction log   │
                │  - Processed comments│
                │  - Configuration     │
                └──────────────────────┘
```

### Component Interaction Flow

1. **Polling Phase**: Workflow Orchestrator triggers LinkedIn API Client to fetch new comments
2. **Filtering Phase**: System checks Storage Repository to identify unprocessed comments
3. **Context Gathering**: LinkedIn API Client retrieves original post content
4. **Generation Phase**: LLM Agent generates response using post context and comment
5. **Approval Phase** (if enabled): Present response to user for approval
6. **Posting Phase**: LinkedIn API Client posts the approved response
7. **Recording Phase**: Storage Repository logs the interaction

## Components and Interfaces

### 1. Workflow Orchestrator

**Responsibility**: Coordinates the end-to-end workflow of comment monitoring and response generation.

**Key Methods**:
- `startPolling(postId: String, config: WorkflowConfig): void` - Initiates periodic comment checking
- `stopPolling(): void` - Halts the polling process
- `processComment(comment: Comment, post: Post): Response` - Orchestrates single comment processing
- `handleError(error: Error): void` - Centralized error handling

**Configuration**:
```java
class WorkflowConfig {
    int pollingIntervalSeconds;
    boolean requireManualApproval;
    String tonePreference;
    List<String> manualReviewKeywords;
    int maxRetries;
}
```

### 2. LinkedIn API Client

**Responsibility**: Interfaces with LinkedIn's API for reading comments and posting responses.

**Key Methods**:
- `fetchComments(postId: String): List<Comment>` - Retrieves comments for a post
- `fetchPost(postId: String): Post` - Retrieves original post content
- `postReply(commentId: String, responseText: String): PostResult` - Posts a response
- `authenticate(credentials: Credentials): AuthToken` - Handles OAuth authentication

**Data Models**:
```java
class Comment {
    String id;
    String authorId;
    String authorName;
    String text;
    Instant timestamp;
}

class Post {
    String id;
    String content;
    List<String> mediaUrls;
    String theme;
    Instant createdAt;
}
```

**Rate Limiting Strategy**:
- Implement token bucket algorithm
- Track API calls per time window
- Exponential backoff on rate limit errors (429 responses)
- Configurable rate limits per LinkedIn API tier

### 3. LLM Agent

**Responsibility**: Generates contextually appropriate, comedic responses using an LLM.

**Key Methods**:
- `generateResponse(post: Post, comment: Comment, config: ResponseConfig): String` - Creates response
- `analyzePostTheme(post: Post): Theme` - Extracts theme and tone from post
- `validateResponse(response: String): ValidationResult` - Checks response quality

**Prompt Engineering**:
```
System Prompt Template:
"You are a witty LinkedIn engagement assistant. Your task is to generate 
responses to comments that:
1. Acknowledge the commenter's specific point
2. Add comedic value aligned with the post's theme: {theme}
3. Match the tone: {tone}
4. Stay under 1250 characters
5. Remain professional yet entertaining

Original Post: {post_content}
Comment: {comment_text}
Commenter: {commenter_name}

Generate an appropriate response:"
```

**LLM Integration**:
- Support for multiple LLM providers (OpenAI, Anthropic, AWS Bedrock)
- Configurable model selection (GPT-4, Claude, etc.)
- Temperature and creativity parameters
- Fallback to simpler models on failure

### 4. Storage Repository

**Responsibility**: Persists interaction history, processed comment tracking, and configuration.

**Key Methods**:
- `saveInteraction(interaction: Interaction): void` - Stores response history
- `isCommentProcessed(commentId: String): boolean` - Checks if comment was handled
- `markCommentProcessed(commentId: String): void` - Marks comment as processed
- `getInteractionHistory(filter: HistoryFilter): List<Interaction>` - Queries history
- `exportHistory(format: ExportFormat): File` - Exports interaction data

**Data Models**:
```java
class Interaction {
    String id;
    String postId;
    String commentId;
    String commenterName;
    String commentText;
    String generatedResponse;
    String postedResponse;
    Instant timestamp;
    ResponseStatus status;
    Map<String, String> metadata;
}

enum ResponseStatus {
    GENERATED,
    APPROVED,
    POSTED,
    FAILED,
    REJECTED
}
```

**Storage Options**:
- File-based: JSON files for simple deployments
- Database: PostgreSQL/MySQL for production
- In-memory: For testing and development

## Data Models

### Core Domain Models

```java
// Comment representation
class Comment {
    String id;
    String postId;
    String authorId;
    String authorName;
    String text;
    Instant timestamp;
    boolean isProcessed;
}

// Post representation
class Post {
    String id;
    String authorId;
    String content;
    List<String> mediaUrls;
    Map<String, String> metadata;
    Instant createdAt;
}

// Generated response
class GeneratedResponse {
    String text;
    double confidenceScore;
    String reasoning;
    List<String> warnings;
}

// Workflow configuration
class WorkflowConfig {
    String postId;
    int pollingIntervalSeconds;
    boolean requireManualApproval;
    String tonePreference;
    List<String> manualReviewKeywords;
    int maxRetries;
    int retryBackoffSeconds;
}

// API credentials
class LinkedInCredentials {
    String clientId;
    String clientSecret;
    String accessToken;
    String refreshToken;
    Instant tokenExpiry;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Comment retrieval belongs to post
*For any* valid post identifier, all retrieved comments should have a postId field matching the requested post identifier.
**Validates: Requirements 1.1**

### Property 2: Comment extraction completeness
*For any* comment retrieved from the API, the extracted comment object should contain non-null text, author information, and timestamp fields.
**Validates: Requirements 1.2**

### Property 3: Retry with exponential backoff
*For any* API operation that fails, the system should retry with delays that increase exponentially (e.g., 1s, 2s, 4s) up to the configured maximum retry count.
**Validates: Requirements 1.3, 4.3**

### Property 4: Processed comment filtering
*For any* set of comments where some are marked as processed, filtering should return only comments where isProcessed is false, and the count of filtered comments plus processed comments should equal the original count.
**Validates: Requirements 1.4**

### Property 5: Rate limit compliance
*For any* sequence of API calls within a time window, the total number of calls should not exceed the configured rate limit for that window.
**Validates: Requirements 1.5**

### Property 6: Post content extraction completeness
*For any* post object, extracting content should return the complete content field without truncation or modification.
**Validates: Requirements 2.1**

### Property 7: Media metadata inclusion
*For any* post containing media URLs or links, the extracted context should include metadata entries for each media element.
**Validates: Requirements 2.2**

### Property 8: Theme and tone identification
*For any* valid post with content, analyzing the post should return non-empty theme and tone values.
**Validates: Requirements 2.3**

### Property 9: Response acknowledges comment
*For any* generated response and its corresponding comment, the response text should contain references to or acknowledgment of content from the comment.
**Validates: Requirements 3.1**

### Property 10: Response length constraint
*For any* generated response, the character count should be less than or equal to 1250 characters.
**Validates: Requirements 3.5**

### Property 11: Response posting verification
*For any* response posting operation, the system should verify and return a success or failure status from the LinkedIn API.
**Validates: Requirements 4.1, 4.2**

### Property 12: Successful post marks comment processed
*For any* comment that receives a successfully posted response, querying the processed status of that comment should return true.
**Validates: Requirements 4.4**

### Property 13: Manual approval workflow
*For any* generated response when manual approval mode is enabled, the response should not be posted until user approval is received.
**Validates: Requirements 5.1**

### Property 14: Automatic posting workflow
*For any* generated response when automatic mode is enabled, the response should be posted without requiring user intervention.
**Validates: Requirements 5.2**

### Property 15: Keyword-triggered manual review
*For any* comment containing configured manual review keywords, the system should flag that comment for manual review regardless of automatic mode settings.
**Validates: Requirements 5.5**

### Property 16: Complete interaction storage
*For any* response generation event, the stored interaction should contain all required fields: comment text, generated response, timestamp, post ID, commenter information, and response status.
**Validates: Requirements 6.1, 6.2**

### Property 17: History filtering accuracy
*For any* history query with date range, post ID, or commenter filters, all returned interactions should match the specified filter criteria, and no matching interactions should be excluded.
**Validates: Requirements 6.3**

### Property 18: History archival on capacity
*For any* storage at capacity, adding new interactions should result in the oldest entries being archived, and the total active entries should not exceed capacity.
**Validates: Requirements 6.4**

### Property 19: Export format validity
*For any* history export request, the generated file should be valid JSON or CSV format and should be parseable by standard parsers.
**Validates: Requirements 6.5**

### Property 20: Error logging completeness
*For any* error that occurs, the error log entry should contain timestamp, context information, and stack trace.
**Validates: Requirements 7.1**

### Property 21: Critical error notification
*For any* critical error event, a notification should be sent to the configured alert channel within a reasonable time window.
**Validates: Requirements 7.2**

### Property 22: Startup configuration validation
*For any* system startup, if required configuration parameters are missing, the system should fail to start and provide a clear error message listing the missing parameters.
**Validates: Requirements 7.3**

### Property 23: Rate limit adaptation
*For any* rate limiting event (429 response), the system should both log the event and increase the polling interval to reduce request frequency.
**Validates: Requirements 7.5**

## Error Handling

### Error Categories

1. **API Errors**
   - Authentication failures (401, 403)
   - Rate limiting (429)
   - Not found (404)
   - Server errors (500, 502, 503)

2. **LLM Errors**
   - Model unavailable
   - Token limit exceeded
   - Invalid response format
   - Timeout

3. **Storage Errors**
   - Disk full
   - Database connection failure
   - Corruption
   - Permission denied

4. **Validation Errors**
   - Invalid configuration
   - Malformed input
   - Missing required fields

### Error Handling Strategies

**Retry Strategy**:
- Transient errors (rate limits, timeouts): Exponential backoff with jitter
- Authentication errors: Attempt token refresh, then fail
- Server errors: Retry up to 3 times, then fail
- Client errors (4xx except 429): Fail immediately, no retry

**Fallback Mechanisms**:
- LLM failure: Use simpler model or template-based responses
- Storage failure: Queue interactions in memory, retry persistence
- API failure: Log locally, continue with cached data if available

**Error Notification**:
- Critical errors: Immediate notification via configured channel (email, Slack, etc.)
- Warning-level errors: Batched notifications every hour
- Info-level errors: Log only, no notification

**Circuit Breaker Pattern**:
- Track failure rate for external services
- Open circuit after 5 consecutive failures
- Half-open after 60 seconds to test recovery
- Close circuit after 3 consecutive successes

## Testing Strategy

### Unit Testing

Unit tests will verify specific examples and edge cases:

**LinkedIn API Client Tests**:
- Test comment fetching with valid post ID
- Test handling of empty comment lists
- Test authentication token refresh flow
- Test rate limit detection and backoff
- Test malformed API responses

**LLM Agent Tests**:
- Test response generation with sample post and comment
- Test response length validation
- Test handling of empty or null inputs
- Test prompt template rendering
- Test model fallback on primary model failure

**Storage Repository Tests**:
- Test interaction saving and retrieval
- Test duplicate comment detection
- Test history filtering by various criteria
- Test export to JSON and CSV formats
- Test archival when capacity is reached

**Workflow Orchestrator Tests**:
- Test end-to-end workflow with mocked dependencies
- Test error propagation and handling
- Test configuration validation
- Test polling start/stop lifecycle

### Property-Based Testing

Property-based tests will verify universal properties across many inputs using a PBT library. For Java, we will use **jqwik** (https://jqwik.net/), a modern property-based testing framework that integrates with JUnit 5.

**Configuration**:
- Each property test should run a minimum of 100 iterations
- Use jqwik's `@Property` annotation with `tries = 100` parameter
- Each test must be tagged with a comment referencing the design property

**Test Tagging Format**:
```java
// Feature: linkedin-comment-responder, Property 1: Comment retrieval belongs to post
@Property(tries = 100)
void allRetrievedCommentsBelongToRequestedPost(@ForAll String postId) {
    // test implementation
}
```

**Property Test Coverage**:
- Property 1-23: Each correctness property will have one corresponding property-based test
- Tests will use jqwik's generators to create random but valid test data
- Custom generators will be created for domain objects (Comment, Post, etc.)

**Generator Strategy**:
- Create `Arbitraries` for Comment, Post, WorkflowConfig, and other domain objects
- Constrain generators to produce valid data (e.g., non-empty strings, valid timestamps)
- Use jqwik's combinators to create complex test scenarios
- Implement shrinking for better failure reporting

### Integration Testing

Integration tests will verify component interactions:

- Test LinkedIn API Client with mock HTTP server
- Test LLM Agent with mock LLM API
- Test Storage Repository with test database
- Test full workflow with all components integrated

### Test Execution

- Unit tests: Run on every build
- Property tests: Run on every build (100 iterations each)
- Integration tests: Run on pre-commit and CI/CD pipeline
- Manual testing: Required for UI/approval flows

## Deployment Considerations

### Configuration Management

- Environment-specific configuration files (dev, staging, prod)
- Secrets management via environment variables or secret manager
- Configuration validation on startup

### Monitoring and Observability

- Metrics: API call counts, response times, error rates, LLM token usage
- Logging: Structured JSON logs with correlation IDs
- Tracing: Distributed tracing for workflow execution
- Dashboards: Real-time monitoring of system health

### Scalability

- Horizontal scaling: Multiple instances can poll different posts
- Rate limiting: Shared rate limiter across instances (Redis-based)
- Storage: Database connection pooling and read replicas
- LLM: Request queuing and batching for efficiency

### Security

- OAuth 2.0 for LinkedIn API authentication
- Secure credential storage (encrypted at rest)
- API key rotation support
- Input sanitization to prevent injection attacks
- Rate limiting to prevent abuse

## Future Enhancements

1. **Multi-platform support**: Extend to Twitter, Facebook, Instagram
2. **Response personalization**: Learn from user edits to improve generation
3. **Sentiment analysis**: Adjust tone based on comment sentiment
4. **A/B testing**: Test different response styles and measure engagement
5. **Analytics dashboard**: Visualize engagement metrics and response performance
6. **Batch processing**: Handle multiple posts simultaneously
7. **Response templates**: Allow users to define custom response templates
8. **Machine learning**: Train custom models on user's writing style
