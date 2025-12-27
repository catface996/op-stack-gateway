# Implementation Plan: Spring Cloud Gateway DDD Project Structure

**Branch**: `001-gateway-ddd-structure` | **Date**: 2025-12-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-gateway-ddd-structure/spec.md`

## Summary

Create a DDD-structured Spring Cloud Gateway project that serves as an HTTP reverse proxy for op-stack-service, op-stack-executor, op-stack-tools, and op-stack-auth backend services. The gateway will handle JWT authentication by calling op-stack-auth service and inject `operatorId` into request bodies before forwarding. The project structure follows the same DDD layered architecture as op-stack-service (common, domain, application, infrastructure, interface, bootstrap modules).

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.4.1, Spring Cloud Gateway 2025.0.0, Spring WebFlux (reactive)
**Storage**: N/A (stateless gateway, no persistence required)
**Testing**: JUnit 5, Spring Boot Test, WebTestClient
**Target Platform**: Linux server (Docker/Kubernetes)
**Project Type**: Multi-module Maven project (DDD architecture)
**Performance Goals**: ≤100ms latency for JWT validation, 99.9% routing success rate
**Constraints**: No JWT caching, all requests validated via auth service
**Scale/Scope**: Single instance initially, 4 backend services proxied

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution template has not been customized for this project. Proceeding with standard best practices:

| Principle | Status | Notes |
|-----------|--------|-------|
| Library-First | PASS | Each module is independently testable |
| Test-First | PASS | Integration tests planned for filter chain |
| Observability | PASS | Access logging with path, status, response time |
| Simplicity | PASS | Minimal complexity, YAGNI principles applied |

## Project Structure

### Documentation (this feature)

```text
specs/001-gateway-ddd-structure/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
op-stack-gateway/
├── pom.xml                          # Parent POM with dependency management
├── common/
│   ├── pom.xml
│   └── src/main/java/com/catface996/gateway/common/
│       ├── constants/               # Error codes, route constants
│       ├── dto/                     # Shared DTOs (ApiResponse, ErrorResponse)
│       ├── enums/                   # Error code enums
│       └── exception/               # Custom exceptions (AuthenticationException, etc.)
│
├── domain/
│   ├── pom.xml
│   └── src/main/java/com/catface996/gateway/domain/
│       ├── auth/                    # Authentication domain
│       │   ├── model/               # AuthenticationResult, TokenInfo
│       │   └── service/             # AuthenticationService interface
│       └── route/                   # Routing domain
│           ├── model/               # Route, BackendService
│           └── service/             # RouteService interface
│
├── application/
│   ├── pom.xml
│   └── src/main/java/com/catface996/gateway/application/
│       ├── auth/                    # Authentication application services
│       │   ├── dto/                 # AuthValidateRequest, AuthValidateResponse
│       │   └── service/             # AuthenticationAppService
│       └── filter/                  # Gateway filter application services
│           └── service/             # FilterChainService
│
├── infrastructure/
│   ├── pom.xml
│   └── src/main/java/com/catface996/gateway/infrastructure/
│       ├── auth/                    # Auth service HTTP client
│       │   └── client/              # AuthServiceClient (WebClient)
│       ├── filter/                  # Gateway filter implementations
│       │   ├── AuthenticationGatewayFilter.java
│       │   ├── OperatorIdInjectionFilter.java
│       │   └── AccessLogFilter.java
│       └── config/                  # Infrastructure configurations
│           └── WebClientConfig.java
│
├── interface/
│   ├── pom.xml
│   └── src/main/java/com/catface996/gateway/interfaces/
│       └── http/                    # Management/health endpoints if needed
│
└── bootstrap/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/catface996/gateway/bootstrap/
        │   │   ├── GatewayApplication.java
        │   │   └── config/
        │   │       ├── RouteConfig.java          # Route definitions
        │   │       └── FilterConfig.java         # Filter chain configuration
        │   └── resources/
        │       ├── application.yml               # Main configuration
        │       ├── application-local.yml         # Local profile
        │       └── application-prod.yml          # Production profile
        └── test/
            └── java/com/catface996/gateway/
                └── integration/                  # Integration tests
```

**Structure Decision**: DDD multi-module Maven project following op-stack-service architecture pattern. The gateway-specific logic resides in:
- `domain/auth`: Authentication domain model and service interfaces
- `infrastructure/filter`: Spring Cloud Gateway filter implementations
- `bootstrap/config`: Route and filter chain configuration

## Complexity Tracking

No constitution violations identified. The DDD structure aligns with the existing op-stack-service architecture.
