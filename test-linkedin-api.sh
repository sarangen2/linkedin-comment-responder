#!/bin/bash

echo "=========================================="
echo "LinkedIn OAuth API Test Script"
echo "=========================================="
echo ""

# Check if credentials are set
if [ -z "$LINKEDIN_CLIENT_ID" ]; then
    echo "❌ LINKEDIN_CLIENT_ID is not set"
    echo "Please set it with: export LINKEDIN_CLIENT_ID='your-client-id'"
    exit 1
fi

if [ -z "$LINKEDIN_CLIENT_SECRET" ]; then
    echo "❌ LINKEDIN_CLIENT_SECRET is not set"
    echo "Please set it with: export LINKEDIN_CLIENT_SECRET='your-client-secret'"
    exit 1
fi

echo "✅ LinkedIn credentials are set:"
echo "   Client ID: ${LINKEDIN_CLIENT_ID:0:10}..."
echo "   Client Secret: ${LINKEDIN_CLIENT_SECRET:0:10}..."
echo ""

echo "Starting Spring Boot application..."
echo "This will take a few seconds..."
echo ""

# Start the application in the background
mvn spring-boot:run -Dspring-boot.run.profiles=dev > app.log 2>&1 &
APP_PID=$!

echo "Application started with PID: $APP_PID"
echo "Waiting for application to be ready..."

# Wait for the application to start
sleep 15

# Check if the application is running
if ! kill -0 $APP_PID 2>/dev/null; then
    echo "❌ Application failed to start. Check app.log for details:"
    tail -20 app.log
    exit 1
fi

# Test if the application is responding
echo "Testing application health..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is healthy"
else
    echo "⚠️  Health check failed, but continuing with API test..."
fi

echo ""
echo "=========================================="
echo "Testing LinkedIn API Endpoints"
echo "=========================================="
echo ""

# Test 1: Configuration Status
echo "1. Testing configuration status..."
echo "GET /api/test/config"
echo ""
curl -s http://localhost:8080/api/test/config | jq '.' 2>/dev/null || curl -s http://localhost:8080/api/test/config
echo ""
echo ""

# Test 2: OAuth Token Status
echo "2. Testing OAuth token status..."
echo "GET /auth/linkedin/status"
echo ""
curl -s http://localhost:8080/auth/linkedin/status | jq '.' 2>/dev/null || curl -s http://localhost:8080/auth/linkedin/status
echo ""
echo ""

# Test 3: Profile Test (will use OAuth token if available)
echo "3. Testing LinkedIn profile..."
echo "GET /api/test/profile"
echo ""
PROFILE_RESPONSE=$(curl -s http://localhost:8080/api/test/profile)
echo "$PROFILE_RESPONSE" | jq '.' 2>/dev/null || echo "$PROFILE_RESPONSE"

# Check if OAuth is required
if echo "$PROFILE_RESPONSE" | grep -q "OAuth required"; then
    echo ""
    echo "🔐 OAuth authentication required!"
    echo ""
    echo "Getting authorization URL..."
    AUTH_RESPONSE=$(curl -s http://localhost:8080/auth/linkedin/authorize)
    AUTH_URL=$(echo "$AUTH_RESPONSE" | jq -r '.data.authorizationUrl' 2>/dev/null)
    
    if [ "$AUTH_URL" != "null" ] && [ -n "$AUTH_URL" ]; then
        echo ""
        echo "=========================================="
        echo "🌐 OAUTH AUTHENTICATION REQUIRED"
        echo "=========================================="
        echo ""
        echo "Please visit this URL in your browser to authenticate:"
        echo ""
        echo "$AUTH_URL"
        echo ""
        echo "After authentication, run this script again to test your profile."
        echo ""
        echo "Or visit: http://localhost:8080/auth/linkedin/authorize"
        echo ""
    else
        echo "❌ Failed to get authorization URL"
    fi
fi

echo ""
echo ""

echo "=========================================="
echo "Test Complete"
echo "=========================================="
echo ""
echo "Stopping application..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null

echo "✅ LinkedIn OAuth API test completed!"
echo ""
echo "Results summary:"
echo "- If you see profile information above, your OAuth setup is working!"
echo "- If OAuth is required, visit the authorization URL shown above"
echo "- After OAuth, your token will be cached for 2 months"
echo ""
echo "Application logs are saved in: app.log"
echo ""
echo "Next steps:"
echo "1. Complete OAuth if required"
echo "2. Test profile access: curl http://localhost:8080/api/test/profile"
echo "3. Start using the LinkedIn Comment Responder!"