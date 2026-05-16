# 학생 개발 안내서

Spring Boot 백엔드가 익숙하지 않은 팀원이 가장 먼저 보는 학습용 안내서다. 세부 규칙은 링크된 문서에 맡기고, 여기에는 실행과 새 기능을 추가하는 흐름만 남긴다.

## 프로젝트 구조

새 기능은 `global`이 아니라 기능 패키지 아래에 만든다.

```text
src/main/java/org/sopt/sopkathon
├── global      # 공통 응답, 에러, 보안, 설정
├── auth        # 회원가입, 로그인, 액세스 토큰
├── member      # 회원 엔티티와 리포지토리
└── example     # API 계약 확인용 샘플
```

예를 들어 게시글 기능은 아래처럼 둔다.

```text
post
├── api             # 컨트롤러
├── application     # 서비스
├── domain          # 엔티티, enum
├── dto             # 요청/응답
└── repository      # JpaRepository
```

상세 규칙은 [백엔드 규칙](../standards/backend-conventions.md)을 본다.

## 로컬 실행

로컬 개발은 보통 MySQL만 Docker로 띄우고, 앱은 IntelliJ 또는 Gradle로 실행한다.

```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew bootRun
```

확인할 URL:

| 용도 | URL |
| --- | --- |
| API 기본 주소 | `http://localhost:8080/api/v1` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| 헬스 체크 | `http://localhost:8080/actuator/health` |

테스트 계정:

| 항목 | 값 |
| --- | --- |
| 이메일 | `test@sopt.org` |
| 비밀번호 | `password123!` |

## MVP 설정

해커톤 초반 local 프로필은 빠른 API 개발에 맞춰져 있다. 인증, 스키마 생성, 시드 사용 기준을 바꾸기 전에 팀 전체에 공유하고 같은 `.env` 기준으로 맞춘다.

토글별 기준은 [MVP 개발 가이드](mvp-development-guide.md), 프로필/환경변수 기준은 [프로필과 환경변수 가이드](../operations/profile-env-guide.md)를 본다.

## 새 기능 추가 순서

1. 웹/모바일 팀과 필요한 화면, 메서드, 경로, 요청/응답을 먼저 맞춘다.
2. `feature/api`, `feature/application`, `feature/dto`를 기본으로 만들고, 엔티티/DB 접근이 필요할 때만 `domain`, `repository`를 추가한다.
3. 요청 DTO에는 검증을 붙이고, 응답 DTO는 엔티티를 직접 노출하지 않는다.
4. 컨트롤러는 HTTP 요청/응답만 맡기고 `ApiResponse<T>`로 감싼다.
5. 서비스에서 트랜잭션과 비즈니스 흐름을 처리한다.
6. 실패 케이스는 `BusinessException`과 도메인별 `ErrorCode` enum으로 표현하고, 필요하면 `feature/error` 패키지를 둔다.
7. Swagger와 컨트롤러 테스트로 응답 모양을 확인한다.

컨트롤러 예시는 [백엔드 규칙](../standards/backend-conventions.md), 인증된 사용자 처리 예시는 [인증 개발 가이드](auth-development-guide.md)를 본다.

## 자주 막히는 지점

| 증상 | 먼저 볼 것 |
| --- | --- |
| DB 연결 실패 | Docker Desktop, `docker compose -f docker-compose.local.yml ps`, 포트 `13306` |
| 테이블/컬럼 없음 | [MVP 개발 가이드](mvp-development-guide.md), 엔티티 애너테이션, 패키지 위치 |
| 새 API가 401 반환 | [인증 개발 가이드](auth-development-guide.md), Swagger Authorize, 공개 엔드포인트 설정 |
| `@CurrentUser`가 null | [인증 개발 가이드](auth-development-guide.md), Authorization 헤더 |
| 검증 에러 | 요청 DTO의 `@Valid`와 필드 메시지 |
| CORS 에러 | `APP_CORS_ALLOWED_ORIGINS`에 웹 Origin 추가 |
| Swagger가 다른 서버 호출 | `APP_SWAGGER_SERVER_URL` 확인 |
| 테스트가 느림 | 개발 중에는 `./scripts/test-fast.sh`, 최종 확인은 `./scripts/verify.sh` |

## 발표 전 체크리스트

- API 경로가 `/api/v1` 아래에 있다.
- 성공 응답은 `ApiResponse<T>`다.
- 실패 케이스는 `BusinessException`과 도메인별 `ErrorCode` enum으로 표현한다.
- Swagger에서 요청/응답을 확인했다.
- 웹/모바일 팀에게 인증 필요 여부와 변경 사항을 공유했다.
- 관련 테스트 또는 `./scripts/test-fast.sh`를 실행했다.
- 제출 전 `./scripts/verify.sh`와 `/actuator/health`를 확인했다.
- 제출 전 스키마 불일치 확인 기준은 [MVP 개발 가이드](mvp-development-guide.md)를 따랐다.

## 더 읽을 문서

| 알고 싶은 것 | 문서 |
| --- | --- |
| API 응답, 에러, 페이지네이션 | [API 규칙](../standards/api-conventions.md) |
| 컨트롤러, 서비스, DTO 작성 규칙 | [백엔드 규칙](../standards/backend-conventions.md) |
| 인증을 켠 뒤 개발하는 법 | [인증 개발 가이드](auth-development-guide.md) |
| MVP 중 어떤 설정을 켜고 끄는지 | [MVP 개발 가이드](mvp-development-guide.md) |
| 프로필과 `.env` 기준 | [프로필과 환경변수 가이드](../operations/profile-env-guide.md) |
| 웹/모바일 협업 방식 | [클라이언트 협업 가이드](../collaboration/client-collaboration.md) |
| Docker, EC2, RDS 배포 | [인프라 구조](../operations/infra.md), [배포 준비 가이드](../runbooks/deploy.md) |
