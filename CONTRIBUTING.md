# Contributing to LinkedIn Comment Responder

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors.

### Expected Behavior

- Be respectful and considerate
- Welcome newcomers and help them get started
- Focus on constructive feedback
- Accept responsibility for mistakes

### Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Publishing others' private information
- Other unprofessional conduct

---

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- Java 17 or higher
- Maven 3.6+
- Git
- A GitHub account
- LinkedIn Developer account (for testing)
- OpenAI API key (for testing)

### Fork and Clone

1. **Fork the repository** on GitHub
2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/linkedin-comment-responder.git
   cd linkedin-comment-responder
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/linkedin-comment-responder.git
   ```

---

## Development Setup

### 1. Install Dependencies

```bash
mvn clean install
```

### 2. Set Up Environment Variables

```bash
export LINKEDIN_CLIENT_ID="your-dev-client-id"
export LINKEDIN_CLIENT_SECRET="your-dev-client-secret"
export LINKEDIN_ACCESS_TOKEN="your-dev-access-token"
export OPENAI_API_KEY="your-dev-api-key"
```

### 3. Run the Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Run Tests

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=WorkflowOrchestratorTest

# With coverage
mvn clean test jacoco:report
```

---

## How to Contribute

### Types of Contributions

We welcome:

- **Bug fixes** - Fix issues in existing code
- **New features** - Add new functionality
- **Documentation** - Improve or add documentation
- **Tests** - Add or improve test coverage
- **Performance improvements** - Optimize existing code
- **Refactoring** - Improve code quality

### Before You Start

1. **Check existing issues** - Someone may already be working on it
2. **Create an issue** - Discuss your idea before implementing
3. **Get feedback** - Ensure your approach aligns with project goals

---

## Coding Standards

### Java Style Guide

Follow standard Java conventions:

#### Naming Conventions

```java
// Classes: PascalCase
public class WorkflowOrchestrator { }

// Methods: camelCase
public void startPolling() { }

// Constants: UPPER_SNAKE_CASE
private static final int MAX_RETRIES = 3;

// Variables: camelCase
private String postId;
```

#### Code Organization

```java
public class ExampleClass {
    // 1. Static fields
    private static final Logger logger = LoggerFactory.getLogger(ExampleClass.class);
    
    // 2. Instance fields
    private final String field1;
    private int field2;
    
    // 3. Constructors
    public ExampleClass(String field1) {
        this.field1 = field1;
    }
    
    // 4. Public methods
    public void publicMethod() { }
    
    // 5. Private methods
    private void privateMethod() { }
}
```

#### Documentation

```java
/**
 * Starts polling for comments on the specified LinkedIn post.
 *
 * @param postId the LinkedIn post URN to monitor
 * @param config the workflow configuration
 * @throws IllegalArgumentException if postId is null or invalid
 * @throws IllegalStateException if polling is already active
 */
public void startPolling(String postId, WorkflowConfig config) {
    // Implementation
}
```

### Spring Boot Conventions

```java
// Controllers
@RestController
@RequestMapping("/api/management")
public class ManagementController { }

// Services
@Service
public class WorkflowOrchestrator { }

// Repositories
@Repository
public class FileBasedStorageRepository { }

// Configuration
@Configuration
public class OpenApiConfig { }
```

### Error Handling

```java
// Use specific exceptions
throw new IllegalArgumentException("Post ID cannot be null");

// Log errors with context
logger.error("Failed to fetch comments for post {}: {}", postId, e.getMessage(), e);

// Use try-with-resources
try (FileWriter writer = new FileWriter(file)) {
    // Use resource
}
```

---

## Testing Guidelines

### Test Structure

```java
@Test
void shouldReturnCommentsForValidPost() {
    // Given
    String postId = "urn:li:share:123";
    
    // When
    List<Comment> comments = client.fetchComments(postId);
    
    // Then
    assertThat(comments).isNotEmpty();
    assertThat(comments).allMatch(c -> c.getPostId().equals(postId));
}
```

### Property-Based Tests

```java
@Property(tries = 100)
void allRetrievedCommentsBelongToRequestedPost(@ForAll String postId) {
    // Feature: linkedin-comment-responder, Property 1: Comment retrieval belongs to post
    List<Comment> comments = client.fetchComments(postId);
    assertThat(comments).allMatch(c -> c.getPostId().equals(postId));
}
```

### Test Coverage

- **Unit tests**: Test individual methods and classes
- **Property tests**: Test universal properties across many inputs
- **Integration tests**: Test component interactions
- **Aim for**: 80%+ code coverage

### Running Tests

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest=*Test

# Property tests only
mvn test -Dtest=*PropertyTest

# With coverage report
mvn clean test jacoco:report
# View: target/site/jacoco/index.html
```

---

## Pull Request Process

### 1. Create a Branch

```bash
# Update your fork
git fetch upstream
git checkout main
git merge upstream/main

# Create feature branch
git checkout -b feature/your-feature-name
```

### 2. Make Changes

- Write clean, documented code
- Follow coding standards
- Add tests for new functionality
- Update documentation as needed

### 3. Commit Changes

```bash
# Stage changes
git add .

