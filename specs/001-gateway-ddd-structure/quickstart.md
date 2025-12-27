# Quickstart: Spring Cloud Gateway DDD Project

**Feature**: 001-gateway-ddd-structure
**Date**: 2025-12-27

## Prerequisites

- Java 21 (OpenJDK recommended)
- Maven 3.9+
- Backend services running (or mock servers):
  - op-stack-auth (port 8084)
  - op-stack-service (port 8081)
  - op-stack-executor (port 8082)
  - op-stack-tools (port 8083)

## Quick Build & Run

```bash
# Clone and navigate to project
cd op-stack-gateway

# Build all modules
mvn clean install

# Run the gateway
cd bootstrap
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The gateway will start on port **8080** by default.

## Project Structure Overview

```
op-stack-gateway/
├── common/          # Shared DTOs, exceptions, constants
├── domain/          # Domain models and service interfaces
├── application/     # Application services and DTOs
├── infrastructure/  # Gateway filters, HTTP clients
├── interface/       # Management endpoints (optional)
└── bootstrap/       # Main application, configuration
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `OP_STACK_AUTH_URL` | `http://localhost:8084` | Auth service URL |
| `OP_STACK_SERVICE_URL` | `http://localhost:8081` | Main service URL |
| `OP_STACK_EXECUTOR_URL` | `http://localhost:8082` | Executor service URL |
| `OP_STACK_TOOLS_URL` | `http://localhost:8083` | Tools service URL |
| `SERVER_PORT` | `8080` | Gateway port |

### Profiles

- `local`: Development configuration with localhost backends
- `prod`: Production configuration with external service URLs

## Testing the Gateway

### 1. Public Routes (No Auth Required)

```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "secret123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "secret123"}'
```

### 2. Protected Routes (Auth Required)

```bash
# Get a JWT token from login response first
TOKEN="your-jwt-token-here"

# Call a protected service endpoint
curl -X POST http://localhost:8080/api/service/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"action": "list"}'

# The gateway will:
# 1. Validate the token with op-stack-auth
# 2. Inject operatorId into the request body
# 3. Forward to op-stack-service
```

### 3. Error Responses

```bash
# Without token (401 Unauthorized)
curl -X POST http://localhost:8080/api/service/users \
  -H "Content-Type: application/json" \
  -d '{"action": "list"}'

# Response:
# {
#   "code": "UNAUTHORIZED",
#   "message": "Authorization header is required",
#   "path": "/api/service/users",
#   "timestamp": "2025-12-27T10:00:00Z"
# }
```

## Development Workflow

### Adding a New Route

1. Edit `bootstrap/src/main/resources/application.yml`
2. Add route configuration under `spring.cloud.gateway.routes`
3. Restart the gateway

### Adding a New Filter

1. Create filter class in `infrastructure/src/main/java/.../filter/`
2. Implement `GlobalFilter` or `GatewayFilter` interface
3. Register in `bootstrap/src/main/java/.../config/FilterConfig.java`

### Running Tests

```bash
# Run all tests
mvn test

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Run with coverage
mvn test jacoco:report
```

## Common Issues

### 1. Connection Refused to Backend

**Problem**: `Connection refused` when calling backend services

**Solution**: Ensure backend services are running on configured ports, or use mock servers for development.

### 2. 401 on All Requests

**Problem**: Getting 401 even with valid-looking token

**Causes**:
- Token expired
- Token not prefixed with "Bearer "
- Auth service not responding

**Debug**: Check gateway logs for authentication filter output.

### 3. operatorId Not Appearing in Backend

**Problem**: Backend receives request without operatorId field

**Causes**:
- Request body not valid JSON
- Content-Type not `application/json`

**Solution**: Ensure requests have proper Content-Type header and valid JSON body.

## Architecture Decisions

1. **Why DDD modules?** Consistency with op-stack-service architecture
2. **Why no JWT caching?** Per clarification, every request validates with auth service
3. **Why body injection vs headers?** Backend services expect operatorId in JSON body
4. **Why POST-only routes?** Backend APIs use POST for all operations

## Next Steps

After basic gateway is working:

1. Add circuit breaker (Resilience4j) for backend failures
2. Add rate limiting filter
3. Add request/response logging filter
4. Set up Prometheus metrics endpoint
