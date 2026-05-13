# SOPKATHON Backend Template

1박2일 해커톤에서 Spring Boot 백엔드 담당자가 웹/모바일 팀과 빠르게 협업하기 위한 full-ready starter다.

## Stack

- Java 21
- Spring Boot 4.0.6
- Gradle 9.4.1
- MySQL 8.4 LTS
- Spring Web MVC, Security, Data JPA, Flyway, Validation, Actuator
- springdoc-openapi Swagger UI
- Testcontainers
- Docker Compose

## Quick Start

로컬 MySQL 실행:

```bash
docker compose -f docker-compose.local.yml up -d mysql
```

애플리케이션 실행:

```bash
./gradlew bootRun
```

검증:

```bash
./scripts/verify.sh
```

URLs:

- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

## Test Account

- Email: `test@sopt.org`
- Password: `password123!`

## Docs

- Backend conventions: `docs/backend-conventions.md`
- API conventions: `docs/api-conventions.md`
- Client collaboration: `docs/client-collaboration.md`
- Architecture: `docs/architecture.md`
- Infrastructure: `docs/infra.md`
- Deploy runbook: `docs/runbook/deploy.md`
- GitHub conventions: `docs/github-conventions.md`
- Codex workflows: `docs/codex-workflows.md`
- Final report: `docs/final-report.md`
