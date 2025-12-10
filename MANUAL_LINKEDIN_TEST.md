# Manual LinkedIn API Test

This guide shows you how to manually test your LinkedIn API integration using your actual credentials.

## Step 1: Set Your LinkedIn Credentials

You need to set these environment variables with your actual LinkedIn app credentials:

```bash
# Replace with your actual LinkedIn app credentials
export LINKEDIN_CLIENT_ID="your-actual-client-id"
export LINKEDIN_CLIENT_SECRET="your-actual-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-actual-access-token"

# Optional: If you have a refresh token
export LINKEDIN_REFRESH_TOKEN="your-refresh-token"
```

## Step 2: Start the Application

```bash
# Start with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Wait for the application to start (you'll see "Started LinkedInCommentResponderApplication" in the logs).

## Step 3: Test Your LinkedIn Profile

Open a new terminal and run these tests:

### Test 1: Check Configuration
```bash
curl http://localhost:8080/api/test/config
```

**Expected Response:**
```json
{
  "success": true,
  "message": "LinkedIn API is configured",
  "data": {
    "hasAccessToken": true,
    "hasClientId": true,
    "accessTokenLength": 150,
    "accessTokenPreview": "AQVmXyZ123...",
    "apiBaseUrl": "https://api.linkedin.com/v2",
    "timestamp": 1703875200000
  }
}
```

### Test 2: Get Your Profile
```bash
curl http://localhost:8080/api/test/profile
```

**Expected Response (Success):**
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

**Expected Response (Error):**
```json
{
  "success": false,
  "message": "Invalid or expired access token",
  "errorCode": "LINKEDIN_API_ERROR",
  "data": {
    "apiTestStatus": "FAILED",
    "tokenValid": false,
    "statusCode": 401,
    "timestamp": 1703875200000
  }
}
```

### Test 3: Test with Custom Token
```bash
curl -X POST http://localhost:8080/api/test/profile/custom \
  -H "Content-Type: application/json" \
  -d '{"accessToken": "your-different-token-here"}'
```

## Step 4: Automated Test Script

For convenience, you can also use the automated test script:

```bash
# Set your credentials first
export LINKEDIN_CLIENT_ID="your-actual-client-id"
export LINKEDIN_CLIENT_SECRET="your-actual-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-actual-access-token"

# Run the automated test
./test-linkedin-api.sh
```

## Getting Your LinkedIn Credentials

### Option 1: LinkedIn Developer Console

1. Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
2. Sign in and go to "My Apps"
3. Select your app (or create one)
4. Go to the **Auth** tab
5. Copy your **Client ID** and **Client Secret**

### Option 2: Generate Access Token

1. In your LinkedIn app, go to **Auth** tab
2. Use the **OAuth 2.0 tools** section
3. Select scopes: `r_liteprofile`, `r_emailaddress`
4. Click **Request access token**
5. Copy the generated access token

## Troubleshooting

### 401 Unauthorized
- Your access token is invalid or expired
- Generate a new access token from LinkedIn Developer Console
- Check that your app has the required permissions

### 403 Forbidden
- Your LinkedIn app doesn't have the required API products enabled
- Go to your app's **Products** tab and request access to:
  - Sign In with LinkedIn
  - Share on LinkedIn (if needed)

### 429 Rate Limited
- You've made too many requests
- Wait a few minutes before trying again
- LinkedIn allows 100 requests per 60 seconds

### Connection Refused
- The Spring Boot application isn't running
- Check that you see "Started LinkedInCommentResponderApplication" in the logs
- Verify the application is running on port 8080

### Configuration Not Found
- Environment variables aren't set
- Make sure you exported the variables in the same terminal session
- Check with: `echo $LINKEDIN_CLIENT_ID`

## What This Test Validates

✅ **LinkedIn API Connectivity** - Can reach LinkedIn's servers  
✅ **Authentication** - Your access token is valid  
✅ **Permissions** - Your app has the right scopes  
✅ **Profile Access** - Can read your basic profile information  
✅ **Application Setup** - Spring Boot app is configured correctly  

## Next Steps

Once your profile test is successful:

1. ✅ Your LinkedIn integration is working
2. ✅ You can proceed to test comment monitoring
3. ✅ Set up the full workflow for automated responses

## Security Note

⚠️ **Never commit your actual credentials to Git!**

The application is configured to use environment variables for security. Your actual credentials should only be set as environment variables, never hardcoded in files.