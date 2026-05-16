# 해커톤 진행 가이드

해커톤 당일에는 이 파일을 기준으로 필요한 문서만 따라가면 된다. 자세한 규칙과 설정값은 각 상세 문서에 맡기고, 여기서는 진행 순서와 판단 지점만 정리한다.

## 먼저 볼 것

처음에는 아래 문서만 보면 충분하다.

| 언제 | 볼 문서 | 목적 |
| --- | --- | --- |
| 처음 실행 | [../README.md](../README.md) | 로컬 실행, Swagger, 테스트 계정 |
| 기능 개발 | [학생 개발 안내서](guides/student-development-handbook.md) | 새 API를 어떤 순서로 만들지 |
| 설정 파악 | [인프라 구조](operations/infra.md) | 로컬/원격 실행 구조와 제공 파일 확인 |
| 배포 준비 | [배포 준비 가이드](runbooks/deploy.md) | EC2/RDS 배포 준비물과 실행 순서 확인 |

나머지 문서는 막히는 상황이 생겼을 때만 보면 된다.

## 진행 순서

### 1. 로컬 실행

목표: 내 노트북에서 서버와 Swagger가 뜨는지 확인한다.

```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew bootRun
```

확인:

- Swagger: `http://localhost:8080/swagger-ui.html`
- 헬스 체크: `http://localhost:8080/actuator/health`

관련 문서:

- [README](../README.md)
- [학생 개발 안내서](guides/student-development-handbook.md)
- [프로필과 환경변수 가이드](operations/profile-env-guide.md)

### 2. API 개발

목표: 웹/모바일 팀이 바로 붙을 수 있는 API를 만든다.

작업 순서:

1. 화면에서 필요한 메서드, 경로, 요청/응답을 먼저 정한다.
2. 컨트롤러, DTO, 서비스, 엔티티/리포지토리를 만든다.
3. Swagger에서 요청/응답을 확인한다.
4. 공통 응답과 에러 포맷을 유지한다.

관련 문서:

- [백엔드 규칙](standards/backend-conventions.md)
- [API 규칙](standards/api-conventions.md)
- [클라이언트 협업 가이드](collaboration/client-collaboration.md)

### 3. 인증 붙이기

처음에는 API 개발 속도를 위해 인증을 열어둘 수 있다. 로그인 사용자 기준 기능이 필요해지는 순간 인증 설정을 켜고, Swagger Authorize 방식까지 클라이언트와 맞춘다.

관련 문서:

- [인증 개발 가이드](guides/auth-development-guide.md)
- [MVP 개발 가이드](guides/mvp-development-guide.md)

### 4. DB 초기화와 시드

초반에는 로컬 DB를 자주 초기화하는 편이 빠르다. 원격 서버는 일반 CD 배포에서 DB를 보존하고, 테스트 데이터가 꼬였을 때만 `Reset DB` GitHub Actions 워크플로를 수동 실행한다. 제출 전에는 스키마 불일치를 한 번 확인한다.

관련 문서:

- [MVP 개발 가이드](guides/mvp-development-guide.md)
- [프로필과 환경변수 가이드](operations/profile-env-guide.md)

### 5. CI/CD 설정

목표: 배포 방식과 GitHub 입력값을 팀에서 한 번만 맞춘다. CI/CD는 해커톤 핵심 API 개발보다 우선순위가 낮으므로, 먼저 [인프라 구조](operations/infra.md)에서 구조를 이해하고 막히면 [배포 준비 가이드](runbooks/deploy.md)의 수동 배포 기준으로 최소 동작부터 확인한다.

관련 문서:

- [인프라 구조](operations/infra.md)
- [배포 준비 가이드](runbooks/deploy.md)
- [GitHub Actions 설정값](operations/github-actions-settings.md)
- [Variables 템플릿](operations/github-actions-variables.template.env)
- [Secrets 템플릿](operations/github-actions-secrets.template.env)

### 6. 발표 전 체크

최소 확인:

1. Swagger에서 클라이언트가 쓸 API가 보인다.
2. 배포 서버 `/actuator/health`가 `UP`이다.
3. 데이터 보존과 스키마 검증 기준을 팀 설정에 맞췄다.
4. 웹 배포 Origin이 `APP_CORS_ALLOWED_ORIGINS`에 들어 있다.
5. Swagger 서버 URL이 실제 EC2/도메인을 가리킨다.

관련 문서:

- [코드 리뷰 체크리스트](standards/code-review.md)
- [배포 준비 가이드](runbooks/deploy.md)
