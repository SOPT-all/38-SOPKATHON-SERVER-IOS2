# SOPKATHON 

## 문서 먼저 보기

개발/협업/배포 문서의 첫 페이지는 [문서 포털](docs/README.md)이다.

## 기술 스택

- Java 21
- Spring Boot 4.0.6
- Gradle 9.4.1
- MySQL 8.4 LTS
- Spring Web MVC, Security, Data JPA, Validation, Actuator
- springdoc-openapi Swagger UI
- Testcontainers
- Docker Compose
- 로컬 환경변수 예시: `.env.example`
- 수동 운영 환경변수 예시: `.env.prod.example`

## 빠른 시작

로컬에서는 보통 MySQL만 Docker로 띄우고 애플리케이션은 Gradle 또는 IDE로 실행한다.

```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew bootRun
```

자주 쓰는 검증:

```bash
./scripts/test-fast.sh
./scripts/verify.sh
```

앱이 실행 중일 때 JWT 흐름만 빠르게 확인하려면:

```bash
./scripts/auth-smoke-test.sh
```

주요 URL:

- API 기본 주소: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- 헬스 체크: `http://localhost:8080/actuator/health`

## MVP 개발용 설정

로컬 프로필은 해커톤 초반 속도에 맞춰 인증을 열고, 스키마/시드를 쉽게 초기화할 수 있게 되어 있다. 로그인 사용자 기준 API가 필요하거나 데이터를 보존해야 하는 순간부터 설정을 조정한다.

자세한 기준은 `docs/guides/mvp-development-guide.md`와 `docs/operations/profile-env-guide.md`를 본다.

## 테스트 계정

- 이메일: `test@sopt.org`
- 비밀번호: `password123!`

## 문서

해커톤 중에는 [해커톤 진행 가이드](docs/HACKATHON_GUIDE.md)부터 보면 된다. 문서 전체 입구는 [문서 포털](docs/README.md)이다.

자주 보는 문서:

- 해커톤 진행 순서: [docs/HACKATHON_GUIDE.md](docs/HACKATHON_GUIDE.md)
- 설정 한눈에 보기: [docs/operations/infra.md](docs/operations/infra.md)
- 처음 개발하는 백엔드 팀원: [docs/guides/student-development-handbook.md](docs/guides/student-development-handbook.md)
- 웹/모바일 연동: [docs/collaboration/client-collaboration.md](docs/collaboration/client-collaboration.md)
- 배포 준비/실행: [docs/runbooks/deploy.md](docs/runbooks/deploy.md)
