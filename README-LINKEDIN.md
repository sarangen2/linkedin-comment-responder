# LinkedIn Comment Responder

An agentic workflow system that automates responses to LinkedIn post comments using AI-powered response generation.

## Project Structure

```
src/main/java/com/example/linkedin/
├── LinkedInCommentResponderApplication.java  # Main Spring Boot application
├── model/                                     # Domain models
│   ├── Comment.java                          # Comment representation
│   ├── Post.java                             # Post representation
│   ├── GeneratedResponse.java                # LLM-generated response
│   ├── Interaction.java                      # Interaction history entry
│   ├── ResponseStatus.java                   # Response status enum
│   ├── WorkflowConfig.java                   # Workflow configuration
│   └── LinkedInCredentials.java              # API credentials
├── client/                                    # External API clients
│   └── LinkedInApiClient.java                # LinkedIn API interface
├── agent/                                     # AI/LLM components
│   └── LLMAgent.java                         # LLM response generation interface
├── repository/                                # Data persistence
│   └── StorageRepository.java                # Storage interface
└── orchestrator/                              # Workflow coordination
    └── WorkflowOrchestrator.java             # Workflow orchestration interface
```

## Dependencies

- **Spring Boot 3.2.0**: Core framework with web and scheduling support
- **Spring WebFlux**: HTTP client for LinkedIn API integration
- **OpenAI Java SDK**: LLM integration for response generation
- **jqwik 1.8.2**: Property-based testing framework
- **Jackson**: JSON processing
- **Jakarta Validation**: Bean validation

## Configuration

Configuration is managed through `src/main/resources/application.properties`:

### LinkedIn API
- `linkedin.api.client-id`: LinkedIn OAuth client ID
- `linkedin.api.client-secret`: LinkedIn OAuth client secret
- `linkedin.api.access-token`: LinkedIn API access token
- `linkedin.api.refresh-token`: LinkedIn API refresh token

### LLM Configuration
- `llm.provider`: LLM provider (default: openai)
- `llm.api-key`: OpenAI API key
- `llm.model`: Model to use (default: gpt-4)
- `llm.temperature`: Response creativity (default: 0.7)

### Workflow Configuration
- `workflow.polling-interval-seconds`: Comment polling frequency (default: 300)
- `workflow.require-manual-approval`: Enable manual approval mode (default: false)
- `workflow.tone-preference`: Response tone (default: witty)
- `workflow.max-retries`: Maximum retry attempts (default: 3)

### Storage Configuration
- `storage.type`: Storage type (default: file)
- `storage.file-path`: Path for interaction storage
- `storage.max-capacity`: Maximum stored interactions

## Building and Running

### Build the project
```bash
mvn clean install
```

### Run the application
```bash
mvn spring-boot:run
```

### Run as packaged JAR
```bash
java -jar target/linkedin-comment-responder-1.0.0.jar
```

## Environment Variables

For security, sensitive credentials should be provided via environment variables:

```bash
export LINKEDIN_CLIENT_ID="your-client-id"
export LINKEDIN_CLIENT_SECRET="your-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-access-token"
export OPENAI_API_KEY="your-openai-api-key"
```

## Next Steps

This is the initial project structure. The following components need implementation:

1. LinkedIn API Client implementation
2. LLM Agent implementation
3. Storage Repository implementation
4. Workflow Orchestrator implementation
5. REST API endpoints for management
6. Error handling and logging
7. Property-based tests for correctness properties

See `.kiro/specs/linkedin-comment-responder/tasks.md` for the complete implementation plan.
