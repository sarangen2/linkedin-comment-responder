---
inclusion: always
---
---
inclusion: always
---

# LinkedIn Comment Responder

An agentic workflow system that automates AI-powered responses to LinkedIn post comments with manual approval capabilities.

## Architecture

### Core Components

- **WorkflowOrchestrator**: Coordinates polling, comment processing, response generation, and posting workflows
- **LinkedInApiClient**: Handles LinkedIn API integration with OAuth 2.0, rate limiting, and retry logic
- **LLMAgent**: Interface for AI-powered response generation with tone preferences
- **StorageRepository**: Manages interaction history and comment processing state
- **ErrorHandler**: Centralized error categorization with correlation ID tracking

### Package Structure

```
com.example.linkedin/
├── agent/              # LLM integration interfaces
├── api/                # REST API controllers and DTOs
├── client/             # External API clients (LinkedIn)
├── config/             # Spring configuration and validation
├── error/              # Error handling and categorization
├── model/              # Domain models
├── orchestrator/       # Workflow coordination
├── repository/         # Data persistence interfaces
└── resilience/         # Circuit breakers and graceful degradation
```

## Code Patterns

### Error Handling

- Use `ErrorHandler` for centralized error processing with automatic categorization
- Always include correlation IDs via MDC for distributed tracing
- Categorize errors using `ErrorCategory` enum (AUTHENTICATION, RATE_LIMIT, LLM_ERROR, etc.)
- Critical errors trigger notifications via `ErrorNotificationService`

```java
Map<String, String> context = new HashMap<>();
context.put("commentId", comment.getId());
errorHandler.handleError(e, context);
```

### Resilience Patterns

- Wrap external API calls with `CircuitBreaker` to prevent cascading failures
- Circuit breaker states: CLOSED (normal), OPEN (failing), HALF_OPEN (testing recovery)
- Use exponential backoff with jitter for retries
- Implement token bucket rate limiting for API calls

### Logging

- Use SLF4J with structured logging and MDC context
- Include correlation IDs in all log statements
- Log levels: DEBUG (detailed flow), INFO (key events), WARN (recoverable issues), ERROR (failures)
- Avoid logging sensitive data (tokens, credentials)

### Workflow Orchestration

- Polling is scheduled via `@Scheduled` with configurable intervals
- Support both automatic posting and manual approval workflows
- Track comment processing state to avoid duplicates
- Store all interactions with metadata for audit trail

## Configuration

### Application Properties

- `linkedin.api.*`: LinkedIn OAuth credentials and tokens
- `llm.*`: LLM provider configuration (OpenAI by default)
- `workflow.*`: Polling intervals, approval mode, tone preferences, retry settings
- `storage.*`: Storage type and capacity settings

### Environment Variables

Sensitive credentials should use environment variables:
- `LINKEDIN_CLIENT_ID`, `LINKEDIN_CLIENT_SECRET`, `LINKEDIN_ACCESS_TOKEN`
- `OPENAI_API_KEY`

## API Design

### REST Endpoints

- Use `/api/management` base path for workflow management
- Return `ApiResponse<T>` wrapper with success/error states
- Include OpenAPI/Swagger annotations for documentation
- Validate request bodies with Jakarta Validation (`@Valid`)
- Use appropriate HTTP status codes (200, 400, 404, 409, 500)

### DTOs

- Separate DTOs in `api/dto` package
- Use validation annotations (`@NotBlank`, `@Min`, etc.)
- Keep DTOs focused on API contract, not domain logic

## Testing

- Use jqwik for property-based testing
- Test error scenarios and edge cases
- Mock external dependencies (LinkedIn API, LLM)
- Verify correlation ID propagation

## Best Practices

1. **Dependency Injection**: Use constructor injection, avoid field injection
2. **Immutability**: Prefer immutable domain models where possible
3. **Null Safety**: Validate inputs and handle null cases explicitly
4. **Thread Safety**: Use `AtomicBoolean`, `AtomicInteger` for concurrent state
5. **Resource Management**: Clean up MDC context in finally blocks
6. **API Versioning**: LinkedIn API uses `/v2` base path
7. **Rate Limiting**: Respect LinkedIn API limits (100 requests per 60 seconds)
8. **Token Management**: Refresh OAuth tokens before expiry

## Common Workflows

### Starting Polling

1. Validate `WorkflowConfig` (postId required)
2. Set `isPolling` flag to true
3. Scheduled method polls at configured interval
4. Filter unprocessed comments
5. Process each comment through workflow

### Processing Comments

1. Fetch original post for context
2. Check manual review keywords
3. Generate response via LLM with tone preference
4. Create interaction record
5. Route to manual approval or automatic posting

### Manual Approval

1. Store pending response, comment, and post
2. Log details for user review
3. Wait for approval/rejection via API
4. Post approved responses or discard rejected ones
5. Update interaction status and clear pending items

## Tone Preferences

Supported tones: "witty", "sarcastic", "wholesome", "professional"
- Pass tone to `LLMAgent.generateResponse()`
- Store tone in interaction metadata for analysis