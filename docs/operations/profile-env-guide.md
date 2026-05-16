# 프로필과 환경변수 가이드

Spring 프로필과 `.env` 파일을 언제 어떻게 쓰는지 설명한다. 프로필 선택, 실제 환경변수 주입 위치, GitHub Actions Variables/Secrets 분리는 이 문서를 기준으로 한다. `DB_DDL_AUTO`, 시드, 인증 허용값을 언제 바꿀지는 [MVP 개발 가이드](../guides/mvp-development-guide.md)를 함께 본다.

## 프로필 역할

| 프로필 | 언제 쓰나 | 특징 |
| --- | --- | --- |
| `local` | 내 노트북에서 IntelliJ/Gradle로 개발 | Docker MySQL, 테스트 계정 시드, 인증 전체 허용 기본값 |
| `dev` | 테스트 서버를 따로 둘 때 | 원격 실험 서버용. DB 기본은 `create`, seed 기본은 `never` |
| `prod` | 테스트 서버 없이 바로 원격 서버를 운영할 때, 또는 최종 시연 서버 | 원격 배포 기본 프로필. DB 기본은 `update`, seed 기본은 `never` |
| `test` | DB 없는 빠른 단위/컨트롤러 테스트 | DataSource/JPA 자동 설정 제외 |
| `integration-test` | Testcontainers MySQL 통합 테스트 | 실제 MySQL에 가까운 테스트 전용 프로필 |

## integration-test는 언제 쓰나

앱 실행용 프로필은 아니다. 인증, 회원가입, JPA 매핑처럼 DB와 강하게 연결된 기능을 깨끗한 MySQL 컨테이너에서 확인할 때 쓴다.

시간이 없을 때는 빠른 테스트를 먼저 돌린다.

```bash
./scripts/test-fast.sh
```

최종 제출 전이나 인증/DB 관련 코드를 바꾼 뒤에는 전체 검증을 돌린다.

```bash
./scripts/verify.sh
```

## 원격 서버 프로필

해커톤에서 서버를 하나만 운영한다면 `prod`를 원격 서버 프로필로 쓰면 된다.

예시:

```bash
SPRING_PROFILES_ACTIVE=prod
```

`dev`는 서버를 나눌 여유가 있을 때만 의미가 있다.

```text
dev  - 초기화해도 되는 원격 실험 서버
prod - 데이터 보존이 필요한 공유/시연 서버
```

1박2일 해커톤에서는 보통 `prod` 하나로 충분하다.

프로필은 실행 환경 묶음이고, 실제 민감 정보는 환경변수나 `.env`로 주입한다. 원격 서버에 `prod`를 쓰더라도 DB 주소, 비밀번호, JWT 서명 키는 Git에 커밋하지 않는다.

## .env는 언제 쓰나

`.env`는 Docker Compose가 컨테이너를 띄울 때 환경변수를 읽는 파일이다. IntelliJ나 `./gradlew bootRun`은 `.env`를 자동으로 읽지 않는다. 로컬 JVM 실행에서 값을 바꾸려면 IDE Run Configuration이나 shell export로 환경변수를 넣는다.

로컬에서 MySQL만 Compose로 띄우고 앱을 Gradle/IDE로 실행한다면, `.env`는 주로 MySQL 컨테이너 설정에 쓰인다.

```bash
docker compose -f docker-compose.local.yml up -d mysql
./gradlew bootRun
```

앱까지 Compose로 띄울 때는 `.env`의 `SERVER_PORT`, `HOST_PORT`, CORS, JWT 값이 앱 컨테이너에도 전달된다.

```bash
docker compose -f docker-compose.local.yml --profile app up --build
```

EC2에서 직접 Docker Compose로 배포한다면 보통 이런 구조가 된다.

```text
~/sopkathon
├── docker-compose.prod.yml
└── .env
```

실행:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

`.env` 안에는 RDS 주소, DB 비밀번호, JWT 서명 키, CORS Origin 같은 실제 값이 들어간다. 그래서 `.env`는 Git에 올리면 안 된다.

## .env.example과 .env.prod.example 차이

