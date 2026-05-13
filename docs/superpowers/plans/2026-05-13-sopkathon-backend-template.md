# HackaThon Backend Template Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a full-ready Spring Boot HackaThon backend template that supports fast web/mobile collaboration, strong evaluation-visible code quality, local MySQL Docker Compose, AWS EC2/RDS deployment planning, robust global responses/errors, JWT auth extension points, Swagger/OpenAPI, GitHub conventions, and Codex operating guidance.

**Architecture:** Use one Spring Boot module with package-by-feature boundaries and a small `global` foundation. Keep Redis, QueryDSL, Kafka, Elasticsearch, Kubernetes, Terraform, and complex Gradle multi-module out of the initial template. Implement behavior-bearing code with tests first, then add minimal production code.

**Tech Stack:** Spring Boot 4.0.6, Java 21 toolchain, Gradle 9.4.1, MySQL 8.4, Spring Web MVC, Spring Security, Spring Data JPA, Flyway, Validation, Actuator, springdoc-openapi v4, Testcontainers, Lombok, GitHub Actions.

---

## Task 1: Build and Dependency Foundation

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/resources/application.properties`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-prod.yml`
- Create: `.env.example`

- [x] Add required dependencies to `build.gradle`.
- [x] Keep Java toolchain at 21 for reliable local/CI setup.
- [x] Convert `application.properties` into YAML profiles.
- [x] Add environment-variable-backed datasource, JWT, CORS, Swagger, and actuator configuration.
- [x] Add `.env.example` with safe placeholder values only.
- [x] Run `./gradlew test` and resolve dependency/import issues.

## Task 2: Docker Compose Foundation

**Files:**
- Create: `docker-compose.local.yml`
- Create: `docker-compose.prod.yml`
- Create: `Dockerfile`
- Create: `.dockerignore`
- Create: `docs/infra.md`
- Create: `docs/runbook/deploy.md`

- [x] Add local MySQL 8.4 service with persistent volume and health check.
- [x] Add app Dockerfile based on the Gradle build artifact.
- [x] Add production compose for EC2 app container connecting to RDS through environment variables.
- [x] Document local start/stop/reset commands.
- [x] Document EC2/RDS deployment, log check, health check, and rollback commands.

## Task 3: Global Response and Error Foundation

**Files:**
- Create: `src/main/java/org/sopt/sopkathon/global/response/ApiResponse.java`
- Create: `src/main/java/org/sopt/sopkathon/global/response/PageResponse.java`
- Create: `src/main/java/org/sopt/sopkathon/global/error/ErrorCode.java`
- Create: `src/main/java/org/sopt/sopkathon/global/error/ErrorLogLevel.java`
- Create: `src/main/java/org/sopt/sopkathon/global/error/BusinessException.java`
- Create: `src/main/java/org/sopt/sopkathon/global/error/FieldErrorResponse.java`
- Create: `src/main/java/org/sopt/sopkathon/global/error/GlobalErrorResponse.java`
- Create: `src/main/java/org/sopt/sopkathon/global/error/GlobalExceptionHandler.java`
- Create: `src/main/java/org/sopt/sopkathon/global/web/TraceIdFilter.java`
- Test: `src/test/java/org/sopt/sopkathon/global/response/ApiResponseTest.java`
- Test: `src/test/java/org/sopt/sopkathon/global/response/PageResponseTest.java`
- Test: `src/test/java/org/sopt/sopkathon/global/error/GlobalExceptionHandlerTest.java`

- [x] Write failing tests for `ApiResponse` factory methods.
- [x] Implement `ApiResponse`.
- [x] Write failing tests for `PageResponse.from(Page<T>)`.
- [x] Implement `PageResponse`.
- [x] Write failing MVC tests for business, validation, and unknown error shapes.
- [x] Implement `ErrorCode`, `BusinessException`, error DTOs, trace ID filter, and exception handler.
- [x] Verify every error response includes `code`, `message`, `status`, `path`, `traceId`, and `timestamp`.

## Task 4: Swagger/OpenAPI and Example API

**Files:**
- Create: `src/main/java/org/sopt/sopkathon/global/config/OpenApiConfig.java`
- Create: `src/main/java/org/sopt/sopkathon/example/api/ExampleController.java`
- Create: `src/main/java/org/sopt/sopkathon/example/dto/ExampleRequest.java`
- Create: `src/main/java/org/sopt/sopkathon/example/dto/ExampleResponse.java`
- Test: `src/test/java/org/sopt/sopkathon/example/api/ExampleControllerTest.java`

- [x] Write failing web tests for `/api/v1/examples/health`, validation failure, and sample business exception.
- [x] Implement example controller and DTOs.
- [x] Configure OpenAPI metadata and JWT security scheme.
- [x] Verify Swagger UI path and `/v3/api-docs` through configuration and final runtime verification plan.

## Task 5: Security and JWT Foundation

**Files:**
- Create: `src/main/java/org/sopt/sopkathon/global/security/SecurityConfig.java`
- Create: `src/main/java/org/sopt/sopkathon/global/security/JwtAuthenticationFilter.java`
- Create: `src/main/java/org/sopt/sopkathon/global/security/JwtTokenProvider.java`
- Create: `src/main/java/org/sopt/sopkathon/global/security/CustomAuthenticationEntryPoint.java`
- Create: `src/main/java/org/sopt/sopkathon/global/security/CustomAccessDeniedHandler.java`
- Create: `src/main/java/org/sopt/sopkathon/global/security/CurrentUser.java`
- Create: `src/main/java/org/sopt/sopkathon/global/security/CurrentUserArgumentResolver.java`
- Test: `src/test/java/org/sopt/sopkathon/global/security/JwtTokenProviderTest.java`
- Test: `src/test/java/org/sopt/sopkathon/global/security/SecurityConfigTest.java`

