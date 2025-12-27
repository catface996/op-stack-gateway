# Feature Specification: Spring Cloud Gateway DDD Project Structure

**Feature Branch**: `001-gateway-ddd-structure`
**Created**: 2025-12-27
**Status**: Draft
**Input**: User description: "参考 ../op-stack-service 目录中的java项目结构，在当前项目中创建一个DDD工程结构。这个项目是一个使用Spring Cloud Gateway技术框架做http请求反向代理的项目，并且要预留对请求的url做鉴权的扩展性。目前后端有op-stack-service，op-stack-executor和 op-stack-tools这三个服务要被代理。op-stack-auth服务是用来注册账号，登录账号，以及对登录信息的jwt做校验，并获取对应的userId的鉴权服务。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gateway Proxies Backend Services (Priority: P1)

A user sends an HTTP request to the gateway, and the gateway routes the request to the appropriate backend service (op-stack-service, op-stack-executor, or op-stack-tools) based on the request URL path.

**Why this priority**: This is the core functionality of the gateway - without routing capability, the gateway serves no purpose. All other features depend on this working correctly.

**Independent Test**: Can be fully tested by sending HTTP requests to the gateway and verifying they reach the correct backend service. Delivers the fundamental value of centralizing API access.

**Acceptance Scenarios**:

1. **Given** a user sends a request to `/api/service/**`, **When** the gateway receives the request, **Then** the request is forwarded to the op-stack-service backend
2. **Given** a user sends a request to `/api/executor/**`, **When** the gateway receives the request, **Then** the request is forwarded to the op-stack-executor backend
3. **Given** a user sends a request to `/api/tools/**`, **When** the gateway receives the request, **Then** the request is forwarded to the op-stack-tools backend
4. **Given** a user sends a request to an unknown path, **When** the gateway receives the request, **Then** the gateway returns a 404 Not Found response

---

### User Story 2 - JWT Authentication for Protected Routes (Priority: P2)

A user sends an HTTP request with a JWT token in the Authorization header, and the gateway validates the token by calling the op-stack-auth service before forwarding the request to the backend service.

**Why this priority**: Authentication ensures only authorized users can access protected resources. This is the second most critical feature after basic routing.

**Independent Test**: Can be tested by sending requests with valid/invalid JWT tokens and verifying the gateway correctly validates them against op-stack-auth service.

**Acceptance Scenarios**:

1. **Given** a user sends a request with a valid JWT token to a protected route, **When** the gateway validates the token via op-stack-auth, **Then** the request is forwarded to the backend with `operatorId` injected into the JSON body
2. **Given** a user sends a request with an expired JWT token, **When** the gateway validates the token, **Then** the gateway returns a 401 Unauthorized response
3. **Given** a user sends a request without a JWT token to a protected route, **When** the gateway processes the request, **Then** the gateway returns a 401 Unauthorized response
4. **Given** a user sends a request with an invalid JWT token, **When** the gateway validates the token, **Then** the gateway returns a 401 Unauthorized response

---

### User Story 3 - Public Routes Without Authentication (Priority: P2)

A user sends an HTTP request to public routes (registration, login) without needing a JWT token.

**Why this priority**: Users need to be able to register and login without existing authentication. This works alongside JWT authentication.

**Independent Test**: Can be tested by sending requests to auth endpoints without tokens and verifying they are processed successfully.

**Acceptance Scenarios**:

1. **Given** a user sends a registration request to `/api/auth/register`, **When** the gateway receives the request, **Then** the request is forwarded to op-stack-auth without requiring authentication
2. **Given** a user sends a login request to `/api/auth/login`, **When** the gateway receives the request, **Then** the request is forwarded to op-stack-auth without requiring authentication

---

### User Story 4 - DDD Module Structure Setup (Priority: P1)

A developer needs a clean, maintainable codebase organized according to DDD principles with clear separation of concerns.

**Why this priority**: The project structure is foundational - it determines code organization, maintainability, and future extensibility.

**Independent Test**: Can be verified by checking that all modules exist, compile successfully, and follow the DDD layered architecture pattern.

**Acceptance Scenarios**:

1. **Given** a developer clones the repository, **When** they run the build, **Then** all modules compile successfully
2. **Given** a developer needs to add new routing logic, **When** they look for the appropriate location, **Then** they find it in the domain module
3. **Given** a developer needs to add external service integration, **When** they look for the appropriate location, **Then** they find it in the infrastructure module
4. **Given** a developer needs to add new API endpoints, **When** they look for the appropriate location, **Then** they find it in the interface module

---

