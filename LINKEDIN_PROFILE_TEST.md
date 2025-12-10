# LinkedIn Profile Test Guide

This guide helps you test your LinkedIn API connectivity and verify your profile information.

## Quick Test

### 1. Start the Application

```bash
# Set your LinkedIn access token
export LINKEDIN_ACCESS_TOKEN="your-linkedin-access-token-here"

# Start the application
mvn spring-boot:run
```

### 2. Test Your Profile

```bash
# Test with configured token
curl http://localhost:8080/api/test/profile
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": "your-linkedin-id",
    "firstName": "Your",
    "lastName": "Name",
    "profilePicture": "https://media.licdn.com/...",
    "apiTestStatus": "SUCCESS",
    "tokenValid": true,
    "timestamp": 1703875200000
  }
}
```

### 3. Test with Custom Token

```bash
curl -X POST http://localhost:8080/api/test/profile/custom \
  -H "Content-Type: application/json" \
  -d '{
    "accessToken": "your-different-token-here"
  }'
```

### 4. Check Configuration

```bash
curl http://localhost:8080/api/test/config
```

## Getting Your LinkedIn Access Token

### Option 1: Quick Test Token (Development)

1. Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
2. Create or select your app
3. Go to **Auth** tab
4. Use the **OAuth 2.0 tools** to generate a test token
5. Copy the access token

### Option 2: Full OAuth Flow

See [CONFIGURATION.md](CONFIGURATION.md) for complete OAuth setup instructions.

## Common Issues

### 401 Unauthorized

**Problem**: Invalid or expired access token

**Solutions**:
- Generate a new access token
- Check token expiration (LinkedIn tokens expire after 60 days)
- Verify the token has correct scopes

### 403 Forbidden

**Problem**: Insufficient API permissions

**Solutions**:
- Check your LinkedIn app has required API products enabled
- Verify your app is approved for the necessary scopes
- Ensure you have the right permissions for profile access

### 429 Rate Limited

**Problem**: Too many API requests

**Solutions**:
- Wait a few minutes before trying again
- Check if you're making too many requests
- Verify your rate limiting configuration

## Required LinkedIn API Scopes

For the profile test to work, your access token needs these scopes:

- `r_liteprofile` - Read basic profile information
- `r_emailaddress` - Read email address (optional)

## Testing Different Scenarios

### Test Invalid Token

```bash
curl -X POST http://localhost:8080/api/test/profile/custom \
  -H "Content-Type: application/json" \
  -d '{"accessToken": "invalid-token"}'
```

**Expected**: 401 Unauthorized

### Test Empty Token

```bash
curl -X POST http://localhost:8080/api/test/profile/custom \
  -H "Content-Type: application/json" \
  -d '{"accessToken": ""}'
```

**Expected**: 400 Bad Request

### Test Configuration Status

```bash
curl http://localhost:8080/api/test/config
```

**Shows**:
- Whether access token is configured
- Token length (without exposing the actual token)
- Configuration status

## Using in Browser

You can also test these endpoints in your browser:

1. **Configuration Status**: http://localhost:8080/api/test/config
2. **Profile Test**: http://localhost:8080/api/test/profile
3. **Swagger UI**: http://localhost:8080/swagger-ui.html

## Integration with Main Workflow

Once your profile test is successful, you can use the same token for the main comment responder workflow:

```bash
# Start monitoring a post
curl -X POST http://localhost:8080/api/management/polling/start \
  -H "Content-Type: application/json" \
  -d '{
    "postId": "urn:li:share:YOUR_POST_ID",
    "pollingIntervalSeconds": 300,
    "requireManualApproval": true,
    "tonePreference": "witty"
  }'
```

## Troubleshooting

### Application Won't Start

Check that you have Java 17+ and Maven installed:

```bash
java -version
mvn -version
```

### Profile Test Returns Empty Data

- Verify your LinkedIn profile is complete
- Check that your app has the right API products enabled
- Ensure your access token has the required scopes

### Token Keeps Expiring

LinkedIn access tokens expire after 60 days. For production use:

1. Implement token refresh logic
2. Store refresh tokens securely
3. Set up automatic token renewal

See the `LinkedInApiClient.java` class for token refresh implementation.

## Next Steps

Once your profile test is working:

1. ✅ Your LinkedIn API connectivity is confirmed
2. ✅ Your access token is valid
3. ✅ You can proceed to test comment monitoring
4. ✅ Set up the full workflow for automated responses

Happy testing! 🚀