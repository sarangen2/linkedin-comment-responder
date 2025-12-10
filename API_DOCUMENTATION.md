# LinkedIn Comment Responder - Management API Documentation

## Overview

The Management API provides REST endpoints for controlling and monitoring the LinkedIn Comment Responder workflow system.

## Base URLs

### Management API
```
http://localhost:8080/api/management
```

### Profile Test API
```
http://localhost:8080/api/test
```

## Interactive API Documentation

Once the application is running, you can access the interactive Swagger UI documentation at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI specification is available at:

```
http://localhost:8080/api-docs
```

## Profile Test Endpoints

### 1. Test LinkedIn Profile

**GET** `/api/test/profile`

Tests LinkedIn API connectivity and retrieves your basic profile information using the configured access token.

**Response:**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": "your-linkedin-id",
    "firstName": "Your",
    "lastName": "Name",
    "profilePicture": "profile-image-url",
    "apiTestStatus": "SUCCESS",
    "tokenValid": true,
    "timestamp": 1703875200000
  }
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `401 Unauthorized` - Invalid or expired access token
- `403 Forbidden` - Insufficient API permissions

**Example:**
```bash
curl http://localhost:8080/api/test/profile
```

### 2. Test with Custom Token

**POST** `/api/test/profile/custom`

Tests LinkedIn API connectivity using a custom access token (useful for testing different tokens).

**Request Body:**
```json
{
  "accessToken": "your-custom-linkedin-access-token"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Profile retrieved with custom token",
  "data": {
    "id": "your-linkedin-id",
    "firstName": "Your",
    "lastName": "Name",
    "apiTestStatus": "SUCCESS",
    "tokenValid": true,
    "tokenSource": "CUSTOM",
    "timestamp": 1703875200000
  }
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/test/profile/custom \
  -H "Content-Type: application/json" \
  -d '{"accessToken": "your-token-here"}'
```

### 3. Check Configuration Status

**GET** `/api/test/config`

Returns the current LinkedIn API configuration status without exposing sensitive data.

**Response:**
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

**Example:**
```bash
curl http://localhost:8080/api/test/config
```

## Management Endpoints

### 4. Start Polling

**POST** `/polling/start`

Starts the automated polling workflow for a specific LinkedIn post.

**Request Body:**
```json
{
  "postId": "urn:li:share:1234567890",
  "pollingIntervalSeconds": 300,
  "requireManualApproval": false,
  "tonePreference": "witty",
  "manualReviewKeywords": ["urgent", "complaint", "refund"],
  "maxRetries": 3,
  "retryBackoffSeconds": 2
}
```

**Response:**
- `200 OK` - Polling started successfully
- `400 Bad Request` - Invalid request parameters
- `409 Conflict` - Polling is already active

**Example:**
```bash
curl -X POST http://localhost:8080/api/management/polling/start \
  -H "Content-Type: application/json" \
  -d '{
    "postId": "urn:li:share:1234567890",
    "pollingIntervalSeconds": 300,
    "requireManualApproval": false,
    "tonePreference": "witty",
    "maxRetries": 3,
    "retryBackoffSeconds": 2
  }'
```

---

### 2. Stop Polling

**POST** `/polling/stop`

Stops the automated polling workflow.

**Response:**
- `200 OK` - Polling stopped successfully
- `400 Bad Request` - Polling is not active

**Example:**
```bash
curl -X POST http://localhost:8080/api/management/polling/stop
```

---

### 3. Get Polling Status

**GET** `/polling/status`

Returns the current polling status and configuration.

**Response:**
- `200 OK` - Status retrieved successfully

**Response Body:**
```json
{
  "success": true,
  "message": "Polling is active",
  "data": {
    "postId": "urn:li:share:1234567890",
    "pollingIntervalSeconds": 300,
    "requireManualApproval": false,
    "tonePreference": "witty",
    "manualReviewKeywords": ["urgent", "complaint"],
    "maxRetries": 3,
    "retryBackoffSeconds": 2
  }
}
```

**Example:**
```bash
curl http://localhost:8080/api/management/polling/status
```

---

### 4. Get Pending Response

**GET** `/approval/pending`

Returns the pending response awaiting manual approval, if any.

**Response:**
- `200 OK` - Pending response retrieved successfully
- `404 Not Found` - No pending response

**Response Body:**
```json
{
  "success": true,
  "message": "Pending response retrieved",
  "data": {
    "commentId": "comment-123",
    "commenterName": "John Doe",
    "commentText": "Great post! Very insightful.",
    "generatedResponse": "Thanks John! Glad you found it helpful.",
    "confidenceScore": 0.95,
    "reasoning": "Positive comment, straightforward acknowledgment",
    "warnings": []
  }
}
```

**Example:**
```bash
curl http://localhost:8080/api/management/approval/pending
```

---

### 5. Approve or Reject Response

**POST** `/approval/decision`

Approves or rejects the pending response awaiting manual approval.

**Request Body:**
```json
{
  "approve": true,
  "reason": "Response looks good"
}
```

