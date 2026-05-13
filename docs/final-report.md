# Final Report

이 문서는 1박2일 대학생 해커톤용 Spring Boot 백엔드 템플릿을 어떤 기준으로 만들었고, 어떤 구조가 나왔는지 설명한다.

## 목표

백엔드 담당자가 주제 공개 직후 바로 기능 개발에 들어갈 수 있도록 기본 인프라를 미리 깔았다. 동시에 웹, Android, iOS 팀이 Swagger와 문서를 보고 빠르게 연동할 수 있게 했다.

핵심 방향은 얇은 starter가 아니라 full-ready starter다. 다만 해커톤에 과한 Redis, QueryDSL, Kafka, Elasticsearch, Kubernetes, Terraform, 복잡한 Gradle multi-module은 초기 범위에서 제외했다.

## 최종 스택

| 영역 | 선택 |
| --- | --- |
| Language | Java 21 LTS |
| Framework | Spring Boot 4.0.6 |
| Build | Gradle 9.4.1 |
| DB | MySQL 8.4 LTS |
| Migration | Flyway |
| Auth | 자체 로그인 + JWT access/refresh token |
| API Docs | springdoc-openapi Swagger UI |
| Test | JUnit, MockMvc, Mockito, Testcontainers |
| Infra | Docker Compose, AWS EC2 + RDS MySQL 기준 |

## 만들어진 구조

```text
org.sopt.sopkathon
├── global
│   ├── config
│   ├── error
│   ├── response
│   ├── security
│   └── web
├── auth
│   ├── api
│   ├── application
│   ├── domain
│   ├── dto
│   └── repository
├── member
│   ├── domain
│   └── repository
└── example
    ├── api
    └── dto
```

단일 모듈로 시작하고 package-by-feature를 사용했다. 주제가 정해지기 전에는 multi-module보다 이 구조가 빠르고 안전하다.

## 주요 구현

- 공통 성공 응답: `ApiResponse<T>`
- 페이지 응답: `PageResponse<T>`
- 공통 에러 응답: `GlobalErrorResponse`
- 에러 코드 체계: HTTP status, 내부 code, message, log level
- trace id: `X-Request-Id`
- JWT 인증 필터와 401/403 공통 응답
- 회원가입, 로그인, 토큰 재발급, 로그아웃
- refresh token DB 저장과 rotation
- Flyway schema migration과 테스트 계정 seed
- Swagger JWT bearer 인증 설정
- Testcontainers MySQL 통합 테스트
- Docker Compose local/prod 구성
- GitHub Actions CI와 PR template
- Codex용 `AGENTS.md`와 workflow 문서

## 실행 방법

로컬 MySQL:

```bash
docker compose -f docker-compose.local.yml up -d mysql
```

앱 실행:

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
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

로컬 MySQL host port는 `13306`이다. 기존 로컬 MySQL이나 다른 프로젝트가 `3306`, `3307`을 쓰는 경우가 많아서 충돌을 피했다.

## 테스트 계정

| Field | Value |
| --- | --- |
| Email | `test@sopt.org` |
| Password | `password123!` |
| Role | `ROLE_USER` |

## 산출물 매핑

| 요구사항 | 산출물 |
| --- | --- |
| 백엔드 컨벤션 | `docs/backend-conventions.md` |
| 웹/모바일 협업 컨벤션 | `docs/api-conventions.md`, `docs/client-collaboration.md` |
| Codex 활용 계획 | `AGENTS.md`, `docs/codex-workflows.md` |
| 프로젝트 기본 세팅 | `build.gradle`, profile YAML, `.env.example` |
| 인프라 세팅 | `Dockerfile`, `docker-compose.local.yml`, `docker-compose.prod.yml`, `docs/infra.md` |
| 아키텍처 원칙 | `docs/architecture.md` |
| 로그인/JWT/global 코드 | `auth`, `member`, `global` packages |
| 클라이언트 테스트 서버 설계 | `docs/runbook/deploy.md`, `docs/client-collaboration.md` |
| GitHub 협업 | `.github/workflows/ci.yml`, `.github/pull_request_template.md`, `docs/github-conventions.md` |

## 검증 결과

2026-05-13 기준으로 확인했다.

| 검증 | 결과 |
| --- | --- |
| `./scripts/verify.sh` | 성공 |
| `./gradlew bootJar` | 성공 |
| local Docker Compose config | 성공 |
| prod Docker Compose config | 성공 |
| local MySQL container | `healthy`, `13306 -> 3306` |
| 앱 DB 연결 | 성공 |
| Flyway migration | V1, V2 적용 성공 |
| `/actuator/health` | `UP` |
| `/v3/api-docs` | 응답 성공 |
| 테스트 계정 로그인 | 성공 |

## 해커톤 당일 커스터마이징 순서

1. 주제에 맞는 핵심 feature package를 만든다.
2. API 계약을 Swagger 기준으로 먼저 잡는다.
3. request/response DTO와 validation을 작성한다.
4. service 테스트를 먼저 만들고 구현한다.
5. controller 테스트로 응답 모양을 고정한다.
6. 필요한 에러 코드를 `ErrorCode`에 추가한다.
7. Flyway migration으로 테이블을 만든다.
8. 웹/모바일 팀에 Swagger URL과 변경사항을 공유한다.

## 후순위로 남긴 것

- OAuth Kakao/Naver/Google 실제 연동
- S3 presigned URL 실제 구현
- QueryDSL
- Redis refresh token 저장소
- 관리자 기능 고도화
- Spring Modulith 경계 검증
- Codex custom skill/plugin 제작
- GitHub branch protection 실제 저장소 설정

이 항목들은 주제가 정해지고 실제 필요가 보일 때 추가하는 편이 낫다.
