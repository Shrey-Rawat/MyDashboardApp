# Contributing to Best Productivity App

Thank you for your interest in contributing to the Best Productivity App! This document provides guidelines and information to help you contribute effectively.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Contributing Process](#contributing-process)
- [Coding Standards](#coding-standards)
- [Testing Requirements](#testing-requirements)
- [Documentation](#documentation)
- [Getting Help](#getting-help)

## Code of Conduct

This project follows our [Code of Conduct](.github/CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## Getting Started

### Prerequisites

Before contributing, ensure you have:
- Android Studio Arctic Fox or later
- OpenJDK 17 or later  
- Git
- Basic knowledge of Kotlin and Android development
- Understanding of Jetpack Compose

### First Steps

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/yourusername/MyDashboardApp.git
   cd MyDashboardApp
   ```
3. **Set up the upstream remote**:
   ```bash
   git remote add upstream https://github.com/original-owner/MyDashboardApp.git
   ```
4. **Install Git hooks** (recommended):
   ```bash
   ./scripts/install-hooks.sh
   ```

## Development Setup

### Build Instructions

Follow the complete [build instructions](docs/build-instructions.md) for detailed setup.

Quick start:
```bash
./gradlew assembleDebug
```

### Project Structure

The project uses a multi-module architecture:
- `app/` - Main application module
- `core/` - Shared utilities and components
- `data/` - Database and repository layer
- `feature-*/` - Individual feature modules
- `auth/`, `billing/`, `sync/`, `export/` - Supporting modules

See [Architecture Decision Records](docs/architecture-decisions/) for detailed architectural information.

## Contributing Process

### 1. Choose an Issue

- Browse [open issues](https://github.com/original-owner/MyDashboardApp/issues)
- Look for issues labeled `good first issue` for beginners
- Comment on the issue to express interest before starting work
- Wait for maintainer acknowledgment before proceeding

### 2. Create a Branch

Create a feature branch from `develop`:
```bash
git checkout develop
git pull upstream develop
git checkout -b feature/your-feature-name
```

### 3. Make Your Changes

- Follow our [coding standards](#coding-standards)
- Write tests for new functionality
- Update documentation as needed
- Keep commits atomic and focused

### 4. Test Your Changes

```bash
# Run unit tests
./gradlew test

# Run linting
./gradlew ktlintCheck

# Run static analysis
./gradlew detekt

# Build all variants
./gradlew assembleDebug
```

### 5. Submit a Pull Request

1. Push your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
2. Open a Pull Request against the `develop` branch
3. Fill out the PR template completely
4. Link any related issues

### 6. Code Review Process

- Maintainers will review your PR
- Address any feedback promptly
- Keep your branch updated with `develop`
- Once approved, maintainers will merge your PR

## Coding Standards

### Kotlin Style

We follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) with these additions:

#### Naming
- Use descriptive names for classes, functions, and variables
- Use PascalCase for class names
- Use camelCase for function and variable names
- Use SCREAMING_SNAKE_CASE for constants

#### Code Organization
- Organize imports alphabetically
- Remove unused imports
- Use meaningful package names that reflect functionality

#### Comments
- Write KDoc for public APIs
- Use inline comments sparingly and only when necessary
- Explain "why" not "what" in comments

### Architecture Guidelines

#### MVVM Pattern
- ViewModels handle business logic and state
- Repositories abstract data access
- Use cases (optional) for complex business operations

#### Jetpack Compose
- Follow [Compose best practices](https://developer.android.com/jetpack/compose/mental-model)
- Use `remember` and `rememberSaveable` appropriately
- Optimize recompositions
- Extract reusable composables

#### Dependency Injection
- Use Hilt for dependency injection
- Define dependencies in appropriate modules
- Avoid field injection in favor of constructor injection

#### Error Handling
- Use Result types for operations that can fail
- Provide meaningful error messages
- Log errors appropriately

### Module Guidelines

#### Feature Modules
- Each feature should be self-contained
- Follow the [feature module guide](docs/adding-feature-modules.md)
- Don't depend on other feature modules directly
- Use the `core` module for shared functionality

#### Testing
- Write unit tests for ViewModels and repositories
- Write UI tests for user-facing functionality
- Aim for meaningful test coverage, not just high percentages
- Use descriptive test names that explain the scenario

## Testing Requirements

### Unit Tests
- **Required** for all new ViewModels
- **Required** for all new repository methods
- **Recommended** for complex utility functions

### Integration Tests
- **Required** for database operations
- **Required** for API interactions
- **Recommended** for cross-module functionality

### UI Tests
- **Required** for new screens and user flows
- **Required** for complex UI interactions
- Use Compose testing utilities

### Test Naming Convention
```kotlin
`function name - given condition - expected result`

@Test
fun `getUserData - when user exists - returns user successfully`() {
    // Test implementation
}
```

## Documentation

### Code Documentation
- Write KDoc for public APIs
- Document complex algorithms or business logic
- Include usage examples where helpful

### Feature Documentation
- Create/update feature design documents for major changes
- Update architecture decision records (ADRs) for architectural changes
- Update README for setup or usage changes

### API Documentation
- Document all public APIs
- Include request/response examples
- Update API documentation for breaking changes

## Pull Request Guidelines

### PR Title
Use conventional commit format:
```
type(scope): description

Examples:
feat(nutrition): add meal planning feature
fix(auth): resolve login state persistence issue
docs(readme): update installation instructions
```

### PR Description
- Clearly describe what changes you made
- Explain why the changes were necessary
- Include screenshots for UI changes
- Reference related issues using "Closes #123" or "Fixes #123"

### PR Size
- Keep PRs focused and reasonably sized
- Large PRs should be discussed with maintainers first
- Consider breaking large changes into multiple PRs

## Getting Help

### Communication Channels
- **GitHub Issues**: For bug reports and feature requests
- **GitHub Discussions**: For questions and general discussion
- **Code Review Comments**: For feedback on specific changes

### Resources
- [Build Instructions](docs/build-instructions.md)
- [Architecture Decisions](docs/architecture-decisions/)
- [Feature Design Documents](docs/feature-designs/)
- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)

### Common Issues

#### Build Problems
1. Check that you have the correct Android SDK installed
2. Ensure you're using OpenJDK 17
3. Try cleaning the project: `./gradlew clean`
4. Check for dependency conflicts

#### Test Failures
1. Ensure you're on the latest `develop` branch
2. Run tests individually to isolate issues
3. Check for environment-specific test failures

#### Code Style Issues
1. Run `./gradlew ktlintFormat` to auto-format
2. Run `./gradlew detekt` to check for issues
3. Ensure your IDE is configured for the project style

## Recognition

Contributors who make significant contributions will be:
- Added to the CONTRIBUTORS.md file
- Mentioned in release notes
- Given appropriate credit in commit messages

Thank you for contributing to Best Productivity App!
