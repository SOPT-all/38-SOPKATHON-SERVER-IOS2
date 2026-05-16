# 배포 준비 가이드

이 문서는 EC2/RDS에 이 Spring Boot 템플릿을 올릴 때 필요한 준비물과 실행 순서를 정리한다. 구조 개요는 [인프라 구조](../operations/infra.md), 프로필/환경변수 기준은 [프로필과 환경변수 가이드](../operations/profile-env-guide.md), GitHub Actions 입력값 전체 목록은 [GitHub Actions 설정값](../operations/github-actions-settings.md)을 본다.

## 목표 구조

해커톤 기본 배포는 아래 흐름을 목표로 한다.

```text
main 푸시
-> GitHub Actions가 bootJar 생성
-> Docker 이미지 빌드/푸시
-> EC2가 이미지를 pull
-> docker compose up -d
-> /actuator/health 확인
```

EC2에서는 빌드하지 않는다. 작은 EC2에서 Gradle/Docker build를 돌리면 느리거나 메모리가 부족할 수 있으므로, GitHub Actions가 만든 Docker 이미지를 받아 실행만 한다.

## 준비물 한눈에 보기

| 준비물 | 왜 필요한가 | 관련 파일/문서 |
| --- | --- | --- |
| Docker Hub 계정/토큰 | 앱 이미지를 저장하고 EC2가 받기 위해 필요 | `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN` |
| EC2 | Spring Boot 앱 컨테이너 실행 | `docker-compose.prod.yml` |
| RDS MySQL | 팀 공유 데이터 저장 | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| EC2 SSH 키 | GitHub Actions가 EC2에 접속하기 위해 필요 | `EC2_SSH_KEY` |
| JWT 서명 키 | 액세스 토큰 서명 | `APP_JWT_SECRET` |
| 웹 Origin | 브라우저 CORS 허용 | `APP_CORS_ALLOWED_ORIGINS` |
| Swagger 서버 URL | Swagger Try it out 대상 서버 | `APP_SWAGGER_SERVER_URL` |

AWS나 Docker Hub가 아직 없어도 워크플로는 먼저 커밋할 수 있다. 필수 값이 비어 있으면 CD는 배포를 건너뛰고 누락된 이름만 로그로 남긴다.

## 로컬에서 먼저 확인

푸시 전에 최소한 JAR가 만들어지는지 확인한다.

```bash
./gradlew bootJar -x test
```

테스트까지 확인하고 싶으면 별도로 실행한다.

```bash
./gradlew test
```

팀 검증 스크립트는 아래 명령이다.

```bash
./scripts/verify.sh
```

## GitHub Actions 설정값 등록

GitHub 위치:

```text
Repository
-> Settings
-> Secrets and variables
-> Actions
```

값을 넣기 전에 템플릿을 확인한다.

- Variables 템플릿: [github-actions-variables.template.env](../operations/github-actions-variables.template.env)
- Secrets 템플릿: [github-actions-secrets.template.env](../operations/github-actions-secrets.template.env)
- 전체 설명: [GitHub Actions 설정값](../operations/github-actions-settings.md)

`.env.prod.example`은 EC2에서 직접 Docker Compose를 실행하는 수동 배포 참고용이다. GitHub Actions 화면에 값을 넣을 때는 위 Variables/Secrets 템플릿을 기준으로 한다.

최소 필수 변수:

| 변수 | 용도 |
| --- | --- |
| `DOCKERHUB_USERNAME` | Docker Hub 계정 이름 |
| `EC2_HOST` | EC2 공개 IP 또는 도메인 |
| `EC2_USERNAME` | EC2 SSH 사용자 |
| `DB_URL` | RDS MySQL JDBC URL |
| `DB_USERNAME` | RDS DB 사용자 |

최소 필수 Secrets:

| Secret | 용도 |
| --- | --- |
| `DOCKERHUB_TOKEN` | Docker Hub 액세스 토큰 |
| `EC2_SSH_KEY` | EC2 접속용 개인 키 전체 내용 |
| `DB_PASSWORD` | RDS DB 비밀번호 |
| `APP_JWT_SECRET` | JWT 서명 키 |

