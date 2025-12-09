<div align="center">
  <img src="https://sarangen2.github.io/linkedin-comment-responder/logo-simple.svg" alt="LinkedIn Comment Responder Logo" width="200"/>
  
  # LinkedIn Comment Responder

  > **🤖 Agent-Built & Agent-Managed Workflow**  
  > This project is an agentic workflow system built entirely by AI agents and managed through agent-driven development practices. The architecture, code, tests, and documentation were created using Kiro's spec-driven development workflow with AI assistance.

  [![GitHub Pages](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://sarangen2.github.io/linkedin-comment-responder/)
  [![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)](https://spring.io/projects/spring-boot)
  [![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

</div>

An intelligent, agentic workflow system that automates responses to LinkedIn post comments using AI-powered response generation. The system analyzes your original post content and incoming comments, then generates contextually-aware, comedic responses that maintain alignment with your post's theme and tone.

## 🎯 Features

### Core Capabilities
- **Automated Comment Monitoring**: Polls LinkedIn posts for new comments at configurable intervals
- **AI-Powered Response Generation**: Uses LLM (GPT-4, Claude, etc.) to create contextually relevant, comedic responses
- **Manual Approval Workflow**: Optional review and approval before posting responses
- **Automatic Posting**: Seamlessly posts approved responses back to LinkedIn
- **Interaction History**: Maintains complete history of all comments and responses
- **Configurable Tone**: Choose from witty, sarcastic, wholesome, professional, or casual tones
- **Error Handling & Resilience**: Circuit breakers, retry logic, and graceful degradation
- **REST API**: Full management API for controlling the workflow
- **Multi-Environment Support**: Separate configurations for dev, staging, and production

### Agent-Driven Development
- **Spec-Driven Architecture**: Built using formal requirements and design specifications
- **Property-Based Testing**: Correctness properties validated through automated testing
- **AI-Generated Code**: Core components generated and refined by AI agents
- **Automated Documentation**: Comprehensive docs maintained by agent workflows
- **Continuous Agent Management**: Ongoing improvements through agent-assisted development

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [LinkedIn OAuth Setup](#-linkedin-oauth-setup)
- [Configuration](#-configuration)
- [Usage](#-usage)
- [API Documentation](#-api-documentation)
- [Architecture](#-architecture)
- [Troubleshooting](#-troubleshooting)
- [Development](#-development)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [Documentation](#-documentation)

## 🚀 Quick Start

```bash
# 1. Clone the repository
git clone <repository-url>
cd linkedin-comment-responder

# 2. Set required environment variables
export LINKEDIN_CLIENT_ID="your-linkedin-client-id"
export LINKEDIN_CLIENT_SECRET="your-linkedin-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-linkedin-access-token"
export OPENAI_API_KEY="your-openai-api-key"

# 3. Build the project
mvn clean install

# 4. Run the application
mvn spring-boot:run

# 5. Start monitoring a post
curl -X POST http://localhost:8080/api/management/polling/start \
  -H "Content-Type: application/json" \
  -d '{
    "postId": "urn:li:share:YOUR_POST_ID",
    "pollingIntervalSeconds": 300,
    "requireManualApproval": true,
    "tonePreference": "witty"
  }'
```

## 📦 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **LinkedIn Developer Account** with OAuth credentials
- **OpenAI API Key** (or other LLM provider)
- **Internet connection** for API access

## 💾 Installation

### Local Development

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd linkedin-comment-responder
   ```

2. **Install dependencies**:
   ```bash
   mvn clean install
   ```

3. **Configure environment variables** (see [Configuration](#-configuration))

4. **Run the application**:
   ```bash
   # Development mode
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   
   # Or run the packaged JAR
   java -jar target/linkedin-comment-responder-1.0.0.jar
   ```

### Docker Deployment

```bash
# Build Docker image
docker build -t linkedin-responder:latest .

# Run container
docker run -d \
  -e LINKEDIN_CLIENT_ID="your-client-id" \
  -e LINKEDIN_CLIENT_SECRET="your-client-secret" \
  -e LINKEDIN_ACCESS_TOKEN="your-access-token" \
  -e OPENAI_API_KEY="your-api-key" \
  -p 8080:8080 \
  linkedin-responder:latest
```

## 🔐 LinkedIn OAuth Setup

To use this application, you need to set up a LinkedIn OAuth application and obtain API credentials.

### Step 1: Create a LinkedIn App

1. Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
2. Click **"Create app"**
3. Fill in the required information:
   - **App name**: LinkedIn Comment Responder
   - **LinkedIn Page**: Select your company page (or create one)
   - **App logo**: Upload a logo (optional)
   - **Legal agreement**: Accept the terms

### Step 2: Configure OAuth Settings

1. In your app dashboard, go to the **"Auth"** tab
2. Add **Redirect URLs**:
   ```
   http://localhost:8080/auth/callback
   ```
3. Note your **Client ID** and **Client Secret**

### Step 3: Request API Access

1. Go to the **"Products"** tab
2. Request access to:
   - **Sign In with LinkedIn** (for authentication)
   - **Share on LinkedIn** (for posting comments)
   - **Marketing Developer Platform** (for reading comments)

⚠️ **Note**: LinkedIn may require verification for certain API products. This can take several days.

### Step 4: Generate Access Token

#### Option A: Manual Token Generation (Development)

1. Construct the authorization URL:
   ```
   https://www.linkedin.com/oauth/v2/authorization?
     response_type=code&
     client_id=YOUR_CLIENT_ID&
     redirect_uri=http://localhost:8080/auth/callback&
     scope=r_liteprofile%20r_emailaddress%20w_member_social%20rw_organization_admin
   ```

2. Visit the URL in your browser and authorize the app
3. Copy the `code` from the redirect URL
4. Exchange the code for an access token:
   ```bash
   curl -X POST https://www.linkedin.com/oauth/v2/accessToken \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=authorization_code" \
     -d "code=YOUR_AUTH_CODE" \
     -d "client_id=YOUR_CLIENT_ID" \
     -d "client_secret=YOUR_CLIENT_SECRET" \
     -d "redirect_uri=http://localhost:8080/auth/callback"
   ```

5. Save the `access_token` and `refresh_token` from the response

#### Option B: OAuth Flow (Production)

For production deployments, implement a proper OAuth flow with token refresh. The application includes token refresh logic in `LinkedInApiClient.java`.

### Step 5: Set Environment Variables

```bash
export LINKEDIN_CLIENT_ID="your_client_id_here"
export LINKEDIN_CLIENT_SECRET="your_client_secret_here"
export LINKEDIN_ACCESS_TOKEN="your_access_token_here"
export LINKEDIN_REFRESH_TOKEN="your_refresh_token_here"
```

### Required LinkedIn API Scopes

- `r_liteprofile` - Read basic profile information
- `r_emailaddress` - Read email address
- `w_member_social` - Post, comment, and interact with posts
- `rw_organization_admin` - Manage organization pages (if posting as a company)

### Token Expiration

- **Access tokens** expire after 60 days
- **Refresh tokens** expire after 1 year
- The application automatically refreshes tokens when they expire
- Monitor logs for token refresh events

## ⚙️ Configuration

The application uses Spring Boot's configuration system with support for multiple environments.

### Environment Variables (Required)

```bash
# LinkedIn API Credentials
export LINKEDIN_CLIENT_ID="your-linkedin-client-id"
export LINKEDIN_CLIENT_SECRET="your-linkedin-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-linkedin-access-token"
export LINKEDIN_REFRESH_TOKEN="your-linkedin-refresh-token"  # Optional

# LLM API Key
export OPENAI_API_KEY="your-openai-api-key"

# Optional: Error Notifications
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
export NOTIFICATION_EMAIL="admin@example.com"
```

### Environment Profiles

The application supports three profiles:

| Profile | Purpose | Logging | Approval | LLM Model | Notifications |
|---------|---------|---------|----------|-----------|---------------|
| **dev** | Local development | DEBUG | Required | gpt-3.5-turbo | Log only |
| **staging** | Pre-production | INFO | Required | gpt-4 | Email |
| **prod** | Production | INFO/WARN | Optional | gpt-4 | Slack |

**Activate a profile**:
```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Staging
java -jar app.jar --spring.profiles.active=staging

# Production
java -jar app.jar --spring.profiles.active=prod
```

### Key Configuration Parameters

See [CONFIGURATION.md](CONFIGURATION.md) for complete documentation.

**Quick reference**:
- `workflow.polling-interval-seconds` - How often to check for new comments (default: 300)
- `workflow.require-manual-approval` - Require approval before posting (default: false)
- `workflow.tone-preference` - Response tone: witty, sarcastic, wholesome, professional, casual
- `llm.model` - LLM model to use (default: gpt-4)
- `llm.temperature` - Response creativity 0.0-2.0 (default: 0.7)

## 📖 Usage

### Starting the Application

```bash
# Start with default settings
mvn spring-boot:run

# Start with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Basic Workflow

1. **Start polling a post**:
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

2. **Check polling status**:
   ```bash
   curl http://localhost:8080/api/management/polling/status
   ```

3. **If manual approval is enabled, review pending responses**:
   ```bash
   # Get pending response
   curl http://localhost:8080/api/management/approval/pending
   
   # Approve the response
   curl -X POST http://localhost:8080/api/management/approval/decision \
     -H "Content-Type: application/json" \
     -d '{"approve": true}'
   
   # Or reject it
   curl -X POST http://localhost:8080/api/management/approval/decision \
     -H "Content-Type: application/json" \
     -d '{"approve": false, "reason": "Tone is too casual"}'
   ```

4. **View interaction history**:
   ```bash
   curl http://localhost:8080/api/management/history
   ```

5. **Stop polling**:
   ```bash
   curl -X POST http://localhost:8080/api/management/polling/stop
   ```

### Advanced Usage

**Update configuration while running**:
```bash
curl -X PATCH http://localhost:8080/api/management/config \
  -H "Content-Type: application/json" \
  -d '{
    "tonePreference": "professional",
    "requireManualApproval": true
  }'
```

**Export interaction history**:
```bash
# Export as JSON
curl -O http://localhost:8080/api/management/history/export?format=json

# Export as CSV
curl -O http://localhost:8080/api/management/history/export?format=csv
```

**Filter history by date range**:
```bash
curl "http://localhost:8080/api/management/history?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z"
```

## 📚 API Documentation

### Interactive Documentation

Once the application is running, access the Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

### Complete API Reference

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for detailed endpoint documentation including:
- Request/response formats
- Status codes
- Example curl commands
- Error handling

### Quick API Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/management/polling/start` | POST | Start monitoring a post |
| `/api/management/polling/stop` | POST | Stop monitoring |
| `/api/management/polling/status` | GET | Get current status |
| `/api/management/approval/pending` | GET | Get pending response |
| `/api/management/approval/decision` | POST | Approve/reject response |
| `/api/management/history` | GET | Query interaction history |
| `/api/management/history/export` | GET | Export history |
| `/api/management/config` | PATCH | Update configuration |

## 🏗️ Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Workflow Orchestrator                     │
│  (Coordinates polling, generation, and posting workflow)     │
└───────────┬─────────────────────────────────────┬───────────┘
            │                                     │
            ▼                                     ▼
┌───────────────────────┐              ┌──────────────────────┐
│  LinkedIn API Client  │              │    LLM Agent         │
│  - Fetch comments     │              │  - Generate response │
│  - Post responses     │              │  - Context analysis  │
│  - Rate limiting      │              │  - Tone matching     │
└───────────┬───────────┘              └──────────┬───────────┘
            │                                     │
            └──────────────┬──────────────────────┘
                           ▼
                ┌──────────────────────┐
                │  Storage Repository  │
                │  - Interaction log   │
                │  - Processed comments│
                │  - Configuration     │
                └──────────────────────┘
```

### Key Components

- **Workflow Orchestrator**: Coordinates the end-to-end workflow
- **LinkedIn API Client**: Interfaces with LinkedIn's API
- **LLM Agent**: Generates contextually appropriate responses
- **Storage Repository**: Persists interaction history and state
- **Management API**: REST endpoints for control and monitoring
- **Error Handler**: Centralized error handling and notifications
- **Circuit Breaker**: Protects against cascading failures

### Agent-Driven Architecture

This project follows a **spec-driven development** methodology where AI agents:

1. **Requirements Phase**: Agents analyze user needs and generate formal requirements using EARS (Easy Approach to Requirements Syntax)
2. **Design Phase**: Agents create detailed architecture with correctness properties for validation
3. **Implementation Phase**: Agents generate code following the design specifications
4. **Testing Phase**: Agents write property-based tests to verify correctness properties
5. **Documentation Phase**: Agents maintain comprehensive documentation aligned with implementation

**Key Artifacts**:
- [requirements.md](.kiro/specs/linkedin-comment-responder/requirements.md) - Formal requirements with acceptance criteria
- [design.md](.kiro/specs/linkedin-comment-responder/design.md) - Architecture and correctness properties
- [tasks.md](.kiro/specs/linkedin-comment-responder/tasks.md) - Implementation task breakdown
- [linkedin-comment-responder.md](.kiro/steering/linkedin-comment-responder.md) - Agent steering guidelines

### Technology Stack

- **Java 17**
- **Spring Boot 3.2.0** - Core framework
- **Spring WebFlux** - HTTP client for LinkedIn API
- **OpenAI Java SDK** - LLM integration
- **Jackson** - JSON processing
- **jqwik 1.8.2** - Property-based testing
- **Maven** - Build tool
- **Kiro** - Agent-driven development platform

See [README-LINKEDIN.md](README-LINKEDIN.md) for detailed project structure.

## 🔧 Troubleshooting

### Common Issues

#### Application fails to start

**Error**: "Configuration validation failed"

**Solution**: Ensure all required environment variables are set:
```bash
echo $LINKEDIN_CLIENT_ID
echo $LINKEDIN_CLIENT_SECRET
echo $LINKEDIN_ACCESS_TOKEN
echo $OPENAI_API_KEY
```

#### LinkedIn API authentication errors

**Error**: "401 Unauthorized" or "403 Forbidden"

**Solutions**:
1. Verify your access token is valid and not expired
2. Check that your LinkedIn app has the required API products enabled
3. Ensure the access token has the necessary scopes
4. Try refreshing the token manually

```bash
# Test your access token
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  https://api.linkedin.com/v2/me
```

#### Rate limiting errors

**Error**: "429 Too Many Requests"

**Solutions**:
1. Increase polling interval:
   ```properties
   workflow.polling-interval-seconds=600
   ```
2. Reduce rate limit parameter:
   ```properties
   linkedin.api.rate-limit-per-minute=50
   ```
3. Check your LinkedIn API tier limits

#### LLM responses are inappropriate

**Solutions**:
1. Enable manual approval mode:
   ```properties
   workflow.require-manual-approval=true
   ```
2. Adjust tone preference:
   ```properties
   workflow.tone-preference=professional
   ```
3. Add manual review keywords:
   ```properties
   workflow.manual-review-keywords=urgent,complaint,legal,refund
   ```
4. Lower temperature for more conservative responses:
   ```properties
   llm.temperature=0.3
   ```

#### Storage directory errors

**Error**: "Storage directory is not writable"

**Solution**: Create directory and set permissions:
```bash
mkdir -p ./data
chmod 755 ./data
```

#### Circuit breaker opening frequently

**Error**: "Circuit breaker is OPEN"

**Solutions**:
1. Check underlying service health (LinkedIn API, LLM API)
2. Adjust circuit breaker thresholds:
   ```properties
   circuit-breaker.failure-threshold=10
   circuit-breaker.reset-timeout-ms=120000
   ```
3. Review error logs for root cause

### Debug Mode

Enable debug logging to troubleshoot issues:

```bash
# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or set logging level
java -jar app.jar --logging.level.com.example.linkedin=DEBUG
```

### Getting Help

1. Check the logs in `logs/` directory
2. Review [CONFIGURATION.md](CONFIGURATION.md) for configuration issues
3. See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for API usage
4. Open an issue on GitHub with:
   - Error messages
   - Configuration (with credentials redacted)
   - Steps to reproduce

## 👨‍💻 Development

### Project Structure

```
src/main/java/com/example/linkedin/
├── LinkedInCommentResponderApplication.java  # Main application
├── model/                                     # Domain models
├── client/                                    # External API clients
├── agent/                                     # AI/LLM components
├── repository/                                # Data persistence
├── orchestrator/                              # Workflow coordination
├── api/                                       # REST API controllers
├── config/                                    # Configuration
├── error/                                     # Error handling
└── resilience/                                # Circuit breakers
```

### Building from Source

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build without running property tests
mvn clean install -Djqwik.tries=0
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=WorkflowOrchestratorTest

# Run with coverage
mvn clean test jacoco:report
```

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods focused and small
- Write tests for new functionality

## 🧪 Testing

### Test Strategy

The project uses a dual testing approach:

1. **Unit Tests**: Verify specific examples and edge cases
2. **Property-Based Tests**: Verify universal properties across many inputs

### Running Tests

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest=*Test

# Property tests only
mvn test -Dtest=*PropertyTest

# Specific test
mvn test -Dtest=FileBasedStorageRepositoryTest
```

### Property-Based Testing

The project uses [jqwik](https://jqwik.net/) for property-based testing:

```java
// Example property test
@Property(tries = 100)
void allRetrievedCommentsBelongToRequestedPost(@ForAll String postId) {
    List<Comment> comments = client.fetchComments(postId);
    assertThat(comments).allMatch(c -> c.getPostId().equals(postId));
}
```

### Test Coverage

- Unit tests: Core functionality and edge cases
- Property tests: Universal correctness properties
- Integration tests: Component interactions

See [design.md](.kiro/specs/linkedin-comment-responder/design.md) for complete testing strategy.

## 🤝 Contributing

Contributions are welcome! We appreciate bug fixes, new features, documentation improvements, and more.

### Quick Start for Contributors

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for new functionality
4. Ensure all tests pass (`mvn test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Detailed Guidelines

See [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Development setup
- Coding standards
- Testing guidelines
- Pull request process
- Issue guidelines

### Development Workflow

1. Check existing issues or create a new one
2. Discuss the approach before implementing large changes
3. Follow the existing code style (see [CONTRIBUTING.md](CONTRIBUTING.md))
4. Add tests for new features
5. Update documentation as needed
6. Ensure all tests pass before requesting review

## 🔒 Privacy Policy

This application requires a privacy policy for LinkedIn Developer Portal compliance.

- **Privacy Policy**: [PRIVACY_POLICY.md](PRIVACY_POLICY.md)
- **HTML Version**: [privacy-policy.html](privacy-policy.html)
- **Setup Guide**: [PRIVACY_POLICY_SETUP.md](PRIVACY_POLICY_SETUP.md)

See [PRIVACY_POLICY_SETUP.md](PRIVACY_POLICY_SETUP.md) for instructions on publishing your privacy policy.

## 📄 License

[Add your license here]

## 🙏 Acknowledgments

- LinkedIn API for providing the platform integration
- OpenAI for LLM capabilities
- Spring Boot team for the excellent framework
- jqwik for property-based testing support

## 📞 Support

- **Documentation**: See docs in this repository
- **Issues**: Open an issue on GitHub
- **Email**: [your-email@example.com]

## 🗺️ Roadmap

- [ ] Multi-platform support (Twitter, Facebook, Instagram)
- [ ] Response personalization based on user edits
- [ ] Sentiment analysis for tone adjustment
- [ ] A/B testing for response styles
- [ ] Analytics dashboard
- [ ] Batch processing for multiple posts
- [ ] Custom response templates
- [ ] Machine learning for style adaptation

## 📚 Documentation

Complete documentation is available in the following files:

### Getting Started
- **[QUICKSTART.md](QUICKSTART.md)** - Get up and running in 10 minutes
- **[README.md](README.md)** - This file - comprehensive overview

### Configuration & Setup
- **[CONFIGURATION.md](CONFIGURATION.md)** - Complete configuration reference
- **[EXAMPLES.md](EXAMPLES.md)** - Configuration examples for different use cases

### API & Usage
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - REST API endpoint reference
- **Swagger UI** - Interactive API docs at `http://localhost:8080/swagger-ui.html`

### Troubleshooting & Support
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Common issues and solutions

### Technical Details
- **[README-LINKEDIN.md](README-LINKEDIN.md)** - Project structure and architecture
- **[design.md](.kiro/specs/linkedin-comment-responder/design.md)** - Detailed design document
- **[requirements.md](.kiro/specs/linkedin-comment-responder/requirements.md)** - System requirements

### Quick Links

| I want to... | See... |
|--------------|--------|
| Get started quickly | [QUICKSTART.md](QUICKSTART.md) |
| Configure the application | [CONFIGURATION.md](CONFIGURATION.md) |
| Use the API | [API_DOCUMENTATION.md](API_DOCUMENTATION.md) |
| Fix an issue | [TROUBLESHOOTING.md](TROUBLESHOOTING.md) |
| See example configs | [EXAMPLES.md](EXAMPLES.md) |
| Understand the architecture | [README-LINKEDIN.md](README-LINKEDIN.md) |

---

## 🤖 About Agent-Driven Development

This project showcases the power of **agent-driven development** using Kiro's spec-driven workflow:

### Development Process

1. **Requirements Gathering**: AI agents analyze the problem and generate formal requirements using EARS syntax with INCOSE quality rules
2. **Design Creation**: Agents create comprehensive architecture with correctness properties that must be validated
3. **Code Generation**: Agents implement the design following established patterns and best practices
4. **Property-Based Testing**: Agents write tests that verify universal correctness properties across all inputs
5. **Documentation**: Agents maintain synchronized documentation that evolves with the codebase

### Benefits of Agent-Driven Development

- **Consistency**: Formal specifications ensure consistent implementation
- **Correctness**: Property-based testing validates behavior across all inputs
- **Maintainability**: Clear architecture and documentation make changes easier
- **Velocity**: Agents handle boilerplate and repetitive tasks
- **Quality**: Automated testing and validation catch issues early

### Learn More

- Explore the [spec files](.kiro/specs/linkedin-comment-responder/) to see the formal requirements and design
- Review the [steering document](.kiro/steering/linkedin-comment-responder.md) for agent guidelines
- Check the [property-based tests](src/test/java/) to see correctness validation in action

---

**Built with ❤️ by AI Agents using Kiro, Spring Boot, and OpenAI**
