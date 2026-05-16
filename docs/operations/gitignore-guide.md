# .gitignore 가이드

이 문서는 해커톤 중 Git에 올려야 하는 파일과 올리면 안 되는 파일을 구분하기 위한 가이드다.

## 원칙

저장소에는 팀원이 같은 환경을 재현하는 데 필요한 파일만 올린다. 개인 PC, IDE, 빌드 결과물, 실제 민감 정보는 올리지 않는다.

## 커밋해야 하는 파일

| 파일 | 이유 |
| --- | --- |
| `.env.example` | 로컬 개발 환경변수 예시 |
| `.env.prod.example` | 수동 EC2 Docker Compose 배포 환경변수 예시 |
| `gradle/wrapper/gradle-wrapper.jar` | 팀원이 같은 Gradle 버전으로 빌드하기 위해 필요 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle Wrapper 버전 정보 |
| `docker-compose.local.yml` | 로컬 MySQL 실행 방법 공유 |
| `docker-compose.prod.yml` | EC2 배포 구조 공유 |
| `src/main/resources/application-*.yml` | Spring 프로필별 설정 |
| `src/main/resources/db/seed/local-test-account.sql` | 로컬 테스트 계정 시드 |

## 커밋하면 안 되는 파일

| 파일/패턴 | 이유 |
| --- | --- |
| `.env`, `.env.local`, `.env.prod` | DB 비밀번호, JWT 서명 키, RDS 주소가 들어갈 수 있음 |
| `.idea/`, `*.iml` | 개인 IntelliJ 설정 |
| `.gradle/`, `build/`, `out/` | 빌드 캐시와 컴파일 결과물 |
| `docs-local/` | 개인 리서치, 에이전트 작업 기록, 내부 계획 문서 |
| `*.log`, `logs/` | 실행 로그 |
| `.DS_Store`, `Thumbs.db` | OS가 자동 생성하는 파일 |
| `*.pem`, `*.key`, `*.p12`, `*.jks` | SSH 키, 인증서, 키스토어 같은 비밀 파일 |

## IntelliJ 설정

IntelliJ는 Gradle 프로젝트를 열면 필요한 모듈 설정을 다시 만든다. `.idea`를 커밋하지 말고, 실행 방법은 README와 문서에 적는다.

## .env 파일 사용법

로컬에서 실제 값을 넣고 싶으면 예시 파일을 복사해서 사용한다.

```bash
cp .env.example .env
```

EC2에서 수동으로 Docker Compose를 실행할 때만 운영 예시를 복사한다.

```bash
cp .env.prod.example .env
```

생성한 `.env`는 `.gitignore`에 의해 무시된다. 실수로 올라가면 안 된다.

GitHub Actions에 입력할 값은 `docs/operations/github-actions-variables.template.env`와 `docs/operations/github-actions-secrets.template.env`를 기준으로 한다.

## 확인 명령

특정 파일이 왜 무시되는지 확인한다.

```bash
git check-ignore -v .env
git check-ignore -v .idea/workspace.xml
git check-ignore -v build/classes/java/main/App.class
```

전체 상태를 무시 파일까지 포함해서 본다.

```bash
git status --short --ignored
```

## 해커톤 중 자주 하는 실수

`.env`를 Slack이나 GitHub에 올리지 않는다. 공유가 필요하면 실제 비밀번호를 빼고 `.env.example` 형식으로 공유한다.

빌드 결과물인 `build/`를 커밋하지 않는다. 팀원은 `./gradlew build`나 `./scripts/verify.sh`로 다시 만들 수 있다.

IntelliJ 설정 충돌이 나면 `.idea`를 고치려 하지 말고, 프로젝트를 Gradle 프로젝트로 다시 열어본다.

개인용 Markdown은 `docs-local/`에 둔다. 팀원이 같이 봐야 하는 실행, API, 인증, 배포, 협업 문서만 `docs/`에 남긴다.

`docs-local/`은 Git뿐 아니라 Docker 빌드 컨텍스트에도 넣지 않는다. Docker 이미지는 애플리케이션 실행에 필요한 소스와 빌드 산출물만 포함해야 한다.
