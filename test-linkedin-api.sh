#!/bin/bash

echo "=========================================="
echo "LinkedIn API Test Script"
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

if [ -z "$LINKEDIN_ACCESS_TOKEN" ]; then
    echo "❌ LINKEDIN_ACCESS_TOKEN is not set"
    echo "Please set it with: export LINKEDIN_ACCESS_TOKEN='your-access-token'"
    exit 1
fi

echo "✅ LinkedIn credentials are set:"
echo "   Client ID: ${LINKEDIN_CLIENT_ID:0:10}..."
echo "   Client Secret: ${LINKEDIN_CLIENT_SECRET:0:10}..."
echo "   Access Token: ${LINKEDIN_ACCESS_TOKEN:0:10}..."
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

# Test 2: Profile Test
echo "2. Testing LinkedIn profile..."
echo "GET /api/test/profile"
echo ""
curl -s http://localhost:8080/api/test/profile | jq '.' 2>/dev/null || curl -s http://localhost:8080/api/test/profile
echo ""
echo ""

# Test 3: Custom Token Test (using the same token)
echo "3. Testing with custom token..."
echo "POST /api/test/profile/custom"
echo ""
curl -s -X POST http://localhost:8080/api/test/profile/custom \
  -H "Content-Type: application/json" \
  -d "{\"accessToken\": \"$LINKEDIN_ACCESS_TOKEN\"}" | jq '.' 2>/dev/null || \
curl -s -X POST http://localhost:8080/api/test/profile/custom \
  -H "Content-Type: application/json" \
  -d "{\"accessToken\": \"$LINKEDIN_ACCESS_TOKEN\"}"
echo ""
echo ""

echo "=========================================="
echo "Test Complete"
echo "=========================================="
echo ""
echo "Stopping application..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null

echo "✅ LinkedIn API test completed!"
echo ""
echo "Check the results above to see if your LinkedIn API integration is working."
echo "If you see profile information, your setup is correct!"
echo ""
echo "Application logs are saved in: app.log"