# Commit with descriptive message
git commit -m "Add feature: description of what you did"
```

**Commit Message Format**:
```
<type>: <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `test`: Adding or updating tests
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `chore`: Maintenance tasks

**Example**:
```
feat: Add support for multiple post monitoring

- Implement concurrent polling for multiple posts
- Add configuration for max concurrent posts
- Update API to accept multiple post IDs

Closes #123
```

### 4. Run Tests

```bash
# Ensure all tests pass
mvn clean test

# Check code style
mvn checkstyle:check

# Verify build
mvn clean install
```

### 5. Push Changes

```bash
git push origin feature/your-feature-name
```

### 6. Create Pull Request

1. Go to your fork on GitHub
2. Click "New Pull Request"
3. Select your feature branch
4. Fill in the PR template:
   - **Title**: Clear, descriptive title
   - **Description**: What changes were made and why
   - **Related Issues**: Link to related issues
   - **Testing**: How you tested the changes
   - **Screenshots**: If applicable

### 7. Code Review

- Respond to feedback promptly
- Make requested changes
- Push updates to the same branch
- Be open to suggestions

### 8. Merge

Once approved:
- Maintainer will merge your PR
- Your branch will be deleted
- Changes will be in the main branch

---

## Issue Guidelines

### Creating Issues

#### Bug Reports

Use the bug report template:

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce:
1. Start the application with config X
2. Call endpoint Y
3. See error

**Expected behavior**
What you expected to happen.

**Actual behavior**
What actually happened.

**Environment**
- OS: [e.g., macOS 14.0]
- Java version: [e.g., 17.0.8]
- Application version: [e.g., 1.0.0]

**Logs**
```
Paste relevant logs here
```

**Additional context**
Any other relevant information.
```

#### Feature Requests

Use the feature request template:

```markdown
**Is your feature request related to a problem?**
A clear description of the problem.

**Describe the solution you'd like**
What you want to happen.

**Describe alternatives you've considered**
Other solutions you've thought about.

**Additional context**
Any other relevant information.
```

### Working on Issues

1. **Comment on the issue** - Let others know you're working on it
2. **Ask questions** - If anything is unclear
3. **Link your PR** - Reference the issue in your PR

---

## Development Workflow

### Typical Workflow

```bash
# 1. Update your fork
git fetch upstream
git checkout main
git merge upstream/main

# 2. Create feature branch
git checkout -b feature/my-feature

# 3. Make changes
# ... edit files ...

# 4. Test changes
mvn test

# 5. Commit changes
git add .
git commit -m "feat: add my feature"

# 6. Push to your fork
git push origin feature/my-feature

# 7. Create pull request on GitHub

# 8. Address review feedback
# ... make changes ...
git add .
git commit -m "fix: address review feedback"
git push origin feature/my-feature

# 9. After merge, clean up
git checkout main
git pull upstream main
git branch -d feature/my-feature
```

---

## Project Structure

Understanding the codebase:

```
src/main/java/com/example/linkedin/
├── LinkedInCommentResponderApplication.java  # Main application
├── model/                                     # Domain models
│   ├── Comment.java
│   ├── Post.java
│   └── ...
├── client/                                    # External API clients
│   └── LinkedInApiClient.java
├── agent/                                     # AI/LLM components
│   ├── LLMAgent.java
│   └── OpenAILLMAgent.java
├── repository/                                # Data persistence
│   ├── StorageRepository.java
│   └── FileBasedStorageRepository.java
├── orchestrator/                              # Workflow coordination
│   └── WorkflowOrchestrator.java
├── api/                                       # REST API controllers
│   └── ManagementController.java
├── config/                                    # Configuration
│   └── ConfigurationValidator.java
├── error/                                     # Error handling
│   ├── ErrorHandler.java
│   └── ErrorNotificationService.java
└── resilience/                                # Circuit breakers
    ├── CircuitBreaker.java
    └── GracefulDegradationHandler.java
```

---

## Resources

### Documentation

- [README.md](README.md) - Project overview
- [QUICKSTART.md](QUICKSTART.md) - Quick start guide
- [CONFIGURATION.md](CONFIGURATION.md) - Configuration reference
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API reference
- [design.md](.kiro/specs/linkedin-comment-responder/design.md) - Design document

### External Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [LinkedIn API Documentation](https://docs.microsoft.com/en-us/linkedin/)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [jqwik Documentation](https://jqwik.net/docs/current/user-guide.html)

---

## Getting Help

### Questions?

- **Documentation**: Check existing docs first
- **Issues**: Search existing issues
- **Discussions**: Use GitHub Discussions for questions
- **Email**: [maintainer-email@example.com]

### Stuck?

- Review the [TROUBLESHOOTING.md](TROUBLESHOOTING.md) guide
- Check logs in `logs/application.log`
- Enable debug logging: `--logging.level.com.example.linkedin=DEBUG`
- Ask in GitHub Discussions

---

## Recognition

Contributors will be:
- Listed in the project README
- Credited in release notes
- Acknowledged in commit history

Thank you for contributing! 🎉

---

**Last Updated**: December 2024
