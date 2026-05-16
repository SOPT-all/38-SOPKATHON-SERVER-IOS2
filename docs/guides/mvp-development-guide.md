# MVP 개발 가이드

해커톤 중 개발 속도를 위해 열어두는 설정과, 제출 전 다시 강화해야 하는 설정을 정리한다. `DB_DDL_AUTO`, 시드, 인증 허용, CORS, Swagger 서버 URL을 언제 바꿀지는 이 문서를 기준으로 판단한다.

프로필 선택, `.env` 파일 위치, GitHub Actions Variables/Secrets 기준은 [프로필과 환경변수 가이드](../operations/profile-env-guide.md)를 본다.

## 추천 단계

| 단계 | 목표 | 추천 설정 |
| --- | --- | --- |
| MVP 초반 | 화면에 필요한 CRUD/API를 먼저 연결 | `APP_SECURITY_PERMIT_ALL=true`, `DB_DDL_AUTO=create` |
| 로그인 연동 시작 | 로그인 사용자 기준 API 검증 | `APP_SECURITY_PERMIT_ALL=false` |
| 데이터 보존 공유 서버 | 웹/모바일이 같은 서버 호출 | `APP_SECURITY_PERMIT_ALL=false`, `DB_DDL_AUTO=update`, `DB_SQL_INIT_MODE=never` |
| 데이터 보존 시작 | 클라이언트가 만든 데이터를 유지 | `DB_DDL_AUTO=update`, `DB_SQL_INIT_MODE=never` |
| 테스트 데이터 초기화 | DB를 최신 스키마와 seed 데이터로 재생성 | `Reset DB` 워크플로 수동 실행 |
| 최종 제출 전 | DB와 엔티티 불일치 확인 | `DB_DDL_AUTO=validate`로 1회 실행 |

원격 서버의 일반 CD 배포는 DB를 초기화하지 않는다. 깨끗한 테스트 데이터가 필요할 때만 `Reset DB` 워크플로를 수동 실행한다.

## 설정별 기준

| 설정 | 로컬 기본값 | 언제 바꾸나 | 주의 |
| --- | --- | --- | --- |
| `APP_SECURITY_PERMIT_ALL` | `true` | 사용자 식별이 필요한 API를 만들 때 `false` | `true`이거나 공개 API에서는 `@CurrentUser`가 `null`일 수 있음 |
| `DB_DDL_AUTO` | `create` | 데이터를 보존해야 하면 `update`, 제출 전 스키마 검증 시 `validate` | `create`는 앱 시작마다 기존 테이블과 데이터를 지움 |
| `DB_SQL_INIT_MODE` | `always` | 시드가 필요 없으면 `never`, 시드를 넣으려면 `always` | `always`는 앱 시작마다 시드 SQL을 다시 실행함 |
| `DB_SQL_INIT_DATA_LOCATIONS` | `optional:classpath:db/seed/local-test-account.sql` | 시드 파일이 바뀌거나 여러 시드를 넣을 때 | 여러 파일은 쉼표로 구분 |
| `DB_DEFER_DATASOURCE_INITIALIZATION` | `true` | Hibernate 스키마 생성 뒤 시드를 실행해야 할 때 유지 | `create + 시드` 조합에서는 보통 `true` 필요 |
| `APP_CORS_ALLOWED_ORIGINS` | 로컬 웹 포트들 | 웹 배포 주소나 다른 로컬 포트가 생길 때 추가 | 브라우저 웹/웹뷰 호출에 중요 |
| `APP_SWAGGER_SERVER_URL` | `http://localhost:8080` | EC2/도메인 배포 후 실제 서버 주소로 변경 | Swagger의 Try it out 요청 대상 서버가 바뀜 |

## 자주 쓰는 값

로컬 초반 개발용으로 DB를 매번 초기화한다.

```bash
DB_DDL_AUTO=create
DB_SQL_INIT_MODE=always
DB_SQL_INIT_DATA_LOCATIONS=optional:classpath:db/seed/local-test-account.sql
```

초기화는 하되 시드는 넣지 않는다.

```bash
DB_DDL_AUTO=create
DB_SQL_INIT_MODE=never
```

데이터를 보존한다.

```bash
DB_DDL_AUTO=update
DB_SQL_INIT_MODE=never
```

인증을 켠다.

```bash
APP_SECURITY_PERMIT_ALL=false
```

제출 전 스키마를 검증한다.

```bash
DB_DDL_AUTO=validate
```

공유 서버에서 로컬 시드를 끈다.

```bash
DB_SQL_INIT_MODE=never
```

웹 Origin을 추가한다.

```bash
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://127.0.0.1:5173,https://example.com
```

Swagger 요청 서버를 배포 주소로 맞춘다.

```bash
APP_SWAGGER_SERVER_URL=http://<EC2_PUBLIC_IP>:8080
```

원격 서버 DB를 seed로 다시 만든다.

```text
GitHub Actions -> Reset DB -> Run workflow
confirmReset=RESET
seedFiles=local-test-account
```

## 당일 흐름

1. Docker MySQL을 띄우고 local 프로필로 앱을 실행한다.
2. 기능 API와 DTO를 먼저 만든 뒤 Swagger로 웹/모바일 팀과 맞춘다.
3. 로그인 사용자가 필요해지는 순간 `APP_SECURITY_PERMIT_ALL=false`로 바꾼다.
4. 데이터 보존이 필요한 원격 서버는 `DB_DDL_AUTO=update`, `DB_SQL_INIT_MODE=never`로 배포한다.
5. 테스트 데이터가 꼬이면 `Reset DB` 워크플로를 수동 실행해서 최신 스키마와 seed 데이터로 다시 만든다.
6. 클라이언트가 만든 데이터를 보존해야 하는 시점부터는 Reset DB 실행을 팀에서 명시적으로 합의한다.
7. 제출 전 `DB_DDL_AUTO=validate`로 1회 실행하고 `./scripts/verify.sh`로 최종 확인한다.

## 다른 문서와의 관계

- 프로필과 `.env` 파일 자체의 기준은 [프로필과 환경변수 가이드](../operations/profile-env-guide.md)가 관리한다.
- 로컬/EC2/RDS 실행 구조는 [인프라 구조](../operations/infra.md)가 설명한다.
- 아키텍처 문서는 설정값을 반복하지 않고 이 문서를 참조한다.
