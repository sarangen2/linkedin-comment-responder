# Troubleshooting Guide

This guide provides solutions to common issues you may encounter when using the LinkedIn Comment Responder.

## Table of Contents

1. [Startup Issues](#startup-issues)
2. [Authentication & Authorization](#authentication--authorization)
3. [API & Rate Limiting](#api--rate-limiting)
4. [LLM & Response Generation](#llm--response-generation)
5. [Storage & Persistence](#storage--persistence)
6. [Workflow & Orchestration](#workflow--orchestration)
7. [Error Notifications](#error-notifications)
8. [Performance Issues](#performance-issues)
9. [Debugging Tips](#debugging-tips)

---

## Startup Issues

### Issue: Application fails to start with "Configuration validation failed"

**Symptoms**:
```
Configuration validation failed: Missing required parameter: linkedin.api.client-id
```

**Cause**: Required configuration parameters are missing or invalid.

**Solutions**:

1. **Verify environment variables are set**:
   ```bash
   echo $LINKEDIN_CLIENT_ID
   echo $LINKEDIN_CLIENT_SECRET
   echo $LINKEDIN_ACCESS_TOKEN
   echo $OPENAI_API_KEY
   ```

2. **Set missing variables**:
   ```bash
   export LINKEDIN_CLIENT_ID="your-client-id"
   export LINKEDIN_CLIENT_SECRET="your-client-secret"
   export LINKEDIN_ACCESS_TOKEN="your-access-token"
   export OPENAI_API_KEY="your-api-key"
   ```

3. **Check for placeholder values**:
   - Ensure you're not using placeholder values like "your-client-id"
   - Replace all placeholders with actual credentials

4. **Verify application.properties**:
   - Check that property names are spelled correctly
   - Ensure environment variable substitution syntax is correct: `${VARIABLE_NAME}`

---

### Issue: "Storage directory is not writable"

**Symptoms**:
```
Configuration validation failed: Storage directory is not writable: /var/lib/linkedin-responder
```

**Cause**: Application doesn't have write permissions to the storage directory.

**Solutions**:

1. **Create directory with proper permissions**:
   ```bash
   mkdir -p /var/lib/linkedin-responder
   chmod 755 /var/lib/linkedin-responder
   ```

2. **Change ownership** (if running as specific user):
   ```bash
   sudo chown -R appuser:appuser /var/lib/linkedin-responder
   ```

3. **Use a different directory**:
   ```properties
   storage.directory=./data
   ```

4. **For Docker**, mount a volume:
   ```bash
   docker run -v /host/path:/app/data linkedin-responder
   ```

---

### Issue: Port 8080 already in use

**Symptoms**:
```
Web server failed to start. Port 8080 was already in use.
```

**Solutions**:

1. **Use a different port**:
   ```bash
   java -jar app.jar --server.port=8081
   ```

2. **Find and kill the process using port 8080**:
   ```bash
   # Find process
   lsof -i :8080
   
   # Kill process
   kill -9 <PID>
   ```

3. **Set port in application.properties**:
   ```properties
   server.port=8081
   ```

---

## Authentication & Authorization

### Issue: "401 Unauthorized" from LinkedIn API

**Symptoms**:
```
LinkedIn API error: 401 Unauthorized
```

**Causes & Solutions**:

1. **Access token expired**:
   - LinkedIn access tokens expire after 60 days
   - **Solution**: Generate a new access token using the OAuth flow
   - See [LinkedIn OAuth Setup](README.md#-linkedin-oauth-setup)

2. **Invalid access token**:
   - Token may be malformed or incorrect
   - **Solution**: Verify token by testing directly:
     ```bash
     curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
       https://api.linkedin.com/v2/me
     ```

3. **Token refresh failed**:
   - Check logs for refresh token errors
   - **Solution**: Provide a valid refresh token:
     ```bash
     export LINKEDIN_REFRESH_TOKEN="your-refresh-token"
     ```

---

### Issue: "403 Forbidden" from LinkedIn API

**Symptoms**:
```
LinkedIn API error: 403 Forbidden - Insufficient permissions
```

**Causes & Solutions**:

1. **Missing API product access**:
   - Your LinkedIn app doesn't have required API products
   - **Solution**: 
     - Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
     - Navigate to your app → Products tab
     - Request access to required products:
       - Sign In with LinkedIn
       - Share on LinkedIn
       - Marketing Developer Platform

2. **Insufficient scopes**:
   - Access token doesn't have required permissions
   - **Solution**: Regenerate token with correct scopes:
     ```
     r_liteprofile r_emailaddress w_member_social rw_organization_admin
     ```

3. **App not verified**:
   - LinkedIn requires verification for certain API access
   - **Solution**: Complete LinkedIn's app verification process
   - This can take 3-5 business days

---

### Issue: Token refresh not working

**Symptoms**:
```
Failed to refresh access token: invalid_grant
```

**Solutions**:

1. **Verify refresh token is valid**:
   - Refresh tokens expire after 1 year
   - Generate a new refresh token if expired

2. **Check client credentials**:
   ```bash
   echo $LINKEDIN_CLIENT_ID
   echo $LINKEDIN_CLIENT_SECRET
   ```

3. **Manual token refresh**:
   ```bash
   curl -X POST https://www.linkedin.com/oauth/v2/accessToken \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=refresh_token" \
     -d "refresh_token=YOUR_REFRESH_TOKEN" \
     -d "client_id=YOUR_CLIENT_ID" \
     -d "client_secret=YOUR_CLIENT_SECRET"
   ```

---

## API & Rate Limiting

### Issue: "429 Too Many Requests" from LinkedIn API

**Symptoms**:
```
LinkedIn API error: 429 Too Many Requests - Rate limit exceeded
```

**Causes & Solutions**:

1. **Polling interval too short**:
   - **Solution**: Increase polling interval:
     ```properties
     workflow.polling-interval-seconds=600
     ```

2. **Rate limit configuration too high**:
   - **Solution**: Reduce rate limit to match your LinkedIn API tier:
     ```properties
     linkedin.api.rate-limit-per-minute=50
     ```

3. **Multiple instances running**:
   - Check if multiple instances are polling the same post
   - **Solution**: Coordinate polling across instances or use a shared rate limiter

4. **Temporary rate limit**:
   - LinkedIn may temporarily reduce limits
   - **Solution**: Wait and let the circuit breaker handle it automatically
   - Check logs for automatic backoff behavior

---

### Issue: Circuit breaker opening frequently

**Symptoms**:
```
Circuit breaker is OPEN for LinkedInApiClient - too many failures
```

**Causes & Solutions**:

1. **Underlying service issues**:
   - LinkedIn API may be experiencing problems
   - **Solution**: Check [LinkedIn API Status](https://www.linkedin-apistatus.com/)

2. **Network connectivity issues**:
   - **Solution**: Test connectivity:
     ```bash
     curl https://api.linkedin.com/v2/me
     ```

3. **Circuit breaker too sensitive**:
   - **Solution**: Adjust thresholds:
     ```properties
     circuit-breaker.failure-threshold=10
     circuit-breaker.reset-timeout-ms=120000
     ```

4. **Authentication failures**:
   - Repeated auth failures trigger circuit breaker
   - **Solution**: Fix authentication issues first (see above)

---

### Issue: Slow API responses

**Symptoms**:
- Requests taking longer than expected
- Timeout errors

**Solutions**:

1. **Increase timeout values**:
   ```properties
   linkedin.api.timeout-seconds=30
   ```

2. **Check network latency**:
   ```bash
   ping api.linkedin.com
   ```

3. **Enable connection pooling**:
   - Already enabled by default in WebClient
   - Verify configuration in logs

4. **Monitor LinkedIn API performance**:
   - Check if LinkedIn is experiencing slowdowns
   - Consider implementing caching for frequently accessed data

---

## LLM & Response Generation

### Issue: LLM responses are inappropriate or off-topic

**Symptoms**:
- Responses don't match the post theme
- Tone is inconsistent
- Responses are irrelevant

**Solutions**:

1. **Enable manual approval**:
   ```properties
   workflow.require-manual-approval=true
   ```

2. **Adjust tone preference**:
   ```properties
   workflow.tone-preference=professional
   ```

3. **Lower temperature for more conservative responses**:
   ```properties
   llm.temperature=0.3
   ```

4. **Use a more capable model**:
   ```properties
   llm.model=gpt-4
   ```

5. **Add manual review keywords**:
   ```properties
   workflow.manual-review-keywords=urgent,complaint,legal,refund,angry
   ```

---

### Issue: LLM responses are truncated

**Symptoms**:
- Responses cut off mid-sentence
- Incomplete thoughts

**Solutions**:

1. **Increase max tokens**:
   ```properties
   llm.max-tokens=1000
   ```

2. **Check LinkedIn character limit**:
   - LinkedIn comments have a 1250 character limit
   - The system enforces this automatically

3. **Review prompt template**:
   - Ensure prompt doesn't consume too many tokens
   - Simplify post content if very long

---

### Issue: "OpenAI API error: 429 Rate limit exceeded"

**Symptoms**:
```
OpenAI API error: 429 - Rate limit exceeded
```

**Solutions**:

1. **Check your OpenAI usage limits**:
   - Visit [OpenAI Usage Dashboard](https://platform.openai.com/usage)
   - Verify you haven't exceeded your quota

2. **Upgrade OpenAI plan**:
   - Consider upgrading to a higher tier

3. **Reduce polling frequency**:
   ```properties
   workflow.polling-interval-seconds=600
   ```

4. **Use a different model**:
   ```properties
   llm.model=gpt-3.5-turbo
   ```

---

### Issue: "OpenAI API error: 401 Invalid API key"

**Symptoms**:
```
OpenAI API error: 401 - Invalid API key
```

**Solutions**:

1. **Verify API key**:
   ```bash
   echo $OPENAI_API_KEY
   ```

2. **Test API key directly**:
   ```bash
   curl https://api.openai.com/v1/models \
     -H "Authorization: Bearer $OPENAI_API_KEY"
   ```

3. **Generate new API key**:
   - Go to [OpenAI API Keys](https://platform.openai.com/api-keys)
   - Create a new key and update environment variable

4. **Check for whitespace**:
   - Ensure no leading/trailing spaces in API key
   ```bash
   export OPENAI_API_KEY=$(echo $OPENAI_API_KEY | tr -d '[:space:]')
   ```

---

### Issue: LLM timeout errors

**Symptoms**:
```
LLM request timed out after 30 seconds
```

**Solutions**:

1. **Increase timeout**:
   ```properties
   llm.timeout-seconds=60
   ```

2. **Reduce max tokens**:
   ```properties
   llm.max-tokens=300
   ```

3. **Use faster model**:
   ```properties
   llm.model=gpt-3.5-turbo
   ```

4. **Check OpenAI status**:
   - Visit [OpenAI Status](https://status.openai.com/)

---

## Storage & Persistence

### Issue: "Failed to save interaction: Disk full"

**Symptoms**:
```
IOException: No space left on device
```

**Solutions**:

1. **Check disk space**:
   ```bash
   df -h
   ```

2. **Clean up old archives**:
   ```bash
   rm -rf ./data/archive/*
   ```

3. **Reduce storage capacity**:
   ```properties
   storage.max.capacity=500
   ```

4. **Enable automatic archival**:
   - Already enabled by default
   - Verify archival is working in logs

5. **Use external storage**:
   - Mount a larger volume
   - Configure storage directory to use mounted volume

---

### Issue: Interaction history not persisting

**Symptoms**:
- History is empty after restart
- Interactions disappear

**Solutions**:

1. **Check file permissions**:
   ```bash
   ls -la ./data/
   ```

2. **Verify storage directory**:
   ```bash
   cat logs/application.log | grep "storage.directory"
   ```

3. **Check for write errors in logs**:
   ```bash
   cat logs/application.log | grep "Failed to save"
   ```

4. **Manually verify files exist**:
   ```bash
   ls -la ./data/interactions.json
   ls -la ./data/processed-comments.json
   ```

---

### Issue: JSON parsing errors

**Symptoms**:
```
Failed to parse interactions file: Unexpected character
```

**Solutions**:

1. **Backup and reset**:
   ```bash
   mv ./data/interactions.json ./data/interactions.json.backup
   # Application will create new file on next save
   ```

2. **Validate JSON manually**:
   ```bash
   cat ./data/interactions.json | jq .
   ```

3. **Check for corruption**:
   - File may be corrupted due to incomplete write
   - Restore from backup if available

4. **Enable file locking**:
   - Already implemented in FileBasedStorageRepository
   - Verify no external processes are modifying files

---

## Workflow & Orchestration

### Issue: Polling not starting

**Symptoms**:
- POST to `/polling/start` returns 200 but nothing happens
- No comments being processed

**Solutions**:

1. **Check polling status**:
   ```bash
   curl http://localhost:8080/api/management/polling/status
   ```

2. **Verify post ID format**:
   - Must be in format: `urn:li:share:1234567890`
   - Check LinkedIn post URL for correct ID

3. **Check logs for errors**:
   ```bash
   tail -f logs/application.log
   ```

4. **Verify scheduled tasks are enabled**:
   - Check for `@EnableScheduling` annotation
   - Verify Spring scheduling is working

---

### Issue: Comments not being detected

**Symptoms**:
- Polling is active but no comments are processed
- Known comments are not appearing

**Solutions**:

1. **Verify post has comments**:
   - Check LinkedIn directly
   - Ensure comments are public

2. **Check API permissions**:
   - Verify your app can read comments
   - Test API access manually:
     ```bash
     curl -H "Authorization: Bearer $LINKEDIN_ACCESS_TOKEN" \
       "https://api.linkedin.com/v2/socialActions/urn:li:share:POST_ID/comments"
     ```

3. **Check processed comments filter**:
   - Comments may already be marked as processed
   - View processed comments file:
     ```bash
     cat ./data/processed-comments.json
     ```

4. **Clear processed comments** (for testing):
   ```bash
   echo "[]" > ./data/processed-comments.json
   ```

---

### Issue: Responses not being posted

**Symptoms**:
- Responses generated but not appearing on LinkedIn
- "Successfully posted" in logs but nothing on LinkedIn

**Solutions**:

1. **Verify posting permissions**:
   - Check your app has `w_member_social` scope
   - Test posting manually via API

2. **Check manual approval mode**:
   - If enabled, responses need approval
   ```bash
   curl http://localhost:8080/api/management/approval/pending
   ```

3. **Review error logs**:
   ```bash
   cat logs/application.log | grep "Failed to post"
   ```

4. **Verify comment ID format**:
   - Must be valid LinkedIn comment URN
   - Check logs for comment IDs being used

---

### Issue: Duplicate responses being posted

**Symptoms**:
- Same response posted multiple times
- Comments processed more than once

**Solutions**:

1. **Check processed comments tracking**:
   ```bash
   cat ./data/processed-comments.json
   ```

2. **Verify only one instance is running**:
   ```bash
   ps aux | grep linkedin-comment-responder
   ```

3. **Check for race conditions**:
   - Review logs for concurrent processing
   - Ensure file locking is working

4. **Clear and restart**:
   ```bash
   curl -X POST http://localhost:8080/api/management/polling/stop
   # Wait a few seconds
   curl -X POST http://localhost:8080/api/management/polling/start ...
   ```

---

## Error Notifications

### Issue: Not receiving error notifications

**Symptoms**:
- Errors occurring but no notifications
- Slack/email notifications not working

**Solutions**:

1. **Verify notifications are enabled**:
   ```properties
   error.notification.enabled=true
   ```

2. **Check notification channel**:
   ```properties
   error.notification.channel=slack
   ```

3. **For Slack notifications**:
   - Test webhook manually:
     ```bash
     curl -X POST -H 'Content-type: application/json' \
       --data '{"text":"Test message"}' \
       $SLACK_WEBHOOK_URL
     ```
   - Verify webhook URL is correct
   - Check Slack app permissions

4. **For email notifications**:
   - Verify SMTP configuration
   - Check email address is correct
   - Review mail server logs

5. **Check error severity**:
   - Only critical errors trigger notifications by default
   - Lower threshold if needed:
     ```properties
     error.notification.min-severity=WARN
     ```

---

### Issue: Too many error notifications

**Symptoms**:
- Notification spam
- Same error notified repeatedly

**Solutions**:

1. **Enable notification throttling**:
   ```properties
   error.notification.throttle-minutes=60
   ```

2. **Increase severity threshold**:
   ```properties
   error.notification.min-severity=ERROR
   ```

3. **Fix underlying errors**:
   - Address root cause instead of suppressing notifications
   - Review logs to identify recurring issues

---

## Performance Issues

### Issue: High memory usage

**Symptoms**:
- Application using excessive memory
- OutOfMemoryError

**Solutions**:

1. **Increase JVM heap size**:
   ```bash
   java -Xmx2g -jar app.jar
   ```

2. **Reduce storage capacity**:
   ```properties
   storage.max.capacity=500
   ```

3. **Enable archival**:
   - Ensure old interactions are being archived
   - Check archive directory size

4. **Profile memory usage**:
   ```bash
   java -XX:+HeapDumpOnOutOfMemoryError -jar app.jar
   ```

---

### Issue: High CPU usage

**Symptoms**:
- CPU constantly at 100%
- Application slow to respond

**Solutions**:

1. **Increase polling interval**:
   ```properties
   workflow.polling-interval-seconds=600
   ```

2. **Check for infinite loops**:
   - Review logs for repeated errors
   - Look for retry storms

3. **Profile CPU usage**:
   ```bash
   jstack <PID> > thread-dump.txt
   ```

4. **Reduce concurrent operations**:
   - Limit number of simultaneous API calls
   - Add delays between operations

---

## Debugging Tips

### Enable Debug Logging

```bash
# Run with debug profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or set logging level
java -jar app.jar --logging.level.com.example.linkedin=DEBUG
```

### View Logs

```bash
# Tail application logs
tail -f logs/application.log

# Search for errors
cat logs/application.log | grep ERROR

# Search for specific component
cat logs/application.log | grep WorkflowOrchestrator
```

### Test Individual Components

```bash
# Test LinkedIn API connection
curl -H "Authorization: Bearer $LINKEDIN_ACCESS_TOKEN" \
  https://api.linkedin.com/v2/me

# Test OpenAI API connection
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $OPENAI_API_KEY"

# Test application health
curl http://localhost:8080/actuator/health
```

### Check Configuration

```bash
# View effective configuration
curl http://localhost:8080/actuator/configprops

# View environment variables
curl http://localhost:8080/actuator/env
```

### Monitor Metrics

```bash
# View metrics
curl http://localhost:8080/actuator/metrics

# View specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Thread Dump

```bash
# Get thread dump
jstack <PID> > thread-dump.txt

# Or via actuator
curl http://localhost:8080/actuator/threaddump
```

### Heap Dump

```bash
# Generate heap dump
jmap -dump:format=b,file=heap-dump.hprof <PID>

# Analyze with jhat
jhat heap-dump.hprof
```

---

## Getting Additional Help

If you're still experiencing issues:

1. **Check the logs** in `logs/application.log`
2. **Review configuration** in [CONFIGURATION.md](CONFIGURATION.md)
3. **Consult API docs** in [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
4. **Open an issue** on GitHub with:
   - Error messages (with credentials redacted)
   - Configuration (with sensitive data removed)
   - Steps to reproduce
   - Environment details (OS, Java version, etc.)

---

**Last Updated**: December 2024
