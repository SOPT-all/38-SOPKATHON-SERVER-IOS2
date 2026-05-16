# GitHub Actions 설정값

이 문서는 CD 워크플로가 읽는 GitHub Actions Variables와 Secrets를 설명한다. 배포 준비 전체 흐름은 [배포 준비 가이드](../runbooks/deploy.md)를 먼저 보고, 여기서는 GitHub 화면에 어떤 값을 넣을지만 확인한다.

원칙은 단순하다.

```text
Secrets = 노출되면 안 되는 민감 정보
Variables = 민감 정보는 아니지만 배포마다 바뀔 수 있는 일반 설정값
Profile = 코드에 둬도 되는 기본 동작
```

GitHub 위치:

```text
Repository
-> Settings
-> Secrets and variables
-> Actions
```

## 템플릿

값을 넣기 전에 아래 두 파일을 보고 필요한 항목을 만든다.

- Variables 템플릿: [github-actions-variables.template.env](github-actions-variables.template.env)
- Secrets 템플릿: [github-actions-secrets.template.env](github-actions-secrets.template.env)

템플릿 파일을 그대로 업로드하는 기능은 GitHub UI에 없다. 각 줄의 `KEY=value`를 보고 GitHub 화면에서 하나씩 추가한다.

## 필수 변수

| 변수 | 예시 | 설명 |
| --- | --- | --- |
| `DOCKERHUB_USERNAME` | `myteam` | Docker Hub 계정 이름 |
| `EC2_HOST` | `1.2.3.4` | EC2 공개 IP 또는 도메인 |
| `EC2_USERNAME` | `ec2-user` | EC2 SSH 사용자 |
| `DB_URL` | `jdbc:mysql://...` | RDS MySQL JDBC URL |
| `DB_USERNAME` | `admin` | RDS DB 사용자 이름 |

## 선택 변수

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `EC2_SSH_PORT` | `22` | SSH 포트를 바꾼 경우만 설정 |
| `DOCKER_PLATFORM` | `linux/arm64` | t3/t2 같은 x86 EC2는 `linux/amd64` |
| `HOST_PORT` | `8080` | EC2 외부에 열 포트 |
| `SERVER_PORT` | `8080` | 컨테이너 내부 Spring Boot 포트 |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError` | 작은 EC2용 JVM 옵션 |
| `DB_DDL_AUTO` | `update` | 초반 초기화는 `create`, 보존은 `update`, 제출 전 검증은 `validate` |
| `DB_DEFER_DATASOURCE_INITIALIZATION` | `true` | `create + 시드`에서 테이블 생성 뒤 시드 실행 |
| `DB_SQL_INIT_MODE` | `never` | 시드 실행은 `always`, 시드 끄기는 `never` |
| `DB_SQL_INIT_DATA_LOCATIONS` | `optional:classpath:db/seed/local-test-account.sql` | 시드 SQL 경로 |
| `APP_SECURITY_PERMIT_ALL` | `false` | 원격 서버에서 인증을 전체 허용할 때만 `true` |
| `APP_CORS_ALLOWED_ORIGINS` | 로컬 웹 Origin | 웹 클라이언트 Origin |
| `APP_CORS_ALLOWED_METHODS` | `GET,POST,PUT,PATCH,DELETE,OPTIONS` | 특수 method가 필요할 때만 설정 |
| `APP_CORS_ALLOWED_HEADERS` | `*` | 특수 요청 header 제한이 필요할 때만 설정 |
| `APP_CORS_EXPOSED_HEADERS` | `Authorization,Location` | 브라우저 JS가 읽어야 하는 응답 header |
| `APP_CORS_ALLOW_CREDENTIALS` | `true` | 인증 header/cookie 포함 요청 허용 여부 |
| `APP_SWAGGER_SERVER_URL` | `http://EC2_HOST:HOST_PORT` | Swagger Try it out 대상 서버 |
| `APP_SWAGGER_SERVER_DESCRIPTION` | `Production` | Swagger 서버 설명 |

## 필수 Secrets

| Secret | 설명 |
| --- | --- |
| `DOCKERHUB_TOKEN` | Docker Hub 액세스 토큰 |
| `EC2_SSH_KEY` | EC2 SSH 개인 키 전체 내용 |
| `DB_PASSWORD` | RDS DB 비밀번호 |
| `APP_JWT_SECRET` | JWT 서명 키 |

## DB 모드 프리셋

서버 하나로 개발과 출품을 같이 하면 기본은 `update`를 권장한다. 깨끗한 DB가 필요할 때만 `create`를 명시한다.

앱 시작마다 DB를 초기화한다.

```text
DB_DDL_AUTO=create
DB_SQL_INIT_MODE=never
```

앱 시작마다 초기화하면서 시드도 넣는다.

```text
DB_DDL_AUTO=create
DB_SQL_INIT_MODE=always
DB_SQL_INIT_DATA_LOCATIONS=optional:classpath:db/seed/local-test-account.sql
```

클라이언트가 만든 데이터를 보존한다.

```text
DB_DDL_AUTO=update
DB_SQL_INIT_MODE=never
```

제출 직전 스키마를 확인한다.

```text
DB_DDL_AUTO=validate
DB_SQL_INIT_MODE=never
```

`create`는 앱 시작마다 기존 테이블과 데이터를 지운다. 원격 서버에서는 일반 CD 설정을 `create/always`로 남기지 말고, 의도적인 초기화가 필요할 때 Reset DB 워크플로를 쓴다.

## Reset DB 워크플로

`main` push로 실행되는 `CD` 워크플로는 DB를 초기화하지 않는다. 배포된 서버의 DB를 의도적으로 비우고 seed를 다시 넣을 때만 GitHub Actions 화면에서 `Reset DB` 워크플로를 수동 실행한다.

실행 위치:

```text
Repository
-> Actions
-> Reset DB
-> Run workflow
```

입력값:

| 입력값 | 예시 | 설명 |
| --- | --- | --- |
| `confirmReset` | `RESET` | 실수 실행을 막기 위한 확인 문구. 정확히 `RESET`이어야 한다. |
| `seedFiles` | `local-test-account` | `src/main/resources/db/seed` 아래 seed 파일 이름. `.sql`은 생략할 수 있다. |

`seedFiles` 변환 예:

```text
local-test-account
-> optional:classpath:db/seed/local-test-account.sql
```

여러 seed는 쉼표로 구분한다. 추가 seed를 쓰려면 먼저 `src/main/resources/db/seed` 아래에 같은 이름의 SQL 파일을 추가해야 한다.

Reset DB는 새 Docker image를 빌드하거나 pull하지 않는다. 현재 EC2에 배포된 image로 `DB_DDL_AUTO=create`를 한 번 실행하고, `seedFiles`가 있으면 `DB_SQL_INIT_MODE=always`, 비어 있으면 `never`로 실행한다. 끝나면 최종 설정을 `DB_DDL_AUTO=update`, `DB_SQL_INIT_MODE=never`로 되돌린다. 새 seed 파일이나 새 Entity를 반영하려면 먼저 `main` push로 CD 배포를 완료한 뒤 Reset DB를 실행한다.

## Docker platform

기본 배포 대상은 t4g.micro 같은 Graviton EC2라서 CD는 `linux/arm64` 이미지를 만든다.

```text
DOCKER_PLATFORM=linux/arm64
```

t3/t2 같은 x86 EC2를 쓰면 변수에 아래 값을 둔다.

```text
DOCKER_PLATFORM=linux/amd64
```

EC2 CPU 아키텍처와 Docker image platform이 다르면 `exec format error`가 날 수 있다.
