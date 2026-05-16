# 인프라 구조

이 문서는 이 템플릿이 제공하는 실행/배포 설정을 빠르게 파악하기 위한 개요다. 실제 배포 명령 순서는 [배포 준비 가이드](../runbooks/deploy.md), 프로필/환경변수 기준은 [프로필과 환경변수 가이드](profile-env-guide.md), DB 초기화와 인증 토글 기준은 [MVP 개발 가이드](../guides/mvp-development-guide.md)를 따른다.

## 제공하는 설정

| 설정 | 파일 | 언제 쓰나 |
| --- | --- | --- |
| 로컬 MySQL | `docker-compose.local.yml` | 개발자가 각자 노트북에서 DB를 띄울 때 |
| 로컬 앱 실행 | `application-local.yml` | IntelliJ/Gradle로 빠르게 수정-실행할 때 |
| 로컬 앱 컨테이너 실행 | `docker-compose.local.yml --profile app` | Docker 환경에서 앱까지 같이 띄워보고 싶을 때 |
| 원격 앱 컨테이너 | `Dockerfile`, `docker-compose.prod.yml` | EC2에서 Spring Boot 컨테이너를 실행할 때 |
| 원격 DB | RDS MySQL | 팀 공유 테스트/시연 데이터를 저장할 때 |
| 배포 자동화 | `.github/workflows/deploy.yml` | GitHub Actions로 이미지 빌드/푸시와 EC2 반영을 자동화할 때 |
| DB 초기화 자동화 | `.github/workflows/reset-db.yml` | 사람이 수동 실행해서 RDS를 최신 스키마와 seed 데이터로 다시 만들 때 |
| 환경변수 예시 | `.env.example`, `.env.prod.example` | 로컬 또는 수동 EC2 배포에서 실제 `.env`를 만들 때 |

## 로컬 개발 구조

기본 로컬 흐름은 MySQL만 Docker로 띄우고, 앱은 IntelliJ 또는 Gradle로 실행하는 방식이다.

```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew bootRun
```

이 방식이 가장 빠른 수정-실행 루프를 만든다. 앱까지 Docker Compose로 띄우고 싶으면 아래처럼 실행한다.

```bash
./gradlew bootJar
docker compose -f docker-compose.local.yml --profile app up -d --build
```

로컬 확인 URL:

- API 기본 URL: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 헬스 체크: `http://localhost:8080/actuator/health`

## 로컬 MySQL 설정

| 항목 | 값 |
| --- | --- |
| 이미지 | `mysql:8.4` |
| 데이터베이스 | `sopkathon` |
| 사용자 / 비밀번호 | `sopt` / `sopt` |
| 호스트 포트 | `13306` |
| 컨테이너 포트 | `3306` |
| 시간대 | UTC |
| 문자셋 / 정렬 규칙 | `utf8mb4` / `utf8mb4_unicode_ci` |
| 볼륨 | `mysql-local-data` |

호스트 포트는 기본 `13306`이다. 개발자 PC에 이미 MySQL이 있거나 다른 프로젝트가 `3306`, `3307`을 쓰는 상황을 피하기 위한 값이다. Compose 내부 서비스끼리는 그대로 `3306`을 사용한다.

로컬 DB 데이터를 완전히 초기화하려면 볼륨까지 지운다.

```bash
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up -d mysql
```

## 원격 배포 구조

해커톤용 권장 구조는 아래처럼 단순하게 유지한다.

```text
Web / Android / iOS
-> EC2 Docker Compose
-> Spring Boot 앱 컨테이너
-> RDS MySQL 8.4
```

선택적으로 파일 업로드가 필요해지면 S3 사전 서명 URL(presigned URL) 방식을 추가한다.

| 구성 | 역할 |
| --- | --- |
| EC2 | Spring Boot 앱 컨테이너 실행 |
| RDS MySQL | 애플리케이션 데이터 저장 |
| Docker Hub | GitHub Actions가 만든 앱 이미지 저장 |
| GitHub Actions | JAR 빌드, Docker 이미지 빌드/푸시, EC2 배포 |
| S3 | 파일 업로드가 필요할 때만 추가 |

