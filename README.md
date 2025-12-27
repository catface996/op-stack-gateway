# op-stack-gateway

Spring Cloud Gateway with DDD architecture for proxying HTTP requests to op-stack backend services.

## Features

- **HTTP Reverse Proxy**: Routes requests to op-stack-service, op-stack-executor, op-stack-tools, op-stack-auth
- **JWT Authentication**: Validates tokens via op-stack-auth service
- **operatorId Injection**: Injects authenticated user's operatorId into request body
- **Public Routes**: Registration and login endpoints bypass authentication
- **Access Logging**: Logs all incoming requests with timing and routing information

## Requirements

- Java 21
- Maven 3.9+

## Quick Start

```bash
# Build all modules
mvn clean install

# Run the gateway (local profile)
mvn spring-boot:run -pl bootstrap -Dspring-boot.run.profiles=local
```

Gateway starts on port **8080**.

## Project Structure

```
op-stack-gateway/
├── common/          # Shared DTOs, exceptions, constants
├── domain/          # Domain models and service interfaces
├── application/     # Application services and DTOs
├── infrastructure/  # Gateway filters, HTTP clients
├── interface/       # Management endpoints
└── bootstrap/       # Application entry point, configuration
```

## Backend Services

| Service | Path Prefix | Default Port |
|---------|-------------|--------------|
| op-stack-service | `/api/service/**` | 8081 |
| op-stack-executor | `/api/executor/**` | 8082 |
| op-stack-tools | `/api/tools/**` | 8083 |
| op-stack-auth | `/api/auth/**` | 8084 |

## Public Routes

These routes do not require authentication:

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

## Configuration

Configuration files are in `bootstrap/src/main/resources/`:

- `application.yml` - Base configuration
- `application-local.yml` - Local development
- `application-prod.yml` - Production environment

## Version Compatibility

- Spring Boot: 3.4.1
- Spring Cloud: 2025.0.0

Compatible with op-stack-service versions.
