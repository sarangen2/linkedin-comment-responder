# LinkedIn OAuth 2.0 Setup Guide

This guide shows you how to set up LinkedIn OAuth 2.0 authentication to automatically generate and cache access tokens for 2 months.

## 🎯 Overview

Instead of manually managing LinkedIn access tokens, this application now includes:

- ✅ **Automatic OAuth 2.0 flow** - Generate tokens via web browser
- ✅ **Token caching** - Tokens cached for 2 months (60 days)
- ✅ **Automatic refresh** - Tokens refreshed automatically when needed
- ✅ **Persistent storage** - Tokens saved to disk and survive app restarts
- ✅ **Web interface** - Easy browser-based authentication

## 📋 Prerequisites

You only need your LinkedIn **Client ID** and **Client Secret** (no access token required).

## 🚀 Quick Setup

### Step 1: Configure Your LinkedIn App

1. Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
2. Select your app (or create one)
3. Go to **Auth** tab
4. Add this redirect URL:
   ```
   http://localhost:8080/auth/linkedin/callback
   ```
5. Note your **Client ID** and **Client Secret**

### Step 2: Set Environment Variables

```bash
export LINKEDIN_CLIENT_ID="your-linkedin-client-id"
export LINKEDIN_CLIENT_SECRET="your-linkedin-client-secret"
```

### Step 3: Start the Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Step 4: Authenticate via Browser

1. **Get authorization URL:**
   ```bash
   curl http://localhost:8080/auth/linkedin/authorize
   ```

2. **Visit the URL in your browser** (or go directly to):
   ```
   http://localhost:8080/auth/linkedin/authorize
   ```

3. **Follow the OAuth flow:**
   - Click the LinkedIn authorization URL
   - Sign in to LinkedIn
   - Authorize your application
   - You'll be redirected back with a success message

4. **Test your profile:**
   ```bash
   curl http://localhost:8080/api/test/profile
   ```

## 🔄 Complete OAuth Flow

### 1. Get Authorization URL

**GET** `/auth/linkedin/authorize`

```bash
curl http://localhost:8080/auth/linkedin/authorize
```

**Response:**
```json
{
  "success": true,
  "message": "Authorization URL generated",
  "data": {
    "authorizationUrl": "https://www.linkedin.com/oauth/v2/authorization?...",
    "instructions": "Visit this URL in your browser to authorize the application",
    "callbackUrl": "http://localhost:8080/auth/linkedin/callback"
  }
}
```

### 2. Visit Authorization URL

Open the `authorizationUrl` in your browser:
- Sign in to LinkedIn
- Review permissions
- Click "Allow" to authorize

### 3. Automatic Token Exchange

The app automatically:
- Receives the authorization code
- Exchanges it for an access token
- Caches the token to disk
- Shows a success page

### 4. Token is Ready!

Your access token is now cached and ready to use:

```bash
curl http://localhost:8080/api/test/profile
```

## 📊 Token Management

### Check Token Status

**GET** `/auth/linkedin/status`

```bash
curl http://localhost:8080/auth/linkedin/status
```

**Response:**
```json
{
  "success": true,
  "message": "Token is available",
  "data": {
    "hasToken": true,
    "isExpired": false,
    "expiresAt": 1709251200,
    "createdAt": 1703875200,
    "scope": "r_liteprofile r_emailaddress w_member_social",
    "hasRefreshToken": true,
    "authRequired": false
  }
}
```

### Clear Token (Logout)

**DELETE** `/auth/linkedin/token`

```bash
curl -X DELETE http://localhost:8080/auth/linkedin/token
```

## 🔧 Configuration

### Token Cache Location

Tokens are cached in: `./data/linkedin-token.json`

You can customize this location:

```properties
# application-dev.properties
linkedin.oauth.token-cache-file=./data/linkedin-token.json
```

### Required LinkedIn Scopes

The application requests these scopes:
- `r_liteprofile` - Read basic profile information
- `r_emailaddress` - Read email address  
- `w_member_social` - Post comments and interact with posts

## 🛠️ API Endpoints

### OAuth Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/linkedin/authorize` | GET | Get authorization URL |
| `/auth/linkedin/callback` | GET | OAuth callback (automatic) |
| `/auth/linkedin/status` | GET | Check token status |
| `/auth/linkedin/token` | DELETE | Clear cached token |

### Profile Test Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/test/profile` | GET | Test profile with OAuth token |
| `/api/test/config` | GET | Check configuration status |
| `/api/test/profile/custom` | POST | Test with custom token |

## 🔍 Testing Your Setup

### 1. Check Configuration

```bash
curl http://localhost:8080/api/test/config
```

Should show:
```json
{
  "success": true,
  "message": "LinkedIn API is configured",
  "data": {
    "hasOAuthToken": true,
    "hasConfiguredToken": false,
    "hasClientId": true,
    "hasClientSecret": true,
    "oauthTokenStatus": {
      "hasToken": true,
      "isExpired": false
    }
  }
}
```

### 2. Test Profile Access

```bash
curl http://localhost:8080/api/test/profile
```

Should return your LinkedIn profile:
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": "your-linkedin-id",
    "firstName": "Your",
    "lastName": "Name",
    "tokenSource": "OAUTH",
    "apiTestStatus": "SUCCESS"
  }
}
```

## 🔄 Token Lifecycle

### Initial Setup
1. User visits authorization URL
2. LinkedIn redirects with authorization code
3. App exchanges code for access token
4. Token cached to disk and memory

### Daily Usage
1. App checks cached token
2. If valid, uses cached token
3. If expired, attempts refresh
4. If refresh fails, requires re-authorization

### Token Expiry
- **LinkedIn tokens last ~60 days**
- **App checks expiry before each use**
- **Automatic refresh when possible**
- **Re-authorization required if refresh fails**

## 🚨 Troubleshooting

### "No access token available - OAuth required"

**Solution**: Run the OAuth flow:
```bash
curl http://localhost:8080/auth/linkedin/authorize
# Visit the returned URL in your browser
```

### "Invalid redirect URI"

**Problem**: LinkedIn app redirect URI doesn't match

**Solution**: In LinkedIn Developer Console, set redirect URI to:
```
http://localhost:8080/auth/linkedin/callback
```

### "Insufficient permissions"

**Problem**: LinkedIn app doesn't have required API products

**Solution**: 
1. Go to LinkedIn Developer Console
2. Click **Products** tab
3. Request access to:
   - Sign In with LinkedIn
   - Share on LinkedIn
   - Marketing Developer Platform (if needed)

### Token file not found

**Problem**: `./data/linkedin-token.json` doesn't exist

**Solution**: This is normal on first run. Complete the OAuth flow to create it.

### App can't write token file

**Problem**: Permission denied writing to `./data/`

**Solution**:
```bash
mkdir -p ./data
chmod 755 ./data
```

## 🎉 Success!

Once OAuth is set up:

✅ **No more manual token management**  
✅ **Tokens automatically refresh**  
✅ **2-month token lifetime**  
✅ **Persistent across app restarts**  
✅ **Ready for production use**  

Your LinkedIn Comment Responder is now fully authenticated and ready to use!

## 🔗 Next Steps

1. **Test comment monitoring**: Set up a LinkedIn post to monitor
2. **Configure LLM**: Add your OpenAI API key
3. **Start the workflow**: Begin automated comment responses

See [README.md](README.md) for complete usage instructions.