- [x] Write failing unit tests for access token creation, parsing, expiration handling, and invalid token handling.
- [x] Implement JWT provider with HMAC secret from configuration.
- [x] Write failing web tests for unauthenticated and forbidden responses.
- [x] Implement security config, JWT filter, authentication entry point, and access denied handler.
- [x] Permit Swagger, actuator health, and auth endpoints.

## Task 6: Member/Auth Persistence Foundation

**Files:**
- Create: `src/main/java/org/sopt/sopkathon/member/domain/Member.java`
- Create: `src/main/java/org/sopt/sopkathon/member/domain/Role.java`
- Create: `src/main/java/org/sopt/sopkathon/member/repository/MemberRepository.java`
- Create: `src/main/java/org/sopt/sopkathon/auth/domain/RefreshToken.java`
- Create: `src/main/java/org/sopt/sopkathon/auth/repository/RefreshTokenRepository.java`
- Create: `src/main/java/org/sopt/sopkathon/auth/api/AuthController.java`
- Create: `src/main/java/org/sopt/sopkathon/auth/application/AuthService.java`
- Create: `src/main/java/org/sopt/sopkathon/auth/dto/SignUpRequest.java`
- Create: `src/main/java/org/sopt/sopkathon/auth/dto/LoginRequest.java`
- Create: `src/main/java/org/sopt/sopkathon/auth/dto/TokenResponse.java`
- Create: `src/main/resources/db/migration/V1__init_auth_member.sql`
- Create: `src/main/resources/db/migration/V2__seed_test_account.sql`
- Test: `src/test/java/org/sopt/sopkathon/auth/application/AuthServiceTest.java`
- Test: `src/test/java/org/sopt/sopkathon/auth/api/AuthControllerTest.java`

- [x] Write failing service tests for sign-up, duplicate email, login success, login wrong password, refresh success, refresh reuse/invalid failure, and logout.
- [x] Implement member and refresh token persistence.
- [x] Write Flyway migrations for `members` and `refresh_tokens`.
- [x] Implement auth API and service.
- [x] Seed a test account through Flyway or a local-only initializer.

## Task 7: Testcontainers and Verification Harness

**Files:**
- Create: `src/test/java/org/sopt/sopkathon/support/IntegrationTestSupport.java`
- Create: `src/test/java/org/sopt/sopkathon/support/WithMockCustomUser.java`
- Create: `scripts/verify.sh`

- [x] Add Spring Boot Testcontainers and MySQL container dependencies.
- [x] Create integration test support with MySQL `@ServiceConnection`.
- [x] Add `scripts/verify.sh` that runs formatting-free baseline checks: `./gradlew clean test`.
- [x] Run the full verification script.

## Task 8: GitHub Collaboration Harness

**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `.github/pull_request_template.md`
- Create: `docs/github-conventions.md`
- Create: `docs/code-review.md`

- [x] Add CI workflow with checkout, setup-java, setup-gradle, and `./gradlew clean test`.
- [x] Add PR template with API change, Swagger check, test result, and client impact checklist.
- [x] Document GitHub Flow, branch naming, Conventional Commits, squash merge, and branch protection.
- [x] Document review checklist for backend changes.

## Task 9: Client/API/Backend Documentation

**Files:**
- Create: `AGENTS.md`
- Create: `docs/backend-conventions.md`
- Create: `docs/api-conventions.md`
- Create: `docs/client-collaboration.md`
- Create: `docs/architecture.md`
- Create: `docs/codex-workflows.md`

- [x] Write repository-level Codex guidance in `AGENTS.md`.
- [x] Document backend package, naming, transaction, DTO, validation, and migration conventions.
- [x] Document API status, error, pagination, auth, CORS, time, and file upload conventions.
- [x] Document client collaboration flow with base URL, Swagger URL, test account, and API change rules.
- [x] Document architecture choices and excluded technologies.
- [x] Document Codex workflows, subagent research conclusions, and future skill/agent plan.

## Task 10: Final Report and Completion Audit

**Files:**
- Create: `docs/final-report.md`

- [x] Run `./gradlew clean test`.
- [x] If Docker is available, run local MySQL compose and application health checks.
- [x] Verify Swagger UI and `/actuator/health`.
- [x] Create a prompt-to-artifact checklist mapping every user requirement to files and verification evidence.
- [x] Write a student-friendly final report explaining what was built, why it was structured this way, how to run it, and what to customize during the HackaThon.

## Self-Review

- The plan covers the user-provided requirements: full-ready template, selected exclusions, Spring/JDK/Gradle, MySQL, Docker Compose, AWS EC2/RDS/S3 optional, dependencies, architecture, global errors/responses, auth, API collaboration, GitHub collaboration, infrastructure docs, Codex guidance, and verification.
- The plan deliberately keeps Redis, QueryDSL, Kafka, Elasticsearch, Kubernetes, Terraform, and complex multi-module out of initial implementation.
- The plan uses TDD for behavior-bearing code and direct edits for configuration/documentation.
- The plan includes final documentation and completion audit evidence.