| 파일 | 용도 | 커밋 여부 |
| --- | --- | --- |
| `.env.example` | 로컬 개발용 예시 | 커밋 |
| `.env.prod.example` | 수동 EC2 Docker Compose 배포용 예시 | 커밋 |
| `.env` | 실제 로컬/서버 민감 정보 | 커밋 금지 |
| `.env.prod` | 개인이 따로 만든 실제 운영 값 | 커밋 금지 |

로컬:

```bash
cp .env.example .env
```

EC2에서 수동 배포할 때:

```bash
cp .env.prod.example .env
```

그 다음 실제 값으로 수정한다.

GitHub Actions로 배포한다면 `.env.prod.example`을 보고 값을 옮기지 않는다. GitHub 화면에 넣을 값은 [Variables 템플릿](github-actions-variables.template.env), [Secrets 템플릿](github-actions-secrets.template.env), [GitHub Actions 설정값](github-actions-settings.md)을 기준으로 한다.

## CI/CD와 .env

둘 다 Spring Boot 컨테이너에 환경변수를 전달하는 방법이다.

| 방식 | 언제 쓰나 |
| --- | --- |
| `.env` | EC2에 직접 접속해서 Docker Compose를 실행할 때 |
| GitHub Actions Variables | CI/CD의 일반 배포 설정을 자동으로 넘길 때 |
| GitHub Actions Secrets | CI/CD의 비밀번호/토큰/개인 키를 자동으로 넘길 때 |
| 서버 환경변수 | Docker Compose 없이 서버가 직접 앱을 실행할 때 |

GitHub Actions로 배포한다면 민감하지 않은 값은 Variables, 민감한 값은 Secrets에 저장한다. CD는 이 값으로 서버의 `.env` 파일을 생성하고 `docker compose`에 넘긴다. 이 경우 `.env.prod.example`은 참고용일 뿐, 입력 기준은 GitHub Actions 템플릿이다.

즉 `.env`는 CI/CD와 반대되는 개념이 아니다. CI/CD가 최종적으로 `.env`를 만들 수도 있고, 직접 환경변수를 주입할 수도 있다.

현재 CD 워크플로는 GitHub Actions Variables/Secrets로 EC2의 `~/sopkathon/.env`를 생성한다. 아직 AWS를 만들지 않았거나 필수 설정이 비어 있으면 CD는 배포를 건너뛰고 누락된 이름만 로그로 남긴다.

`Reset DB` 워크플로는 새 `.env`를 업로드하지 않는다. EC2의 기존 `~/sopkathon/.env`를 백업한 뒤 reset용 임시 env를 만들고, 초기화가 끝나면 `DB_DDL_AUTO=update`, `DB_SQL_INIT_MODE=never` 상태로 되돌린다.

필수 값은 [GitHub Actions 설정값](github-actions-settings.md)을 따른다.

DB 초기화와 시드도 외부값으로 관리한다. 값 전환 시점은 [MVP 개발 가이드](../guides/mvp-development-guide.md)를 따른다.

| 목적 | `DB_DDL_AUTO` | `DB_SQL_INIT_MODE` |
| --- | --- | --- |
| 로컬에서 재시작마다 깨끗한 DB | `create` | `never` 또는 `always` |
| 로컬에서 재시작마다 깨끗한 DB + 시드 | `create` | `always` |
| 기존 데이터 보존 | `update` | `never` |
| 제출 전 스키마 확인 | `validate` | `never` |

시드 파일 경로는 `DB_SQL_INIT_DATA_LOCATIONS`로 바꾼다.

원격 서버를 seed 데이터로 다시 만들 때는 CD 설정을 `create/always`로 바꾸지 말고 `Reset DB` 워크플로를 수동 실행한다.

## 해커톤 추천

CI/CD 워크플로는 먼저 커밋해두고, AWS/EC2/RDS가 준비되면 GitHub Actions Variables/Secrets만 채워서 켜는 방식을 추천한다.

추천 순서:

1. 로컬에서 `.env.example`을 보고 필요한 값을 이해한다.
2. CI/CD 워크플로를 먼저 커밋한다.
3. EC2와 RDS를 만든다.
4. EC2에 Docker와 Docker Compose 플러그인을 설치한다.
5. GitHub Actions Variables에 일반 설정을 채우고, GitHub Actions Secrets에 비밀번호/토큰/개인 키를 채운다.
6. `main`에 푸시하거나 CD 워크플로를 수동 실행한다.
