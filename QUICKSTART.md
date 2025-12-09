# Quick Start Guide

Get the LinkedIn Comment Responder up and running in 10 minutes.

## Prerequisites Checklist

Before you begin, ensure you have:

- [ ] Java 17 or higher installed
- [ ] Maven 3.6+ installed
- [ ] LinkedIn Developer account
- [ ] OpenAI API account
- [ ] Internet connection

## Step 1: Verify Prerequisites

```bash
# Check Java version (should be 17+)
java -version

# Check Maven version (should be 3.6+)
mvn -version
```

If you don't have Java or Maven installed:
- **Java**: Download from [Adoptium](https://adoptium.net/)
- **Maven**: Download from [Apache Maven](https://maven.apache.org/download.cgi)

## Step 2: Get LinkedIn API Credentials

### 2.1 Create LinkedIn App

1. Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
2. Click **"Create app"**
3. Fill in:
   - App name: `LinkedIn Comment Responder`
   - LinkedIn Page: Select or create a page
   - Accept terms

### 2.2 Get Client Credentials

1. In your app dashboard, go to **"Auth"** tab
2. Copy your **Client ID** and **Client Secret**
3. Add redirect URL: `http://localhost:8080/auth/callback`

### 2.3 Request API Access

1. Go to **"Products"** tab
2. Request access to:
   - Sign In with LinkedIn
   - Share on LinkedIn
   - Marketing Developer Platform

⚠️ **Note**: API access may take 3-5 business days for approval.

### 2.4 Generate Access Token

**Quick method** (for testing):

1. Visit this URL (replace `YOUR_CLIENT_ID`):
   ```
   https://www.linkedin.com/oauth/v2/authorization?response_type=code&client_id=YOUR_CLIENT_ID&redirect_uri=http://localhost:8080/auth/callback&scope=r_liteprofile%20r_emailaddress%20w_member_social%20rw_organization_admin
   ```

2. Authorize the app and copy the `code` from the redirect URL

3. Exchange code for token:
   ```bash
   curl -X POST https://www.linkedin.com/oauth/v2/accessToken \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=authorization_code" \
     -d "code=YOUR_CODE" \
     -d "client_id=YOUR_CLIENT_ID" \
     -d "client_secret=YOUR_CLIENT_SECRET" \
     -d "redirect_uri=http://localhost:8080/auth/callback"
   ```

4. Save the `access_token` from the response

## Step 3: Get OpenAI API Key

1. Go to [OpenAI Platform](https://platform.openai.com/)
2. Sign up or log in
3. Navigate to **API Keys** section
4. Click **"Create new secret key"**
5. Copy and save the key (starts with `sk-`)

## Step 4: Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd linkedin-comment-responder

# Build the project
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30.123 s
```

## Step 5: Configure Environment Variables

```bash
# Set required credentials
export LINKEDIN_CLIENT_ID="your-client-id-here"
export LINKEDIN_CLIENT_SECRET="your-client-secret-here"
export LINKEDIN_ACCESS_TOKEN="your-access-token-here"
export OPENAI_API_KEY="sk-your-api-key-here"
```

**Tip**: Add these to your `~/.bashrc` or `~/.zshrc` to make them permanent:

```bash
echo 'export LINKEDIN_CLIENT_ID="your-client-id-here"' >> ~/.bashrc
echo 'export LINKEDIN_CLIENT_SECRET="your-client-secret-here"' >> ~/.bashrc
echo 'export LINKEDIN_ACCESS_TOKEN="your-access-token-here"' >> ~/.bashrc
echo 'export OPENAI_API_KEY="sk-your-api-key-here"' >> ~/.bashrc
source ~/.bashrc
```

## Step 6: Start the Application

```bash
# Start with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Wait for this message:
```
Started LinkedInCommentResponderApplication in X.XXX seconds
```

The application is now running at `http://localhost:8080`

## Step 7: Get Your LinkedIn Post ID

1. Go to LinkedIn and find the post you want to monitor
2. Click the three dots (•••) on the post
3. Select **"Copy link to post"**
4. Extract the post ID from the URL

**Example URL**:
```
https://www.linkedin.com/feed/update/urn:li:share:7123456789012345678/
```

**Post ID**:
```
urn:li:share:7123456789012345678
```

## Step 8: Start Monitoring

Open a new terminal and start monitoring your post:

```bash
curl -X POST http://localhost:8080/api/management/polling/start \
  -H "Content-Type: application/json" \
  -d '{
    "postId": "urn:li:share:YOUR_POST_ID_HERE",
    "pollingIntervalSeconds": 300,
    "requireManualApproval": true,
    "tonePreference": "witty"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Polling started successfully"
}
```

## Step 9: Test with a Comment

1. Go to your LinkedIn post
2. Add a test comment (or ask a friend to comment)
3. Wait up to 5 minutes for the system to detect it

## Step 10: Review and Approve Response

Check for pending responses:

```bash
curl http://localhost:8080/api/management/approval/pending
```

If a response is pending, you'll see:
```json
{
  "success": true,
  "data": {
    "commentId": "comment-123",
    "commenterName": "John Doe",
    "commentText": "Great post!",
    "generatedResponse": "Thanks John! Glad you enjoyed it! 😊",
    "confidenceScore": 0.95
  }
}
```

Approve the response:

```bash
curl -X POST http://localhost:8080/api/management/approval/decision \
  -H "Content-Type: application/json" \
  -d '{"approve": true}'
```

## Step 11: Verify on LinkedIn

1. Go back to your LinkedIn post
2. Check that the response was posted
3. Verify it looks good

## 🎉 Success!

You're now running the LinkedIn Comment Responder! The system will:
- Check for new comments every 5 minutes
- Generate contextually appropriate responses
- Wait for your approval before posting
- Keep a history of all interactions

## Next Steps

### View Interaction History

```bash
curl http://localhost:8080/api/management/history
```

### Stop Monitoring

```bash
curl -X POST http://localhost:8080/api/management/polling/stop
```

### Change Configuration

```bash
curl -X PATCH http://localhost:8080/api/management/config \
  -H "Content-Type: application/json" \
  -d '{
    "tonePreference": "professional",
    "pollingIntervalSeconds": 600
  }'
```

### Enable Automatic Posting

⚠️ **Warning**: Only enable this after you're confident in the response quality!

```bash
curl -X POST http://localhost:8080/api/management/polling/start \
  -H "Content-Type: application/json" \
  -d '{
    "postId": "urn:li:share:YOUR_POST_ID",
    "pollingIntervalSeconds": 300,
    "requireManualApproval": false,
    "tonePreference": "witty"
  }'
```

## Troubleshooting

### Application won't start

**Check environment variables**:
```bash
echo $LINKEDIN_CLIENT_ID
echo $OPENAI_API_KEY
```

If empty, set them again (see Step 5).

### "401 Unauthorized" error

Your LinkedIn access token may be invalid or expired.
- Verify token: `echo $LINKEDIN_ACCESS_TOKEN`
- Generate a new token (see Step 2.4)

### "429 Rate Limit" error

You're making too many API requests.
- Increase polling interval to 600 seconds
- Wait a few minutes and try again

### No comments detected

- Verify your post has comments
- Check the post ID is correct
- Ensure your LinkedIn app has API access approved

### Response not posted

- Check if manual approval is enabled
- Look for pending responses: `curl http://localhost:8080/api/management/approval/pending`
- Review logs: `tail -f logs/application.log`

## Common Commands

```bash
# Check status
curl http://localhost:8080/api/management/polling/status

# View history
curl http://localhost:8080/api/management/history

# Export history
curl -O http://localhost:8080/api/management/history/export?format=json

# Stop polling
curl -X POST http://localhost:8080/api/management/polling/stop

# View logs
tail -f logs/application.log

# Stop application
# Press Ctrl+C in the terminal running the application
```

## Interactive API Documentation

Once running, visit:
```
http://localhost:8080/swagger-ui.html
```

This provides a web interface to test all API endpoints.

## Getting Help

- **Full Documentation**: See [README.md](README.md)
- **Configuration Guide**: See [CONFIGURATION.md](CONFIGURATION.md)
- **API Reference**: See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- **Troubleshooting**: See [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- **Examples**: See [EXAMPLES.md](EXAMPLES.md)

## Tips for Success

1. **Start with manual approval** - Review responses before enabling automatic posting
2. **Test with a low-traffic post** - Get comfortable with the system first
3. **Monitor the logs** - Watch for errors or issues
4. **Adjust the tone** - Experiment with different tone preferences
5. **Set up notifications** - Configure Slack or email alerts for production use

## Security Reminders

- ✅ Never commit credentials to version control
- ✅ Use environment variables for sensitive data
- ✅ Rotate API keys regularly
- ✅ Monitor API usage and costs
- ✅ Review generated responses before automatic posting

---

**Congratulations!** You've successfully set up the LinkedIn Comment Responder. Happy automating! 🚀
