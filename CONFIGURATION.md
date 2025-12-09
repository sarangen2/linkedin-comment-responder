# LinkedIn Comment Responder - Configuration Guide

This document provides comprehensive information about configuring the LinkedIn Comment Responder application.

## Table of Contents

1. [Overview](#overview)
2. [Environment Profiles](#environment-profiles)
3. [Configuration Parameters](#configuration-parameters)
4. [Environment Variables](#environment-variables)
5. [Configuration Validation](#configuration-validation)
6. [Examples](#examples)
7. [Troubleshooting](#troubleshooting)

## Overview

The LinkedIn Comment Responder uses Spring Boot's configuration system with support for:
- Environment-specific profiles (dev, staging, prod)
- Environment variable overrides for sensitive credentials
- Startup validation to catch configuration errors early
- Secure credential management

## Environment Profiles

The application supports three environment profiles:

### Development (`dev`)
- **Purpose**: Local development and testing
- **Characteristics**:
  - Verbose logging (DEBUG level)
  - Faster polling intervals (60 seconds)
  - Manual approval required
  - Lower-cost LLM model (gpt-3.5-turbo)
  - Local file storage
  - Log-only error notifications

**Activate**: `java -jar app.jar --spring.profiles.active=dev`

### Staging (`staging`)
- **Purpose**: Pre-production testing
- **Characteristics**:
  - Moderate logging (INFO level)
  - Moderate polling intervals (180 seconds)
  - Manual approval required
  - Production LLM model for testing
  - Email error notifications
  - File-based logging

**Activate**: `java -jar app.jar --spring.profiles.active=staging`

### Production (`prod`)
- **Purpose**: Production deployment
- **Characteristics**:
  - Minimal logging (INFO/WARN level)
  - Standard polling intervals (300 seconds)
  - Automatic posting enabled
  - Production LLM model
  - Slack error notifications
  - Comprehensive logging with rotation

**Activate**: `java -jar app.jar --spring.profiles.active=prod`

## Configuration Parameters

### LinkedIn API Configuration

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `linkedin.api.client-id` | LinkedIn OAuth client ID | - | Yes |
| `linkedin.api.client-secret` | LinkedIn OAuth client secret | - | Yes |
| `linkedin.api.access-token` | LinkedIn API access token | - | Yes |
| `linkedin.api.refresh-token` | LinkedIn API refresh token | - | No |
| `linkedin.api.base-url` | LinkedIn API base URL | `https://api.linkedin.com/v2` | Yes |
| `linkedin.api.rate-limit-per-minute` | API rate limit | 100 | Yes |

**Notes**:
- Credentials should be provided via environment variables
- Access tokens expire and need periodic refresh
- Rate limits vary by LinkedIn API tier

### LLM Configuration

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `llm.provider` | LLM provider (openai, anthropic, bedrock) | openai | Yes |
| `llm.api-key` | LLM API key | - | Yes |
| `llm.model` | Model name (e.g., gpt-4, gpt-3.5-turbo) | gpt-4 | Yes |
| `llm.temperature` | Response creativity (0.0-2.0) | 0.7 | Yes |
| `llm.max-tokens` | Maximum response tokens | 500 | Yes |

**Notes**:
- Temperature: Lower = more focused, Higher = more creative
- Max tokens: Affects response length and cost
- Different models have different capabilities and costs

### Workflow Configuration

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `workflow.polling-interval-seconds` | Comment polling interval | 300 | Yes |
| `workflow.require-manual-approval` | Require approval before posting | false | Yes |
| `workflow.tone-preference` | Response tone (witty, sarcastic, wholesome, professional, casual) | witty | Yes |
| `workflow.max-retries` | Maximum retry attempts | 3 | Yes |
| `workflow.retry-backoff-seconds` | Initial retry delay | 2 | Yes |
| `workflow.manual-review-keywords` | Keywords triggering manual review (comma-separated) | - | No |

**Notes**:
- Shorter polling intervals increase API usage
- Manual approval recommended for initial deployment
- Manual review keywords are case-insensitive

### Storage Configuration

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `storage.directory` | Base storage directory | ./data | Yes |
| `storage.interactions.file` | Interaction history filename | interactions.json | Yes |
| `storage.processed.file` | Processed comments filename | processed-comments.json | Yes |
| `storage.max.capacity` | Maximum interactions before archival | 1000 | Yes |
| `storage.archive.directory` | Archive directory | ./data/archive | Yes |

**Notes**:
- Directory must be writable by application
- Automatic archival when capacity reached
- JSON format for easy inspection

### Error Notification Configuration

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `error.notification.enabled` | Enable error notifications | false | Yes |
| `error.notification.channel` | Notification channel (log, email, slack) | log | Yes |
| `error.notification.email` | Email address for notifications | - | If channel=email |
| `error.notification.slack-webhook` | Slack webhook URL | - | If channel=slack |

**Notes**:
- Log channel: Errors logged only
- Email channel: Requires SMTP configuration
- Slack channel: Requires webhook URL

### Circuit Breaker Configuration

| Parameter | Description | Default | Required |
|-----------|-------------|---------|----------|
| `circuit-breaker.failure-threshold` | Failures before opening circuit | 5 | Yes |
| `circuit-breaker.reset-timeout-ms` | Time before retry attempt | 60000 | Yes |
| `circuit-breaker.success-threshold` | Successes to close circuit | 3 | Yes |

**Notes**:
- Protects against cascading failures
- Applies to external service calls
- Automatic recovery when service restored

## Environment Variables

Sensitive credentials should be provided via environment variables:

### Required Environment Variables

```bash
# LinkedIn API Credentials
export LINKEDIN_CLIENT_ID="your-linkedin-client-id"
export LINKEDIN_CLIENT_SECRET="your-linkedin-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-linkedin-access-token"
export LINKEDIN_REFRESH_TOKEN="your-linkedin-refresh-token"

# LLM API Key
export OPENAI_API_KEY="your-openai-api-key"
```

### Optional Environment Variables

```bash
# Error Notifications
export NOTIFICATION_EMAIL="admin@example.com"
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
```

### Setting Environment Variables

**Linux/macOS**:
```bash
# Temporary (current session)
export LINKEDIN_CLIENT_ID="your-value"

# Permanent (add to ~/.bashrc or ~/.zshrc)
echo 'export LINKEDIN_CLIENT_ID="your-value"' >> ~/.bashrc
source ~/.bashrc
```

**Windows**:
```cmd
# Temporary (current session)
set LINKEDIN_CLIENT_ID=your-value

# Permanent (System Properties > Environment Variables)
setx LINKEDIN_CLIENT_ID "your-value"
```

**Docker**:
```bash
docker run -e LINKEDIN_CLIENT_ID="your-value" \
           -e OPENAI_API_KEY="your-key" \
           linkedin-responder:latest
```

## Configuration Validation

The application validates all configuration on startup and will fail fast with clear error messages if:

- Required parameters are missing
- Parameters have invalid values
- Storage directories are not writable
- Notification channels are misconfigured

### Validation Levels

**Errors** (application will not start):
- Missing required credentials
- Invalid parameter values
- Inaccessible storage directories

**Warnings** (application will start but log warnings):
- Unusual parameter values
- Potential performance issues
- Recommended configuration changes

### Example Validation Output

```
2024-12-08 10:00:00 - Validating application configuration...
2024-12-08 10:00:00 - Configuration warnings:
2024-12-08 10:00:00 -   - Polling interval is very short (60s), may cause rate limiting
2024-12-08 10:00:00 -   - Storage capacity is very low (100), may archive frequently
2024-12-08 10:00:00 - Configuration validation passed
2024-12-08 10:00:00 - === Configuration Summary ===
2024-12-08 10:00:00 - LinkedIn API Base URL: https://api.linkedin.com/v2
2024-12-08 10:00:00 - LinkedIn Client ID: abcd***
2024-12-08 10:00:00 - Rate Limit: 100/min
...
```

## Examples

### Example 1: Development Setup

```bash
# Set environment variables
export LINKEDIN_CLIENT_ID="dev-client-id"
export LINKEDIN_CLIENT_SECRET="dev-client-secret"
export LINKEDIN_ACCESS_TOKEN="dev-access-token"
export OPENAI_API_KEY="sk-..."

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Example 2: Production Deployment

```bash
# Set production environment variables
export LINKEDIN_CLIENT_ID="prod-client-id"
export LINKEDIN_CLIENT_SECRET="prod-client-secret"
export LINKEDIN_ACCESS_TOKEN="prod-access-token"
export OPENAI_API_KEY="sk-..."
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/..."

# Run with prod profile
java -jar target/linkedin-comment-responder.jar --spring.profiles.active=prod
```

### Example 3: Custom Configuration Override

```bash
# Override specific properties
java -jar app.jar \
  --spring.profiles.active=prod \
  --workflow.polling-interval-seconds=600 \
  --workflow.tone-preference=professional
```

### Example 4: Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/linkedin-comment-responder.jar app.jar

# Set default profile
ENV SPRING_PROFILES_ACTIVE=prod

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker run -d \
  -e LINKEDIN_CLIENT_ID="..." \
  -e LINKEDIN_CLIENT_SECRET="..." \
  -e LINKEDIN_ACCESS_TOKEN="..." \
  -e OPENAI_API_KEY="..." \
  -e SLACK_WEBHOOK_URL="..." \
  -p 8080:8080 \
  linkedin-responder:latest
```

## Troubleshooting

### Issue: Application fails to start with "Configuration validation failed"

**Solution**: Check the error message for specific missing parameters. Ensure all required environment variables are set.

```bash
# Verify environment variables are set
echo $LINKEDIN_CLIENT_ID
echo $OPENAI_API_KEY
```

### Issue: "Storage directory is not writable"

**Solution**: Ensure the application has write permissions to the storage directory.

```bash
# Create directory and set permissions
mkdir -p /var/lib/linkedin-responder/prod
chmod 755 /var/lib/linkedin-responder/prod
```

### Issue: Rate limiting errors

**Solution**: Increase polling interval or reduce rate limit parameter.

```properties
workflow.polling-interval-seconds=600
linkedin.api.rate-limit-per-minute=50
```

### Issue: LLM responses are truncated

**Solution**: Increase max tokens parameter.

```properties
llm.max-tokens=1000
```

### Issue: Notifications not working

**Solution**: Verify notification channel configuration and credentials.

```bash
# For Slack, test webhook
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"Test message"}' \
  $SLACK_WEBHOOK_URL
```

### Issue: Circuit breaker opening frequently

**Solution**: Adjust circuit breaker thresholds or investigate underlying service issues.

```properties
circuit-breaker.failure-threshold=10
circuit-breaker.reset-timeout-ms=120000
```

## Best Practices

1. **Never commit credentials**: Always use environment variables for sensitive data
2. **Start with dev profile**: Test configuration in development before production
3. **Enable manual approval initially**: Review generated responses before automatic posting
4. **Monitor rate limits**: Adjust polling intervals based on API tier
5. **Configure error notifications**: Set up Slack or email for production monitoring
6. **Regular backups**: Backup interaction history and processed comments files
7. **Log rotation**: Configure appropriate log retention for your environment
8. **Test circuit breakers**: Verify resilience by simulating service failures

## Additional Resources

- [LinkedIn API Documentation](https://docs.microsoft.com/en-us/linkedin/)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