선택값과 기본값은 [GitHub Actions 설정값](../operations/github-actions-settings.md)에만 자세히 정리한다. 같은 표를 여러 문서에 복사하지 않는다.

## DB 초기화와 시드 전략

배포 서버도 초반에는 빠르게 초기화하며 개발할 수 있다. 다만 클라이언트가 만든 데이터가 의미를 갖는 순간부터 보존 모드로 바꿔야 한다.

| 단계 | 설정 |
| --- | --- |
| 앱 시작마다 깨끗한 DB | `DB_DDL_AUTO=create`, `DB_SQL_INIT_MODE=never` |
| 앱 시작마다 초기화 + 시드 | `DB_DDL_AUTO=create`, `DB_SQL_INIT_MODE=always` |
| 데이터 보존 | `DB_DDL_AUTO=update`, `DB_SQL_INIT_MODE=never` |
| 제출 전 스키마 확인 | `DB_DDL_AUTO=validate`, `DB_SQL_INIT_MODE=never` |

`create`는 앱 시작마다 기존 테이블과 행을 지운다. 원격 서버에서는 일반 CD 설정을 `create/always`로 남기지 말고, 의도적인 초기화가 필요할 때 Reset DB 워크플로를 쓴다. 전환 시점의 자세한 기준은 [MVP 개발 가이드](../guides/mvp-development-guide.md)를 따른다.

## EC2 최초 준비

EC2에는 Docker와 Docker Compose 플러그인을 설치한다. 그 다음 앱 디렉터리를 만든다.

```bash
mkdir -p ~/sopkathon
cd ~/sopkathon
```

GitHub Actions 배포는 `docker-compose.prod.yml`과 `.env`를 `~/sopkathon`에 업로드한다. 수동 배포를 할 때만 파일을 직접 둔다.

보안 그룹 기준:

- EC2 SSH 인바운드: GitHub Actions 러너가 접속할 수 있어야 한다. 해커톤에서는 임시로 `0.0.0.0/0`을 열 수 있지만, 가능하면 필요한 범위로 좁힌다.
- EC2 HTTP 인바운드: 클라이언트가 호출할 포트. 기본은 `8080`.
- RDS MySQL 인바운드: EC2 보안 그룹에서 오는 `3306`만 허용한다. RDS를 공개 인터넷에 열지 않는 것을 기본으로 한다.

## 자동 배포

GitHub Actions Variables와 Secrets가 준비된 뒤 `main`에 푸시하면 CD가 자동으로 실행된다. 수동 재배포가 필요하면 GitHub Actions 화면에서 `CD` 워크플로를 선택하고 `Run workflow`를 누른다.

CD는 DB를 초기화하지 않는다. 기본 배포는 `DB_DDL_AUTO=update`, `DB_SQL_INIT_MODE=never`로 기존 데이터를 보존하는 흐름이다.

EC2에서 실제로 실행되는 핵심 명령은 아래와 같다.

