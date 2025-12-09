# Configuration Examples

This document provides example configurations for different use cases of the LinkedIn Comment Responder.

## Table of Contents

1. [Basic Setup](#basic-setup)
2. [Development Environment](#development-environment)
3. [Production Deployment](#production-deployment)
4. [Conservative Mode](#conservative-mode)
5. [Aggressive Engagement](#aggressive-engagement)
6. [Multi-Post Monitoring](#multi-post-monitoring)
7. [Professional Tone](#professional-tone)
8. [Casual/Fun Tone](#casualfun-tone)
9. [High-Volume Posts](#high-volume-posts)
10. [Low-Volume Posts](#low-volume-posts)

---

## Basic Setup

Minimal configuration for getting started quickly.

### Environment Variables

```bash
export LINKEDIN_CLIENT_ID="your-client-id"
export LINKEDIN_CLIENT_SECRET="your-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-access-token"
export OPENAI_API_KEY="sk-your-api-key"
```

### Start Polling

```bash
curl -X POST http://localhost:8080/api/management/polling/start \
  -H "Content-Type: application/json" \
  -d '{
    "postId": "urn:li:share:1234567890",
    "pollingIntervalSeconds": 300,
    "requireManualApproval": true,
    "tonePreference": "witty"
  }'
```

**Use Case**: First-time users who want to test the system with manual approval.

---

## Development Environment

Configuration optimized for local development and testing.

### application-dev.properties

```properties
# LinkedIn API
linkedin.api.client-id=${LINKEDIN_CLIENT_ID}
linkedin.api.client-secret=${LINKEDIN_CLIENT_SECRET}
linkedin.api.access-token=${LINKEDIN_ACCESS_TOKEN}
linkedin.api.base-url=https://api.linkedin.com/v2
linkedin.api.rate-limit-per-minute=50

# LLM Configuration (use cheaper model for dev)
llm.provider=openai
llm.api-key=${OPENAI_API_KEY}
llm.model=gpt-3.5-turbo
llm.temperature=0.7
llm.max-tokens=300

# Workflow (faster polling for testing)
workflow.polling-interval-seconds=60
workflow.require-manual-approval=true
workflow.tone-preference=witty
workflow.max-retries=3
workflow.retry-backoff-seconds=2

# Storage (local files)
storage.directory=./data/dev
storage.max.capacity=100

# Error Notifications (log only)
error.notification.enabled=true
error.notification.channel=log

# Logging (verbose)
logging.level.com.example.linkedin=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Run Command

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Use Case**: Local development, testing, and debugging.

---

## Production Deployment

Configuration optimized for production use with automatic posting.

### application-prod.properties

```properties
# LinkedIn API
linkedin.api.client-id=${LINKEDIN_CLIENT_ID}
linkedin.api.client-secret=${LINKEDIN_CLIENT_SECRET}
linkedin.api.access-token=${LINKEDIN_ACCESS_TOKEN}
linkedin.api.refresh-token=${LINKEDIN_REFRESH_TOKEN}
linkedin.api.base-url=https://api.linkedin.com/v2
linkedin.api.rate-limit-per-minute=100

# LLM Configuration (production model)
llm.provider=openai
llm.api-key=${OPENAI_API_KEY}
llm.model=gpt-4
llm.temperature=0.7
llm.max-tokens=500

# Workflow (automatic posting)
workflow.polling-interval-seconds=300
workflow.require-manual-approval=false
workflow.tone-preference=professional
workflow.max-retries=3
workflow.retry-backoff-seconds=2
workflow.manual-review-keywords=urgent,complaint,legal,refund,angry,lawsuit

# Storage (persistent)
storage.directory=/var/lib/linkedin-responder/prod
storage.max.capacity=10000

# Error Notifications (Slack)
error.notification.enabled=true
error.notification.channel=slack
error.notification.slack-webhook=${SLACK_WEBHOOK_URL}

# Circuit Breaker
circuit-breaker.failure-threshold=5
circuit-breaker.reset-timeout-ms=60000
circuit-breaker.success-threshold=3

# Logging (minimal)
logging.level.com.example.linkedin=INFO
logging.level.org.springframework.web=WARN
logging.file.name=/var/log/linkedin-responder/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### Docker Compose

```yaml
version: '3.8'
services:
  linkedin-responder:
    image: linkedin-responder:latest
    container_name: linkedin-responder-prod
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - LINKEDIN_CLIENT_ID=${LINKEDIN_CLIENT_ID}
      - LINKEDIN_CLIENT_SECRET=${LINKEDIN_CLIENT_SECRET}
      - LINKEDIN_ACCESS_TOKEN=${LINKEDIN_ACCESS_TOKEN}
      - LINKEDIN_REFRESH_TOKEN=${LINKEDIN_REFRESH_TOKEN}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - SLACK_WEBHOOK_URL=${SLACK_WEBHOOK_URL}
    ports:
      - "8080:8080"
    volumes:
      - ./data:/var/lib/linkedin-responder/prod
      - ./logs:/var/log/linkedin-responder
    restart: unless-stopped
```

**Use Case**: Production deployment with automatic posting and Slack notifications.

---

## Conservative Mode

Configuration for careful, reviewed responses with high quality standards.

### Start Polling Request

```json
{
  "postId": "urn:li:share:1234567890",
  "pollingIntervalSeconds": 600,
  "requireManualApproval": true,
  "tonePreference": "professional",
  "manualReviewKeywords": [
    "urgent", "complaint", "issue", "problem", "concern",
    "disappointed", "frustrated", "angry", "legal", "refund",
    "cancel", "unsubscribe", "terrible", "worst", "hate"
  ],
  "maxRetries": 5,
  "retryBackoffSeconds": 5
}
```

### LLM Configuration

```properties
llm.model=gpt-4
llm.temperature=0.3
llm.max-tokens=400
```

**Characteristics**:
- Manual approval required for all responses
- Professional tone
- Lower temperature for more conservative responses
- Extensive keyword list for manual review
- Longer polling interval
- More retries with longer backoff

**Use Case**: Corporate accounts, sensitive topics, brand-conscious users.

---

## Aggressive Engagement

Configuration for maximum engagement with quick, witty responses.

### Start Polling Request

```json
{
  "postId": "urn:li:share:1234567890",
  "pollingIntervalSeconds": 120,
  "requireManualApproval": false,
  "tonePreference": "witty",
  "manualReviewKeywords": ["legal", "lawsuit", "sue"],
  "maxRetries": 3,
  "retryBackoffSeconds": 2
}
```

### LLM Configuration

```properties
llm.model=gpt-4
llm.temperature=0.9
llm.max-tokens=500
```

**Characteristics**:
- Automatic posting (no manual approval)
- Witty, engaging tone
- Higher temperature for more creative responses
- Short polling interval for quick responses
- Minimal manual review keywords

**Use Case**: Personal brands, content creators, viral posts.

---

## Multi-Post Monitoring

Configuration for monitoring multiple posts simultaneously.

### Script to Start Multiple Posts

```bash
#!/bin/bash

# Array of post IDs to monitor
POST_IDS=(
  "urn:li:share:1111111111"
  "urn:li:share:2222222222"
  "urn:li:share:3333333333"
)

# Start polling for each post
for POST_ID in "${POST_IDS[@]}"; do
  echo "Starting polling for $POST_ID"
  
  curl -X POST http://localhost:8080/api/management/polling/start \
    -H "Content-Type: application/json" \
    -d "{
      \"postId\": \"$POST_ID\",
      \"pollingIntervalSeconds\": 300,
      \"requireManualApproval\": false,
      \"tonePreference\": \"witty\"
    }"
  
  echo ""
  sleep 2
done

echo "All posts are now being monitored"
```

**Note**: Current implementation supports one post at a time. For multi-post support, you would need to:
1. Run multiple instances of the application
2. Use different storage directories for each instance
3. Coordinate rate limiting across instances

**Use Case**: Power users monitoring multiple posts.

---

## Professional Tone

Configuration for formal, business-appropriate responses.

### Start Polling Request

```json
{
  "postId": "urn:li:share:1234567890",
  "pollingIntervalSeconds": 300,
  "requireManualApproval": true,
  "tonePreference": "professional",
  "manualReviewKeywords": [],
  "maxRetries": 3,
  "retryBackoffSeconds": 2
}
```

### LLM Configuration

```properties
llm.model=gpt-4
llm.temperature=0.5
llm.max-tokens=400
```

### Example Prompt Customization

If you want to customize the prompt for even more professional responses, modify the `OpenAILLMAgent.java`:

```java
private String buildPrompt(Post post, Comment comment, String tonePreference) {
    return String.format("""
        You are a professional LinkedIn engagement assistant for a corporate account.
        
        Your task is to generate responses that:
        1. Acknowledge the commenter's point professionally
        2. Maintain a formal, business-appropriate tone
        3. Align with the post's theme: %s
        4. Stay under 1250 characters
        5. Avoid humor, sarcasm, or casual language
        6. Focus on value and professionalism
        
        Original Post: %s
        Comment: %s
        Commenter: %s
        
        Generate an appropriate professional response:
        """,
        post.getTheme(),
        post.getContent(),
        comment.getText(),
        comment.getAuthorName()
    );
}
```

**Use Case**: Corporate accounts, B2B companies, executive profiles.

---

## Casual/Fun Tone

Configuration for entertaining, personality-driven responses.

### Start Polling Request

```json
{
  "postId": "urn:li:share:1234567890",
  "pollingIntervalSeconds": 180,
  "requireManualApproval": false,
  "tonePreference": "casual",
  "manualReviewKeywords": ["complaint", "angry", "legal"],
  "maxRetries": 3,
  "retryBackoffSeconds": 2
}
```

### LLM Configuration

```properties
llm.model=gpt-4
llm.temperature=0.9
llm.max-tokens=500
```

**Characteristics**:
- Casual, fun tone
- High temperature for creativity
- Automatic posting for quick engagement
- Minimal restrictions

**Use Case**: Personal brands, influencers, entertainment content.

---

## High-Volume Posts

Configuration optimized for posts with many comments.

### Start Polling Request

```json
{
  "postId": "urn:li:share:1234567890",
  "pollingIntervalSeconds": 120,
  "requireManualApproval": false,
  "tonePreference": "witty",
  "manualReviewKeywords": ["urgent", "complaint", "legal"],
  "maxRetries": 3,
  "retryBackoffSeconds": 2
}
```

### Configuration Adjustments

```properties
# Increase rate limit
linkedin.api.rate-limit-per-minute=150

# Increase storage capacity
storage.max.capacity=50000

# Use faster model
llm.model=gpt-3.5-turbo
llm.max-tokens=300

# Optimize circuit breaker
circuit-breaker.failure-threshold=10
circuit-breaker.reset-timeout-ms=30000
```

**Characteristics**:
- Short polling interval
- Higher rate limits
- Larger storage capacity
- Faster (cheaper) LLM model
- Automatic posting

**Use Case**: Viral posts, popular content creators.

---

## Low-Volume Posts

Configuration optimized for posts with few comments.

### Start Polling Request

```json
{
  "postId": "urn:li:share:1234567890",
  "pollingIntervalSeconds": 600,
  "requireManualApproval": true,
  "tonePreference": "professional",
  "manualReviewKeywords": [],
  "maxRetries": 3,
  "retryBackoffSeconds": 2
}
```

### Configuration Adjustments

```properties
# Lower rate limit (save API calls)
linkedin.api.rate-limit-per-minute=30

# Smaller storage
storage.max.capacity=1000

# Use premium model for quality
llm.model=gpt-4
llm.temperature=0.7
llm.max-tokens=500
```

**Characteristics**:
- Longer polling interval (save API calls)
- Manual approval for quality
- Premium LLM model
- Lower rate limits

**Use Case**: Niche content, B2B posts, specialized topics.

---

## Testing & Debugging

Configuration for testing and debugging the system.

### application-test.properties

```properties
# LinkedIn API (use test credentials)
linkedin.api.client-id=test-client-id
linkedin.api.client-secret=test-client-secret
linkedin.api.access-token=test-access-token
linkedin.api.base-url=http://localhost:9000/mock-linkedin-api
linkedin.api.rate-limit-per-minute=1000

# LLM Configuration (use mock or cheap model)
llm.provider=openai
llm.api-key=${OPENAI_API_KEY}
llm.model=gpt-3.5-turbo
llm.temperature=0.5
llm.max-tokens=200

# Workflow (fast polling for testing)
workflow.polling-interval-seconds=10
workflow.require-manual-approval=true
workflow.tone-preference=witty
workflow.max-retries=1
workflow.retry-backoff-seconds=1

# Storage (temporary)
storage.directory=./data/test
storage.max.capacity=10

# Error Notifications (disabled)
error.notification.enabled=false

# Logging (very verbose)
logging.level.com.example.linkedin=TRACE
logging.level.org.springframework=DEBUG
```

### Mock LinkedIn API Server

```bash
# Use a tool like WireMock or json-server
npm install -g json-server

# Create mock data
cat > db.json << EOF
{
  "comments": [
    {
      "id": "comment-1",
      "postId": "urn:li:share:1234567890",
      "authorId": "user-1",
      "authorName": "John Doe",
      "text": "Great post!",
      "timestamp": "2024-12-08T10:00:00Z"
    }
  ]
}
EOF

# Start mock server
json-server --watch db.json --port 9000
```

**Use Case**: Unit testing, integration testing, CI/CD pipelines.

---

## Environment-Specific Examples

### Development

```bash
export SPRING_PROFILES_ACTIVE=dev
export LINKEDIN_CLIENT_ID="dev-client-id"
export LINKEDIN_CLIENT_SECRET="dev-client-secret"
export LINKEDIN_ACCESS_TOKEN="dev-access-token"
export OPENAI_API_KEY="sk-dev-key"

mvn spring-boot:run
```

### Staging

```bash
export SPRING_PROFILES_ACTIVE=staging
export LINKEDIN_CLIENT_ID="staging-client-id"
export LINKEDIN_CLIENT_SECRET="staging-client-secret"
export LINKEDIN_ACCESS_TOKEN="staging-access-token"
export OPENAI_API_KEY="sk-staging-key"
export NOTIFICATION_EMAIL="staging-alerts@example.com"

java -jar target/linkedin-comment-responder.jar
```

### Production

```bash
export SPRING_PROFILES_ACTIVE=prod
export LINKEDIN_CLIENT_ID="prod-client-id"
export LINKEDIN_CLIENT_SECRET="prod-client-secret"
export LINKEDIN_ACCESS_TOKEN="prod-access-token"
export LINKEDIN_REFRESH_TOKEN="prod-refresh-token"
export OPENAI_API_KEY="sk-prod-key"
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/PROD/WEBHOOK"

java -Xmx2g -jar target/linkedin-comment-responder.jar
```

---

## Quick Reference

### Tone Preferences

| Tone | Temperature | Use Case |
|------|-------------|----------|
| `professional` | 0.3-0.5 | Corporate, B2B |
| `witty` | 0.7-0.8 | Personal brand, engaging |
| `sarcastic` | 0.8-0.9 | Entertainment, humor |
| `wholesome` | 0.5-0.7 | Friendly, warm |
| `casual` | 0.7-0.9 | Relaxed, fun |

### Polling Intervals

| Interval | Use Case |
|----------|----------|
| 60s | Testing, development |
| 120s | High-volume posts |
| 300s | Standard posts |
| 600s | Low-volume posts |
| 900s | Very low-volume, API conservation |

### LLM Models

| Model | Cost | Speed | Quality | Use Case |
|-------|------|-------|---------|----------|
| gpt-3.5-turbo | Low | Fast | Good | High-volume, testing |
| gpt-4 | High | Slow | Excellent | Production, quality |
| gpt-4-turbo | Medium | Fast | Excellent | Production, balanced |

---

## Additional Resources

- [Configuration Guide](CONFIGURATION.md) - Complete configuration reference
- [API Documentation](API_DOCUMENTATION.md) - API endpoint details
- [Troubleshooting](TROUBLESHOOTING.md) - Common issues and solutions
- [Main README](README.md) - Getting started guide

---

**Last Updated**: December 2024