공유 테스트 서버나 시연 서버에서는 MySQL을 EC2 안에 같이 띄우지 않는 것을 기본으로 한다. RDS를 쓰면 DB 백업, 보안 그룹 관리, EC2 재배포와 데이터 분리가 더 단순해진다.

## 배포 방식

기본 배포는 EC2에서 빌드하지 않는다. GitHub Actions가 JAR와 Docker 이미지를 만들고, EC2는 이미지를 받아 실행만 한다.

```text
GitHub Actions
-> ./gradlew bootJar -x test
-> docker build
-> docker push

EC2
-> docker compose pull
-> docker compose up -d
```

작은 EC2에서 Gradle 빌드와 Docker build를 직접 수행하면 느리거나 메모리가 부족할 수 있다. 그래서 CI/CD가 준비되면 이미지 빌드는 GitHub Actions에 맡긴다.

PR의 `CI` 워크플로는 `bootJar -x test`로 빌드 가능 여부만 빠르게 확인한다. `main` 푸시의 `CD` 워크플로는 Docker 이미지를 푸시하고 EC2에 반영한다. CD는 DB를 초기화하지 않는다. DB를 다시 만들 때는 `Reset DB` 워크플로를 사람이 수동 실행한다. 테스트 코드는 배포 통과 기준이 아니므로, 필요한 경우 로컬에서 `./gradlew test` 또는 `./scripts/verify.sh`로 별도 확인한다.

## 환경변수 흐름

환경변수는 실행 위치에 따라 주입 방법만 달라진다.

| 위치 | 사용하는 파일/설정 | 설명 |
| --- | --- | --- |
| 로컬 개발 | `.env.example` -> `.env` | Docker Compose가 로컬 MySQL과, `--profile app` 사용 시 앱 컨테이너 값으로 읽는다. |
| 수동 EC2 배포 | `.env.prod.example` -> EC2 `.env` | EC2에서 직접 `docker compose --env-file .env`로 실행한다. |
| GitHub Actions 배포 | Variables / Secrets | 워크플로가 EC2의 `.env`를 만들어 업로드한다. 입력 기준은 GitHub Actions 템플릿이다. |
| GitHub Actions DB 초기화 | EC2의 기존 `.env` | Reset DB 워크플로가 임시 reset env를 만들고, 끝나면 `update/never`로 복구한다. |

실제 `.env`에는 DB 비밀번호, JWT 서명 키, RDS 주소가 들어갈 수 있으므로 Git에 올리지 않는다. 어떤 값을 Variables로 둘지, 어떤 값을 Secrets로 둘지는 [프로필과 환경변수 가이드](profile-env-guide.md)와 [GitHub Actions 설정값](github-actions-settings.md)을 따른다.

## 팀원이 먼저 알아야 할 것

1. 로컬은 `docker-compose.local.yml`로 MySQL만 띄우고 `./gradlew bootRun`으로 앱을 실행한다.
2. 원격은 `docker-compose.prod.yml`로 Spring Boot 앱 컨테이너만 실행하고 DB는 RDS를 쓴다.
3. 일반 CD 배포는 DB를 보존한다. DB 초기화는 `Reset DB` 워크플로를 수동 실행한다.
4. MVP 초반 인증 전체 허용은 `APP_SECURITY_PERMIT_ALL=true`, 인증 연동 시점부터 `false`로 바꾼다.
5. Swagger가 호출할 서버는 `APP_SWAGGER_SERVER_URL`, 웹 CORS는 `APP_CORS_ALLOWED_ORIGINS`로 맞춘다.

설정값을 언제 바꿀지는 [MVP 개발 가이드](../guides/mvp-development-guide.md), 배포 순서는 [배포 준비 가이드](../runbooks/deploy.md)를 본다.