```bash
docker compose -f docker-compose.prod.yml --env-file .env pull
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

## DB 수동 초기화

DB를 깨끗하게 다시 만들고 seed를 넣어야 할 때는 `Reset DB` 워크플로를 수동 실행한다.

```text
Repository
-> Actions
-> Reset DB
-> Run workflow
```

입력:

| 입력값 | 값 |
| --- | --- |
| `confirmReset` | `RESET` |
| `seedFiles` | `local-test-account` |

`seedFiles`는 `src/main/resources/db/seed` 아래의 파일 이름만 쓴다. `.sql`은 생략할 수 있고, 여러 파일은 쉼표로 구분한다. 추가 seed를 쓰려면 먼저 같은 이름의 SQL 파일을 저장소에 추가하고 CD 배포를 끝낸다.

Reset DB 흐름:

```text
EC2의 현재 .env 백업
-> DB_DDL_AUTO=create 로 앱 재시작
-> Hibernate가 현재 배포 image 기준으로 스키마 재생성
-> seedFiles가 있으면 선택한 seed SQL 실행
-> DB_DDL_AUTO=update, DB_SQL_INIT_MODE=never 로 앱 재시작
-> /actuator/health 확인
```

새 Entity나 새 seed 파일을 반영하려면 먼저 `main`에 push해서 CD 배포가 끝난 뒤 Reset DB를 실행한다. Reset DB는 새 Docker image를 빌드하거나 pull하지 않는다.

## 수동 배포 백업

GitHub Actions가 느리거나 장애가 있으면 로컬에서 이미지를 직접 푸시한 뒤 EC2에서 pull/up할 수 있다.

이때만 EC2의 `~/sopkathon/.env`를 직접 만들며, 예시는 `.env.prod.example`을 참고한다.

로컬:

```bash
./gradlew bootJar -x test
docker build -t <dockerhub-username>/sopkathon-server:manual .
docker push <dockerhub-username>/sopkathon-server:manual
```

EC2:

```bash
cd ~/sopkathon
APP_IMAGE=<dockerhub-username>/sopkathon-server:manual docker compose -f docker-compose.prod.yml --env-file .env up -d
curl -fsS http://localhost:8080/actuator/health
```

## 배포 확인

컨테이너 상태:

```bash
docker compose -f docker-compose.prod.yml --env-file .env ps
```

로그:

```bash
docker compose -f docker-compose.prod.yml --env-file .env logs -f app
```

헬스 체크:

```bash
curl -fsS http://localhost:8080/actuator/health
```

예상 응답:

```json
{"status":"UP"}
```

인증 흐름까지 확인하려면 계정이 준비된 뒤 스모크 테스트를 실행한다.

```bash
BASE_URL=http://<배포 API 주소>:8080 ./scripts/auth-smoke-test.sh
```

공유 서버나 운영 환경은 보통 시드를 끄므로, 스모크 테스트 전에 회원가입 API로 계정을 만들거나 별도 시드를 준비한다.

## 롤백

CD는 `sha-<commit>` 태그와 `latest` 태그를 같이 푸시한다. 문제가 생기면 Docker Hub에서 직전 커밋 태그를 확인한 뒤 `APP_IMAGE`만 바꿔 재실행한다.

```bash
APP_IMAGE=<dockerhub-username>/sopkathon-server:sha-직전커밋 docker compose -f docker-compose.prod.yml --env-file .env up -d
curl -fsS http://localhost:8080/actuator/health
```

## 자주 막히는 지점

| 증상 | 확인 |
| --- | --- |
| CD가 `배포 대기`로 끝남 | 필수 GitHub Actions Variables/Secrets가 비어 있다. Actions 로그의 누락 목록을 본다. |
| Docker 이미지 빌드 실패 | 로컬에서 `./gradlew bootJar -x test`가 되는지 확인한다. `.dockerignore`가 `build/libs/*.jar`를 포함하는지도 본다. |
| EC2에서 `exec format error` | EC2 CPU와 `DOCKER_PLATFORM`이 맞는지 확인한다. t4g는 `linux/arm64`, t3/t2는 `linux/amd64`다. |
| 앱이 RDS에 연결 실패 | EC2 -> RDS 보안 그룹, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, RDS 데이터베이스 생성 여부를 본다. |
| DB 데이터가 사라짐 | `DB_DDL_AUTO=create`인지 확인한다. 데이터 보존 시점에는 `update`로 바꾼다. |
| Reset DB가 seed 파일을 못 찾음 | seed 파일이 `src/main/resources/db/seed` 아래에 있고, 해당 변경이 CD로 먼저 배포됐는지 확인한다. |
| 클라이언트 호출 실패 | EC2 인바운드 `HOST_PORT`, `APP_CORS_ALLOWED_ORIGINS`, `Authorization` 헤더를 확인한다. |
| Swagger Try it out이 로컬을 호출함 | `APP_SWAGGER_SERVER_URL`이 EC2/도메인 주소인지 확인한다. |

## 제출 전 배포 체크

- `/actuator/health`가 `UP`이다.
- Swagger UI가 배포 서버를 호출한다.
- 웹 배포 Origin이 CORS에 들어 있다.
- 데이터 보존이 필요하면 `DB_DDL_AUTO=update` 또는 `validate` 상태다.
- 클라이언트가 쓸 계정이나 회원가입 흐름이 준비되어 있다.
- 문제가 생겼을 때 직전 Docker 이미지 태그로 롤백할 수 있다.
