# Data Model: Spring Cloud Gateway DDD Project Structure

**Feature**: 001-gateway-ddd-structure
**Date**: 2025-12-27

## Overview

This document defines the domain entities, value objects, and DTOs for the Spring Cloud Gateway project. Since the gateway is stateless (no persistence), these models represent in-memory structures for request processing.

## Domain Layer Entities

### 1. AuthenticationResult

**Location**: `domain/src/main/java/com/catface996/gateway/domain/auth/model/AuthenticationResult.java`

**Purpose**: Represents the result of JWT token validation from op-stack-auth service.

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| authenticated | boolean | Whether the token is valid | Required |
| userId | Long | User ID extracted from token | Nullable (null if not authenticated) |
| errorCode | String | Error code if authentication failed | Nullable |
| errorMessage | String | Human-readable error message | Nullable |

**Validation Rules**:
- If `authenticated=true`, `userId` must not be null
- If `authenticated=false`, `errorCode` should be present

### 2. TokenInfo

**Location**: `domain/src/main/java/com/catface996/gateway/domain/auth/model/TokenInfo.java`

**Purpose**: Represents parsed JWT token information before validation.

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| rawToken | String | The raw JWT token string | Required, not blank |
| tokenType | String | Token type (Bearer) | Required, default "Bearer" |

**Validation Rules**:
- `rawToken` must be in valid JWT format (three base64 segments separated by dots)

### 3. BackendService (Value Object)

**Location**: `domain/src/main/java/com/catface996/gateway/domain/route/model/BackendService.java`

**Purpose**: Represents a downstream backend service configuration.

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| serviceId | String | Unique identifier (e.g., "op-stack-service") | Required, unique |
| baseUrl | String | Base URL of the service | Required, valid URL |
| pathPrefix | String | URL path prefix to match (e.g., "/api/service") | Required |
| requiresAuth | boolean | Whether authentication is required | Default: true |

## Application Layer DTOs

### 4. AuthValidateRequest

**Location**: `application/src/main/java/com/catface996/gateway/application/auth/dto/AuthValidateRequest.java`

**Purpose**: Request DTO for calling op-stack-auth validation endpoint.

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| token | String | JWT token to validate | Required |

### 5. AuthValidateResponse

**Location**: `application/src/main/java/com/catface996/gateway/application/auth/dto/AuthValidateResponse.java`

**Purpose**: Response DTO from op-stack-auth validation endpoint.

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| valid | boolean | Whether the token is valid | Required |
| userId | Long | User ID if valid | Nullable |
| code | String | Error code if invalid | Nullable |
| message | String | Error message if invalid | Nullable |

## Common Layer DTOs

### 6. ApiResponse<T>

**Location**: `common/src/main/java/com/catface996/gateway/common/dto/ApiResponse.java`

**Purpose**: Standard API response wrapper for gateway endpoints.

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| success | boolean | Whether the request succeeded | Required |
| code | String | Response code | Required |
| message | String | Response message | Required |
| data | T | Response payload | Nullable |
| timestamp | LocalDateTime | Response timestamp | Required, auto-generated |

### 7. ErrorResponse

**Location**: `common/src/main/java/com/catface996/gateway/common/dto/ErrorResponse.java`

**Purpose**: Standard error response for gateway errors (401, 503, etc.).

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| code | String | Error code (e.g., "UNAUTHORIZED") | Required |
| message | String | Human-readable error message | Required |
| path | String | Request path that caused the error | Required |
| timestamp | LocalDateTime | Error timestamp | Required, auto-generated |

## Enums

### 8. ErrorCode

**Location**: `common/src/main/java/com/catface996/gateway/common/enums/ErrorCode.java`

**Purpose**: Enumeration of gateway error codes.

| Value | HTTP Status | Description |
|-------|-------------|-------------|
| UNAUTHORIZED | 401 | Invalid, expired, or missing JWT token |
| FORBIDDEN | 403 | Token valid but insufficient permissions |
| SERVICE_UNAVAILABLE | 503 | Backend service unreachable |
| BAD_GATEWAY | 502 | Invalid response from backend service |
| NOT_FOUND | 404 | No route matches the request path |
| INTERNAL_ERROR | 500 | Unexpected gateway error |

## Entity Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                        Request Flow                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  HTTP Request                                                    │
│       │                                                          │
│       ▼                                                          │
│  ┌─────────────┐    extract    ┌─────────────┐                  │
│  │ TokenInfo   │◄──────────────│ Auth Header │                  │
│  └─────────────┘               └─────────────┘                  │
│       │                                                          │
│       │ validate via HTTP                                        │
│       ▼                                                          │
│  ┌──────────────────┐         ┌─────────────────────┐           │
│  │AuthValidateRequest│────────►│  op-stack-auth      │           │
│  └──────────────────┘         │  /auth/validate     │           │
│                               └─────────────────────┘           │
│       │                              │                           │
│       │                              ▼                           │
│       │                       ┌─────────────────────┐           │
│       │                       │AuthValidateResponse │           │
│       │                       └─────────────────────┘           │
│       │                              │                           │
│       ▼                              ▼                           │
│  ┌────────────────────┐    maps to   ┌────────────────────┐    │
│  │AuthenticationResult│◄─────────────│   userId/valid     │    │
│  └────────────────────┘              └────────────────────┘    │
│       │                                                          │
│       │ if authenticated                                         │
│       ▼                                                          │
│  ┌─────────────────────────────────────────────────────┐        │
│  │ Request Body + operatorId: userId                    │        │
│  │ Forward to BackendService                            │        │
│  └─────────────────────────────────────────────────────┘        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## State Transitions

### Authentication Flow States

```
┌─────────────┐
│   START     │
└──────┬──────┘
       │
       ▼
┌─────────────┐    No Auth Header    ┌─────────────┐
│ Check Auth  │───────────────────────► UNAUTHORIZED│
│   Header    │                       └─────────────┘
└──────┬──────┘
       │ Has Header
       ▼
┌─────────────┐    Invalid Format    ┌─────────────┐
│ Parse Token │───────────────────────► UNAUTHORIZED│
│             │                       └─────────────┘
└──────┬──────┘
       │ Valid Format
       ▼
┌─────────────┐    Service Down      ┌─────────────┐
│ Call Auth   │───────────────────────► SVC_UNAVAIL │
│  Service    │                       └─────────────┘
└──────┬──────┘
       │
       ▼
┌─────────────┐    Invalid/Expired   ┌─────────────┐
│ Check       │───────────────────────► UNAUTHORIZED│
│  Response   │                       └─────────────┘
└──────┬──────┘
       │ Valid
       ▼
┌─────────────┐
│AUTHENTICATED│
└─────────────┘
```

## Notes

- No database persistence required; all models are transient
- AuthenticationResult is the core domain model; DTOs are adapters
- BackendService configuration is loaded from YAML at startup
- Error responses follow consistent format across all error types
