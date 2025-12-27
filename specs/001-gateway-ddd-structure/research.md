# Research: Spring Cloud Gateway DDD Project Structure

**Feature**: 001-gateway-ddd-structure
**Date**: 2025-12-27

## Research Topics

### 1. Spring Cloud Gateway Filter Chain Architecture

**Decision**: Use Global Filters for authentication and request body modification

**Rationale**:
- Spring Cloud Gateway provides `GlobalFilter` interface for cross-cutting concerns
- Global filters execute for all routes, ideal for authentication
- Order can be controlled via `@Order` annotation or `Ordered` interface
- Reactive WebFlux model requires non-blocking filter implementation

**Alternatives Considered**:
- Route-specific `GatewayFilter`: Rejected because authentication applies to most routes
- Spring Security WebFlux: Rejected because auth validation is delegated to external service

### 2. Request Body Modification in Reactive Gateway

**Decision**: Use `ModifyRequestBodyGatewayFilterFactory` pattern with `ServerHttpRequestDecorator`

**Rationale**:
- Spring Cloud Gateway operates on reactive streams; request body is read once
- `ServerHttpRequestDecorator` allows body modification before forwarding
- Must cache body bytes using `DataBufferUtils` for JSON parsing and modification
- Use Jackson for JSON parsing/serialization in reactive context

**Alternatives Considered**:
- Rewriting entire request: More complex, unnecessary for adding single field
- Using headers instead of body: Rejected per clarification (operatorId in body)

### 3. WebClient Configuration for Auth Service Calls

**Decision**: Use Spring WebClient with connection pooling and timeout configuration

**Rationale**:
- WebClient is the reactive HTTP client for Spring WebFlux
- Connection pooling reduces latency for repeated calls to auth service
- Configurable timeouts ensure 50ms target can be monitored
- Built-in retry mechanism available if needed later

**Configuration Approach**:
```yaml
webclient:
  auth-service:
    base-url: ${AUTH_SERVICE_URL:http://localhost:8081}
    connect-timeout: 5000
    read-timeout: 50000
```

### 4. DDD Module Dependencies in Spring Cloud Gateway

**Decision**: Follow strict dependency direction: bootstrap → infrastructure → application → domain ← common

**Rationale**:
- Matches op-stack-service architecture
- Domain layer has no external dependencies (pure Java)
- Infrastructure implements domain interfaces
- Application orchestrates domain services
- Bootstrap wires everything together

**Module Dependency Matrix**:
| Module | Depends On |
|--------|------------|
| common | (none) |
| domain | common |
| application | domain, common |
| infrastructure | application, domain, common |
| interface | application, common |
| bootstrap | all modules |

### 5. Route Configuration Strategy

**Decision**: YAML-based route configuration with programmatic filter registration

**Rationale**:
- YAML provides easy configuration without code changes
- Programmatic filters allow complex logic (auth, body modification)
- Spring Cloud Gateway supports both approaches seamlessly
- Environment-specific YAML profiles for different backend URLs

**Route Patterns**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-route
          uri: ${SERVICE_URL:http://localhost:8082}
          predicates:
            - Path=/api/service/**
          filters:
            - StripPrefix=1
            - name: Authentication
            - name: OperatorIdInjection
```

### 6. Public Route Exclusion from Authentication

**Decision**: Use route metadata and filter order to skip authentication for public routes

**Rationale**:
- Route metadata can flag routes as public
- Authentication filter checks metadata before processing
- Clean separation between route definition and filter logic
- Easily extensible for future public routes

**Implementation Pattern**:
```yaml
routes:
  - id: auth-public
    uri: ${AUTH_SERVICE_URL}
    predicates:
      - Path=/api/auth/register,/api/auth/login
    metadata:
      public: true
```

### 7. Error Handling Strategy

**Decision**: Use `ErrorWebExceptionHandler` for consistent error responses

**Rationale**:
- Centralized error handling for all gateway errors
- Returns JSON error responses matching backend service format
- HTTP status codes: 401 (auth failure), 503 (service unavailable), 404 (not found)
- Logs error details for observability

**Error Response Format**:
```json
{
  "code": "UNAUTHORIZED",
  "message": "Invalid or expired token",
  "timestamp": "2025-12-27T10:00:00Z"
}
```

### 8. Access Logging Implementation

**Decision**: Custom `GlobalFilter` with `afterCommit` callback for response logging

**Rationale**:
- Spring Cloud Gateway doesn't have built-in access log format
- `afterCommit` ensures response status is available
- Structured logging with request path, status, duration
- Can use SLF4J MDC for request tracing

**Log Format**:
```
ACCESS | method=POST path=/api/service/users status=200 duration=45ms clientIp=192.168.1.1
```

## Technology Stack Summary

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.4.1 |
| Gateway | Spring Cloud Gateway | 2025.0.0 |
| Reactive | Spring WebFlux | 6.2.x |
| HTTP Client | WebClient | 6.2.x |
| JSON | Jackson | 2.17.x |
| Build | Maven | 3.9.x |
| Java | OpenJDK | 21 |
| Testing | JUnit 5 + WebTestClient | 5.10.x |

## Open Questions Resolved

All technical unknowns have been resolved through research. No remaining NEEDS CLARIFICATION items.
