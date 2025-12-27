# Tasks: Spring Cloud Gateway DDD Project Structure

**Input**: Design documents from `/specs/001-gateway-ddd-structure/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Tests are NOT explicitly requested in the specification. Test tasks are omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

**Version Constraint**: Spring Boot 3.4.1, Spring Cloud 2025.0.0 (matching op-stack-service)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4)

## Path Conventions

- **Multi-module Maven**: Each module has its own `pom.xml` and `src/main/java/` structure
- Base package: `com.catface996.gateway`

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Create the DDD multi-module Maven project structure matching op-stack-service

- [x] T001 Create parent pom.xml with dependency management (Spring Boot 3.4.1, Spring Cloud 2025.0.0) in pom.xml
- [x] T002 [P] Create common module structure with pom.xml in common/pom.xml
- [x] T003 [P] Create domain module structure with pom.xml in domain/pom.xml
- [x] T004 [P] Create application module structure with pom.xml in application/pom.xml
- [x] T005 [P] Create infrastructure module structure with pom.xml in infrastructure/pom.xml
- [x] T006 [P] Create interface module structure with pom.xml in interface/pom.xml
- [x] T007 [P] Create bootstrap module structure with pom.xml in bootstrap/pom.xml
- [x] T008 Create .gitignore for Maven/Java project in .gitignore

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T009 [P] Create ErrorCode enum with gateway error codes in common/src/main/java/com/catface996/gateway/common/enums/ErrorCode.java
- [x] T010 [P] Create ErrorResponse DTO for error responses in common/src/main/java/com/catface996/gateway/common/dto/ErrorResponse.java
- [x] T011 [P] Create ApiResponse generic wrapper DTO in common/src/main/java/com/catface996/gateway/common/dto/ApiResponse.java
- [x] T012 [P] Create GatewayException base exception class in common/src/main/java/com/catface996/gateway/common/exception/GatewayException.java
- [x] T013 [P] Create AuthenticationException for auth failures in common/src/main/java/com/catface996/gateway/common/exception/AuthenticationException.java
- [x] T014 [P] Create ServiceUnavailableException for backend failures in common/src/main/java/com/catface996/gateway/common/exception/ServiceUnavailableException.java
- [x] T015 Create GatewayApplication main class in bootstrap/src/main/java/com/catface996/gateway/bootstrap/GatewayApplication.java
- [x] T016 Create base application.yml with server port and Spring profiles in bootstrap/src/main/resources/application.yml
- [x] T017 [P] Create application-local.yml with localhost backend URLs in bootstrap/src/main/resources/application-local.yml
- [x] T018 [P] Create application-prod.yml with environment variable URLs in bootstrap/src/main/resources/application-prod.yml
- [x] T019 Create GlobalExceptionHandler for error responses in infrastructure/src/main/java/com/catface996/gateway/infrastructure/config/GlobalExceptionHandler.java

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 4 - DDD Module Structure Setup (Priority: P1) 🎯 MVP

**Goal**: Establish clean, maintainable codebase with DDD layered architecture

**Independent Test**: Run `mvn clean compile` - all modules should compile successfully

### Implementation for User Story 4

- [x] T020 [P] [US4] Create package-info.java for common module in common/src/main/java/com/catface996/gateway/common/package-info.java
- [x] T021 [P] [US4] Create package-info.java for domain module in domain/src/main/java/com/catface996/gateway/domain/package-info.java
- [x] T022 [P] [US4] Create package-info.java for application module in application/src/main/java/com/catface996/gateway/application/package-info.java
- [x] T023 [P] [US4] Create package-info.java for infrastructure module in infrastructure/src/main/java/com/catface996/gateway/infrastructure/package-info.java
- [x] T024 [P] [US4] Create package-info.java for interface module in interface/src/main/java/com/catface996/gateway/interfaces/package-info.java
- [x] T025 [P] [US4] Create package-info.java for bootstrap module in bootstrap/src/main/java/com/catface996/gateway/bootstrap/package-info.java
- [x] T026 [US4] Verify all modules compile with `mvn clean compile` from project root

**Checkpoint**: DDD structure established and compiles successfully

---

## Phase 4: User Story 1 - Gateway Proxies Backend Services (Priority: P1) 🎯 MVP

**Goal**: Route HTTP requests to op-stack-service, op-stack-executor, op-stack-tools, op-stack-auth based on URL path

**Independent Test**: Send requests to `/api/service/**`, `/api/executor/**`, `/api/tools/**`, `/api/auth/**` and verify routing

### Implementation for User Story 1

- [x] T027 [P] [US1] Create BackendService value object model in domain/src/main/java/com/catface996/gateway/domain/route/model/BackendService.java
- [x] T028 [P] [US1] Create RouteConstants with path prefixes in common/src/main/java/com/catface996/gateway/common/constants/RouteConstants.java
- [x] T029 [US1] Add Spring Cloud Gateway dependency to bootstrap pom.xml in bootstrap/pom.xml
- [x] T030 [US1] Configure route definitions for all 4 backend services in bootstrap/src/main/resources/application.yml
- [x] T031 [US1] Create AccessLogFilter for request logging in infrastructure/src/main/java/com/catface996/gateway/infrastructure/filter/AccessLogFilter.java
- [x] T032 [US1] Register AccessLogFilter as global filter in bootstrap/src/main/java/com/catface996/gateway/bootstrap/config/FilterConfig.java

**Checkpoint**: Gateway routes requests to all 4 backend services

---

## Phase 5: User Story 2 - JWT Authentication for Protected Routes (Priority: P2)

**Goal**: Validate JWT tokens via op-stack-auth service and inject operatorId into request body

**Independent Test**: Send requests with valid/invalid JWT tokens and verify authentication behavior

### Implementation for User Story 2

- [x] T033 [P] [US2] Create TokenInfo domain model in domain/src/main/java/com/catface996/gateway/domain/auth/model/TokenInfo.java
- [x] T034 [P] [US2] Create AuthenticationResult domain model in domain/src/main/java/com/catface996/gateway/domain/auth/model/AuthenticationResult.java
- [x] T035 [P] [US2] Create AuthenticationService interface in domain/src/main/java/com/catface996/gateway/domain/auth/service/AuthenticationService.java
- [x] T036 [P] [US2] Create AuthValidateRequest DTO in application/src/main/java/com/catface996/gateway/application/auth/dto/AuthValidateRequest.java
- [x] T037 [P] [US2] Create AuthValidateResponse DTO in application/src/main/java/com/catface996/gateway/application/auth/dto/AuthValidateResponse.java
- [x] T038 [US2] Create WebClientConfig for HTTP client in infrastructure/src/main/java/com/catface996/gateway/infrastructure/config/WebClientConfig.java
- [x] T039 [US2] Create AuthServiceClient to call op-stack-auth in infrastructure/src/main/java/com/catface996/gateway/infrastructure/auth/client/AuthServiceClient.java
- [x] T040 [US2] Create AuthenticationGatewayFilter for JWT validation in infrastructure/src/main/java/com/catface996/gateway/infrastructure/filter/AuthenticationGatewayFilter.java
- [x] T041 [US2] Create OperatorIdInjectionFilter to modify request body in infrastructure/src/main/java/com/catface996/gateway/infrastructure/filter/OperatorIdInjectionFilter.java
- [x] T042 [US2] Register Authentication and OperatorIdInjection filters in bootstrap/src/main/java/com/catface996/gateway/bootstrap/config/FilterConfig.java
- [x] T043 [US2] Add auth service URL configuration to application.yml in bootstrap/src/main/resources/application.yml

**Checkpoint**: Protected routes require valid JWT, operatorId injected into body

---

## Phase 6: User Story 3 - Public Routes Without Authentication (Priority: P2)

**Goal**: Allow unauthenticated access to registration and login endpoints

**Independent Test**: Send requests to `/api/auth/register` and `/api/auth/login` without token and verify forwarding

### Implementation for User Story 3

- [x] T044 [P] [US3] Create PublicRouteConstants for public paths in common/src/main/java/com/catface996/gateway/common/constants/PublicRouteConstants.java
- [x] T045 [US3] Update AuthenticationGatewayFilter to skip public routes in infrastructure/src/main/java/com/catface996/gateway/infrastructure/filter/AuthenticationGatewayFilter.java
- [x] T046 [US3] Configure public route definitions with metadata in bootstrap/src/main/resources/application.yml

**Checkpoint**: Public routes bypass authentication, protected routes still require JWT

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T047 [P] Update README.md with project overview and quickstart in README.md
- [x] T048 [P] Add logback-spring.xml for structured logging in bootstrap/src/main/resources/logback-spring.xml
- [x] T049 Verify full application starts with `mvn spring-boot:run -pl bootstrap`
- [x] T050 Run quickstart.md validation - test all documented curl commands

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 4 (Phase 3)**: Depends on Foundational - verifies DDD structure compiles
- **User Story 1 (Phase 4)**: Depends on Foundational - core routing functionality
- **User Story 2 (Phase 5)**: Depends on User Story 1 - adds authentication to routes
- **User Story 3 (Phase 6)**: Depends on User Story 2 - modifies authentication filter
- **Polish (Phase 7)**: Depends on all user stories complete

### User Story Dependencies

```
[Setup] → [Foundational] → [US4: DDD Structure] → [US1: Routing] → [US2: Auth] → [US3: Public Routes] → [Polish]
                                                        ↓
                                                   MVP Complete!
```

### Within Each User Story

- Domain models before service interfaces
- Service interfaces before implementations
- Implementations before configuration
- Configuration before filters
- Filters before registration

### Parallel Opportunities

**Phase 1 (Setup)**: T002-T007 can run in parallel (different module pom.xml files)

**Phase 2 (Foundational)**: T009-T014 can run in parallel (different source files), T017-T018 can run in parallel

**Phase 3 (US4)**: T020-T025 can run in parallel (package-info.java files)

**Phase 4 (US1)**: T027-T028 can run in parallel (domain model and constants)

**Phase 5 (US2)**: T033-T037 can run in parallel (domain models and DTOs)

---

## Parallel Example: Phase 2 Foundation

```bash
# Launch all DTOs and exceptions together:
Task: "Create ErrorCode enum in common/.../enums/ErrorCode.java"
Task: "Create ErrorResponse DTO in common/.../dto/ErrorResponse.java"
Task: "Create ApiResponse DTO in common/.../dto/ApiResponse.java"
Task: "Create GatewayException in common/.../exception/GatewayException.java"
Task: "Create AuthenticationException in common/.../exception/AuthenticationException.java"
Task: "Create ServiceUnavailableException in common/.../exception/ServiceUnavailableException.java"
```

---

## Implementation Strategy

### MVP First (User Stories 4 + 1)

1. Complete Phase 1: Setup (project structure)
2. Complete Phase 2: Foundational (exceptions, DTOs, main class)
3. Complete Phase 3: US4 (verify DDD compiles)
4. Complete Phase 4: US1 (basic routing)
5. **STOP and VALIDATE**: Test routing works independently
6. Deploy/demo if ready - gateway routes to backends without auth

### Full Implementation

1. Complete MVP (above)
2. Add Phase 5: US2 (JWT authentication)
3. Add Phase 6: US3 (public route bypass)
4. Add Phase 7: Polish
5. Each phase adds value without breaking previous functionality

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Spring Boot 3.4.1 and Spring Cloud 2025.0.0 versions match op-stack-service
- Module order in parent pom.xml: common, domain, application, infrastructure, interface, bootstrap