### Edge Cases

- What happens when a backend service is unavailable? The gateway returns a 503 Service Unavailable response with a meaningful error message.
- What happens when the op-stack-auth service is unavailable during token validation? The gateway returns a 503 Service Unavailable response, not allowing unauthenticated access.
- What happens when multiple routes match a request? The most specific route takes precedence.
- What happens when the JWT token format is malformed (not three parts separated by dots)? The gateway returns a 401 Unauthorized without calling the auth service.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST route HTTP requests to op-stack-service based on URL path pattern `/api/service/**`
- **FR-002**: System MUST route HTTP requests to op-stack-executor based on URL path pattern `/api/executor/**`
- **FR-003**: System MUST route HTTP requests to op-stack-tools based on URL path pattern `/api/tools/**`
- **FR-004**: System MUST route HTTP requests to op-stack-auth based on URL path pattern `/api/auth/**`
- **FR-005**: System MUST validate JWT tokens by calling op-stack-auth service for every request to protected routes (no caching)
- **FR-006**: System MUST extract userId from validated JWT tokens and inject it as `operatorId` field into the request body
- **FR-007**: System MUST allow unauthenticated access to public routes (registration, login)
- **FR-008**: System MUST return 401 Unauthorized for requests with invalid, expired, or missing JWT tokens on protected routes
- **FR-009**: System MUST return 503 Service Unavailable when backend services are unreachable
- **FR-010**: System MUST follow DDD module structure with application, domain, infrastructure, interface, bootstrap, and common modules
- **FR-011**: System MUST provide extensibility for adding custom authentication/authorization filters
- **FR-012**: System MUST strip sensitive headers (e.g., Authorization) before forwarding requests to backend services (optional configuration)
- **FR-013**: System MUST modify JSON request body to add `operatorId` field with userId value before forwarding to backend services
- **FR-014**: System MUST log access logs for all requests including request path, HTTP status code, and response time

### Key Entities

- **Route**: Represents a mapping between a URL pattern and a backend service, including predicates (path, method) and filters (authentication, header manipulation)
- **AuthenticationResult**: Contains the result of JWT validation including userId and token validity status
- **GatewayFilter**: Represents a filter that can be applied to requests (authentication filter, rate limiting filter, logging filter)
- **BackendService**: Represents a downstream service that the gateway proxies to, including service identifier and base URL

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Gateway successfully routes 99.9% of requests to the correct backend service
- **SC-002**: JWT validation adds no more than 100ms latency to request processing under normal load
- **SC-003**: Developers can add a new route configuration in under 5 minutes
- **SC-004**: New authentication/authorization filters can be added without modifying existing code (Open/Closed Principle)
- **SC-005**: All four backend services (op-stack-service, op-stack-executor, op-stack-tools, op-stack-auth) are accessible through the gateway
- **SC-006**: 100% of requests to protected routes without valid authentication are rejected
- **SC-007**: Project compiles and starts successfully with `mvn spring-boot:run`

## Clarifications

### Session 2025-12-27

- Q: How should the gateway communicate with op-stack-auth for JWT validation? → A: HTTP call to auth service endpoint (e.g., POST /auth/validate with token)
- Q: What level of request logging/observability is required? → A: Standard - Access logs with request path, status code, and response time
- Q: Should the gateway cache JWT validation results? → A: No caching - validate every request with auth service
- Q: How should userId be passed to backend services? → A: Inject into request body as `operatorId` field (all backend requests are POST with JSON body)

## Assumptions

1. Backend services (op-stack-service, op-stack-executor, op-stack-tools, op-stack-auth) are already running and accessible
2. op-stack-auth service provides an HTTP endpoint (e.g., POST /auth/validate) to validate JWT tokens and return userId
3. JWT tokens follow standard format with Bearer prefix in Authorization header
4. All backend services use HTTP (HTTPS termination handled at load balancer level)
5. Service discovery is not required initially - backend service URLs are configured statically
6. The gateway runs as a single instance (horizontal scaling is out of scope for initial implementation)
7. op-stack-auth service responds to validation requests within 50ms (to meet gateway's 100ms latency target without caching)
8. All backend service API endpoints accept POST requests with JSON body containing `operatorId` field for authenticated user identification

## Out of Scope

- Rate limiting (can be added later as a filter)
- Request/Response caching
- JWT validation result caching (every request validates against auth service)
- Circuit breaker pattern (can be added via Resilience4j later)
- Service discovery (Eureka, Consul integration)
- WebSocket proxy support
- gRPC proxy support
- Multi-tenancy support