**Response:**
- `200 OK` - Decision processed successfully
- `404 Not Found` - No pending response
- `500 Internal Server Error` - Failed to post approved response

**Example (Approve):**
```bash
curl -X POST http://localhost:8080/api/management/approval/decision \
  -H "Content-Type: application/json" \
  -d '{"approve": true}'
```

**Example (Reject):**
```bash
curl -X POST http://localhost:8080/api/management/approval/decision \
  -H "Content-Type: application/json" \
  -d '{"approve": false, "reason": "Tone is too casual"}'
```

---

### 6. Query Interaction History

**GET** `/history`

Retrieves interaction history with optional filters.

**Query Parameters:**
- `postId` (optional) - Filter by post ID
- `startDate` (optional) - Start date in ISO-8601 format (e.g., `2024-01-01T00:00:00Z`)
- `endDate` (optional) - End date in ISO-8601 format

**Response:**
- `200 OK` - History retrieved successfully
- `400 Bad Request` - Invalid date format

**Response Body:**
```json
{
  "success": true,
  "message": "Retrieved 5 interactions",
  "data": [
    {
      "id": "interaction-1",
      "postId": "urn:li:share:1234567890",
      "commentId": "comment-123",
      "commenterName": "John Doe",
      "commentText": "Great post!",
      "generatedResponse": "Thanks John!",
      "postedResponse": "Thanks John!",
      "timestamp": "2024-12-08T12:00:00Z",
      "status": "POSTED",
      "metadata": {
        "confidence_score": "0.95",
        "tone_preference": "witty"
      }
    }
  ]
}
```

**Example:**
```bash
# Get all history
curl http://localhost:8080/api/management/history

# Filter by post ID
curl "http://localhost:8080/api/management/history?postId=urn:li:share:1234567890"

# Filter by date range
curl "http://localhost:8080/api/management/history?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z"
```

---

### 7. Export Interaction History

**GET** `/history/export`

Exports interaction history to JSON or CSV format.

**Query Parameters:**
- `format` (optional, default: `json`) - Export format (`json` or `csv`)

**Response:**
- `200 OK` - History exported successfully (file download)
- `400 Bad Request` - Invalid format

**Example:**
```bash
# Export as JSON
curl -O http://localhost:8080/api/management/history/export?format=json

# Export as CSV
curl -O http://localhost:8080/api/management/history/export?format=csv
```

---

### 8. Update Configuration

**PATCH** `/config`

Updates the workflow configuration while polling is active.

**Request Body:**
```json
{
  "pollingIntervalSeconds": 600,
  "requireManualApproval": true,
  "tonePreference": "professional",
  "manualReviewKeywords": ["urgent", "complaint", "legal"],
  "maxRetries": 5,
  "retryBackoffSeconds": 3
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response:**
- `200 OK` - Configuration updated successfully
- `400 Bad Request` - Polling is not active or invalid parameters

**Response Body:**
```json
{
  "success": true,
  "message": "Configuration updated",
  "data": {
    "postId": "urn:li:share:1234567890",
    "pollingIntervalSeconds": 600,
    "requireManualApproval": true,
    "tonePreference": "professional",
    "manualReviewKeywords": ["urgent", "complaint", "legal"],
    "maxRetries": 5,
    "retryBackoffSeconds": 3
  }
}
```

**Example:**
```bash
curl -X PATCH http://localhost:8080/api/management/config \
  -H "Content-Type: application/json" \
  -d '{
    "requireManualApproval": true,
    "tonePreference": "professional"
  }'
```

---

## Response Status Codes

All endpoints return standard HTTP status codes:

- `200 OK` - Request successful
- `400 Bad Request` - Invalid request parameters
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., polling already active)
- `500 Internal Server Error` - Server error

## Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error message"
}
```

## Tone Preferences

Supported tone preferences:
- `witty` - Clever and humorous
- `sarcastic` - Playfully sarcastic
- `wholesome` - Warm and friendly
- `professional` - Formal and business-like

## Response Status Values

Interaction history includes these status values:
- `GENERATED` - Response generated but not yet posted
- `APPROVED` - Response approved by user (manual approval mode)
- `POSTED` - Response successfully posted to LinkedIn
- `FAILED` - Failed to post response
- `REJECTED` - Response rejected by user (manual approval mode)

## Getting Started

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Access the Swagger UI:
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. Start polling for a post:
   ```bash
   curl -X POST http://localhost:8080/api/management/polling/start \
     -H "Content-Type: application/json" \
     -d '{
       "postId": "your-post-id",
       "pollingIntervalSeconds": 300,
       "requireManualApproval": false,
       "tonePreference": "witty"
     }'
   ```

4. Monitor the status:
   ```bash
   curl http://localhost:8080/api/management/polling/status
   ```

5. View interaction history:
   ```bash
   curl http://localhost:8080/api/management/history
   ```

## Notes

- The API uses JSON for request and response bodies
- All timestamps are in ISO-8601 format (UTC)
- The polling interval is in seconds
- Manual approval mode requires calling the approval endpoint for each generated response
- Configuration can be updated while polling is active without restarting
