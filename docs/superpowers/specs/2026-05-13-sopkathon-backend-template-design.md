# HackaThon Backend Template Design

## Goal

Build a full-ready Spring Boot backend template for a 1-night 2-day university HackaThon. The template should let a backend developer move fast with web, Android, and iOS teammates while still showing strong code quality during evaluation.

## Current Project State

- Project root: `SOPT-38-HackaThon`
- Build: Gradle Groovy DSL
- Spring Boot: `4.0.6`
- Gradle wrapper: `9.4.1`
- Java toolchain: `21`
- Current dependencies: only `spring-boot-starter` and `spring-boot-starter-test`
- Current source: generated application class and context-load test only

## Research-Based Decisions

### Spring Boot and Java

Keep Spring Boot `4.0.6`. Official Spring Boot requirements say 4.0.6 supports Java 17 through Java 26 and Gradle 8.14+ or 9.x. The generated project already matches those requirements with Gradle 9.4.1.

Keep Java `21` as the default toolchain even though newer JDKs exist. Java 21 is a widely available LTS on local machines, GitHub Actions, and EC2. This reduces setup friction during the HackaThon. The template can document Java 25 as an optional upgrade if the whole team has it installed.

### Database

Use MySQL `8.4` for local Docker Compose and AWS RDS. Amazon RDS currently supports MySQL 8.4, and the major version has standard support until 2029. MySQL is also familiar to many university Spring developers, which lowers team debugging cost.

PostgreSQL remains a valid alternative when the final topic needs stronger JSONB, search, geospatial, or analytics features. For this template, MySQL is the default because the user prefers it and it is enough for most HackaThon CRUD/API-heavy products.

### Scope Control

Start with a strong single-module application. Do not add Redis, QueryDSL, Kafka, Elasticsearch, Kubernetes, Terraform, or complex Gradle multi-module at the beginning. These are useful when requirements demand them, but they increase setup and debugging cost before the topic is known.

Use package boundaries and documentation for modularity. Spring Modulith can be considered later, but is not part of the first implementation slice because Boot 4 compatibility and learning overhead are not necessary for the immediate HackaThon template.

### API and Client Collaboration

OpenAPI/Swagger is the API contract. Client teammates should be able to inspect endpoints, try JWT-authorized calls, see validation errors, and copy example payloads without asking backend repeatedly.

Use `/api/v1` for all public API endpoints.

Use camelCase JSON. Use ISO-8601 timestamps. Store server timestamps in UTC unless a domain explicitly requires local event time.

### Error and Response Model

Use a clear split:

- Success responses use `ApiResponse<T>`.
- Page responses use `PageResponse<T>`.
- Error responses use a ProblemDetail-compatible `ErrorResponse` shape.

Spring Framework 7 supports RFC 9457 Problem Details through `ProblemDetail`, `ErrorResponse`, and `ResponseEntityExceptionHandler`. The template should align with this rather than invent a completely unrelated error shape.

Every error response includes:

- `status`
- `code`
- `message`
- `path`
- `traceId`
- `timestamp`
- optional `errors` for field validation details

`ErrorCode` owns HTTP status, application error code, default message, and log level. This keeps API behavior consistent as the project grows.

### Authentication

Implement local email/password login with access and refresh JWTs. Store refresh tokens in MySQL so logout, token rotation, and forced invalidation are possible.

Leave OAuth extension points for Kakao, Naver, Google, and Apple, but do not implement provider flows in the first slice. OAuth provider setup depends on HackaThon product direction and external console configuration.

Use Spring Security consistently for:

- CORS
- stateless authentication
- authentication entry point
- access denied handler
- public Swagger and health endpoints
- JWT filter or resource-server compatible token verification

### Docker and Infrastructure

Provide Docker Compose for local MySQL immediately. Provide a production-oriented compose file and Dockerfile for EC2 app deployment. Use RDS MySQL in dev/prod instead of running MySQL on EC2.

Use externalized configuration via Spring profiles and environment variables. Secrets must not be committed. Provide `.env.example`.

