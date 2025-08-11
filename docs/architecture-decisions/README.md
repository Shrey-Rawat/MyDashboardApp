# Architectural Decision Records (ADRs)

This directory contains Architectural Decision Records (ADRs) for the Best Productivity App project. ADRs document important architectural decisions made during the development of the project.

## What is an ADR?

An Architectural Decision Record (ADR) is a document that captures an important architectural decision made along with its context and consequences.

## ADR Format

Each ADR should follow this template structure:

```markdown
# ADR-XXXX: [Short descriptive title]

## Status
[Proposed | Accepted | Deprecated | Superseded]

## Context
[Describe the forces at play, including technological, political, social, and project local. These forces are probably in tension, and should be called out as such.]

## Decision
[State the architecture decision and provide detailed justification.]

## Consequences
[Describe the resulting context, after applying the decision. All consequences should be listed here, not just the "positive" ones.]
```

## Current ADRs

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-0001](adr-0001-multi-module-architecture.md) | Multi-Module Architecture | Accepted |
| [ADR-0002](adr-0002-gradle-convention-plugins.md) | Gradle Convention Plugins | Accepted |
| [ADR-0003](adr-0003-jetpack-compose-ui.md) | Jetpack Compose for UI | Accepted |
| [ADR-0004](adr-0004-mvvm-architecture-pattern.md) | MVVM Architecture Pattern | Accepted |
| [ADR-0005](adr-0005-room-database-choice.md) | Room Database for Local Storage | Accepted |
| [ADR-0006](adr-0006-kotlin-coroutines-async.md) | Kotlin Coroutines for Asynchronous Programming | Accepted |
| [ADR-0007](adr-0007-dagger-hilt-di.md) | Dagger Hilt for Dependency Injection | Accepted |

## Creating New ADRs

1. Copy the ADR template from `docs/templates/adr-template.md`
2. Create a new file with the format `adr-XXXX-short-title.md`
3. Fill in all sections of the template
4. Submit for review through pull request
5. Update this README with the new ADR entry

## ADR Lifecycle

- **Proposed**: The ADR is under discussion
- **Accepted**: The decision has been made and should be followed
- **Deprecated**: The decision is no longer relevant but kept for historical context
- **Superseded**: Replaced by a newer ADR (reference the superseding ADR)

## References

- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [ADR GitHub Organization](https://adr.github.io/)
