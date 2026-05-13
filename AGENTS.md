# AGENTS.md

Codex는 이 저장소에서 Spring Boot 해커톤 백엔드 템플릿을 다룬다. 목표는 웹/모바일 팀이 바로 붙을 수 있고, 코드 완성도 평가에서도 설명 가능한 구조를 유지하는 것이다.

## Project Layout

- `src/main/java/org/sopt/sopkathon/global`: 공통 응답, 에러, 보안, 설정, 웹 필터
- `src/main/java/org/sopt/sopkathon/auth`: 회원가입, 로그인, JWT refresh token
- `src/main/java/org/sopt/sopkathon/member`: 회원 도메인과 repository
- `src/main/java/org/sopt/sopkathon/example`: API 계약 확인용 샘플 API
- `src/main/resources/db/migration`: Flyway SQL migration
- `docs`: 팀 협업, API, 인프라, 아키텍처 문서
- `scripts`: 반복 검증 스크립트

## Commands

- Run tests: `./gradlew test`
- Full verification: `./gradlew clean test`
- Team verification script: `./scripts/verify.sh`
- Build jar: `./gradlew bootJar`
- Local MySQL: `docker compose -f docker-compose.local.yml up -d mysql`
- Local app with Docker Compose profile: `docker compose -f docker-compose.local.yml --profile app up --build`

## Engineering Rules

- Keep the project single-module until the HackaThon topic clearly demands otherwise.
- Use package-by-feature for domain code and keep cross-cutting code under `global`.
- Controllers return `ApiResponse<T>` or `ResponseEntity<ApiResponse<T>>`.
- Business failures must use `BusinessException` and `ErrorCode`.
- Every new public API should have a controller test for response shape.
- Service behavior with branching logic should have unit tests.
- DB schema changes must be Flyway migrations. Do not rely on Hibernate DDL generation.
- Do not introduce Redis, QueryDSL, Kafka, Elasticsearch, Kubernetes, Terraform, or complex Gradle multi-module unless the user explicitly expands scope.

## API Rules

- API prefix is `/api/v1`.
- JSON field names use camelCase.
- Time values use ISO-8601 and UTC unless a feature needs a user timezone.
- Swagger UI is `/swagger-ui.html`.
- Health check is `/actuator/health`.
- Access tokens are sent as `Authorization: Bearer <token>`.

## Review Rules

- Before claiming completion, run the smallest relevant test first, then run `./gradlew clean test` for larger changes.
- For review tasks, use `docs/code-review.md` as the checklist.
- If API shape changes, update `docs/api-conventions.md` and `docs/client-collaboration.md`.
- If deployment behavior changes, update `docs/infra.md` and `docs/runbook/deploy.md`.

## Done Means

- Code compiles.
- Relevant tests pass.
- API response/error shape remains consistent.
- Swagger and docs are not stale for changed endpoints.
- Any manual verification gap is stated plainly.