Expose `/actuator/health` for health checks.

### Testing

Use TDD for behavior-bearing production code. Configuration files and generated boilerplate can be added directly, but common response/error/auth behavior should be covered by tests.

Prefer fast focused tests first:

- unit tests for response factories and error response factories
- web slice tests for sample API and exception mapping
- integration test with Testcontainers for DB-backed auth flows

Spring Boot supports Testcontainers service connections through `spring-boot-testcontainers` and `@ServiceConnection`, which reduces test configuration.

### GitHub Collaboration

Use GitHub Flow. Keep `main` as the deployment branch. Use short-lived branches such as `feat/*`, `fix/*`, `docs/*`, and `chore/*`.

Use Conventional Commits for commit and PR titles.

Use GitHub Actions for Gradle build/test. GitHub's Java with Gradle guide recommends using the Gradle wrapper and `actions/setup-java`.

Document branch protection, required checks, pull request review, and squash merge. GitHub Docs support branch protection rules such as requiring PR reviews, status checks, and linear history.

### Codex and AI Harness

For the first implementation, create practical repository instructions rather than advanced automation.

OpenAI Codex docs recommend using `AGENTS.md` for durable repository guidance: repo layout, run commands, test/lint commands, engineering conventions, constraints, and completion criteria. They also recommend moving repeatable workflows into Skills only after the workflow is stable and repeated.

Therefore:

- Add `AGENTS.md` now.
- Add `docs/codex-workflows.md` now.
- Add `docs/code-review.md` now.
- Do not create custom Skills yet.
- Do not create automations yet.
- Document future AI harness work as phase 2.

## Architecture

Use a single Spring Boot module with package-by-feature plus a small `global` foundation.

```text
org.sopt.sopkathon
  auth
    api
    application
    domain
    dto
    infrastructure
  member
    domain
    repository
  file
    application
    infrastructure
  global
    api
    config
    error
    response
    security
    web
  example
    api
    application
    dto
```

The template should include one sample secured/exception-producing API so global response and error conventions are verifiable before a real HackaThon topic exists.

## Implementation Slices

1. Build/dependency/profile foundation
2. Docker Compose and environment foundation
3. Global response and exception foundation
4. Swagger/OpenAPI and sample API
5. Security/JWT/auth foundation
6. Persistence/Flyway/member/refresh-token foundation
7. Tests and CI
8. GitHub/API/backend/infra/Codex documentation
9. Final verification and student-friendly report

## References

- Spring Boot 4.0.6 system requirements: https://docs.spring.io/spring-boot/system-requirements.html
- Spring Boot Gradle plugin: https://docs.spring.io/spring-boot/gradle-plugin/index.html
- Spring Framework error responses: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
- Spring Security resource server JWT: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
- Spring Boot externalized configuration: https://docs.spring.io/spring-boot/reference/features/external-config.html
- Spring Boot Docker Compose: https://docs.spring.io/spring-boot/how-to/docker-compose.html
- Spring Boot Testcontainers: https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
- Spring Boot managed dependency coordinates: https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html
- Spring Boot 4 MockMvc test package: https://docs.spring.io/spring-boot/api/java/org/springframework/boot/webmvc/test/autoconfigure/AutoConfigureMockMvc.html
- springdoc-openapi v4: https://springdoc.org/v4/
- Amazon RDS for MySQL versions: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/MySQL.Concepts.VersionMgmt.html
- GitHub Flow: https://docs.github.com/en/get-started/using-github/github-flow
- GitHub branch protection: https://docs.github.com/articles/about-required-status-checks
- GitHub Actions Java with Gradle: https://docs.github.com/en/actions/how-tos/writing-workflows/building-and-testing/building-and-testing-java-with-gradle
- Conventional Commits: https://www.conventionalcommits.org/en/v1.0.0/
- Codex AGENTS.md: https://developers.openai.com/codex/guides/agents-md
- Codex best practices: https://developers.openai.com/codex/learn/best-practices
