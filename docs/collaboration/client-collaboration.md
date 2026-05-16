# 클라이언트 협업 가이드

웹, Android, iOS 팀원이 백엔드와 연동할 때 보는 문서다. 상세 응답/에러 규칙은 [API 규칙](../standards/api-conventions.md)을 기준으로 하고, 이 문서는 실제 협업 절차와 공유 정보를 다룬다.

## URL

| 환경 | URL |
| --- | --- |
| 로컬 API | `http://localhost:8080` |
| 로컬 Swagger | `http://localhost:8080/swagger-ui.html` |
| 로컬 헬스 체크 | `http://localhost:8080/actuator/health` |
| 공유 API | 해커톤 배포 후 공유 |
| 공유 Swagger | `http(s)://<배포 API 주소>/swagger-ui.html` |

모든 API는 `/api/v1` 아래에 있다.

배포 URL이 정해지면 백엔드 담당자가 공유 API와 공유 Swagger를 팀 채널에 고정 메시지로 공유한다.

## 처음 연동 순서

프론트엔드 개발자는 보통 아래 순서로 보면 된다.

1. 공유 Swagger에서 호출할 API의 method, path, 요청 body, 응답 예시를 확인한다.
2. 앱의 API base URL을 환경별로 분리한다. 예: `http://localhost:8080/api/v1`, `https://<배포 API 주소>/api/v1`
3. 로그인/회원가입으로 `accessToken`을 받는다.
4. 인증이 필요한 API에만 `Authorization: Bearer <accessToken>` 헤더를 붙인다.
5. 성공 응답은 `data`를 읽고, 실패 응답은 `message`, `code`, `errors`를 읽는다.
6. 목록 API는 `data.content`와 `data.page`를 기준으로 화면을 만든다.

프론트 코드에서 `/api/v1`을 base URL과 endpoint 양쪽에 중복으로 붙이지 않는다.

## 테스트 계정

local 프로필에서는 시드 SQL로 테스트 계정이 들어간다.

| 항목 | 값 |
| --- | --- |
| 이메일 | `test@sopt.org` |
| 비밀번호 | `password123!` |
| 역할 | `ROLE_USER` |

직접 회원가입해서 새 계정을 만들어도 된다.

## Swagger 토큰 테스트

1. Swagger UI를 연다.
2. 인증이 필요 없는 API는 바로 호출한다.
3. 인증이 필요한 API는 `/api/v1/auth/login` 또는 `/api/v1/auth/signup`으로 토큰을 받는다.
4. Swagger 우측 상단 Authorize에는 `accessToken` 값만 넣는다.
5. 실제 앱 요청에서는 `Authorization: Bearer <accessToken>` 헤더를 붙인다.
6. 액세스 토큰이 만료되면 다시 로그인한다. 기본 만료 시간은 48시간이다.
7. 로그아웃은 서버 API 호출이 아니라 클라이언트가 저장한 액세스 토큰을 삭제한다.

현재 템플릿은 리프레시 토큰을 제공하지 않는다. 리프레시 토큰이 필요하면 백엔드와 클라이언트가 만료/재발급/로그아웃 정책을 먼저 합의한 뒤 API를 추가한다.

## 로그인 예시

요청:

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "test@sopt.org",
  "password": "password123!"
}
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS_200",
  "message": "요청이 성공했습니다.",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "eyJ..."
  }
}
```

## 클라이언트 에러 처리

백엔드 에러 응답은 항상 같은 모양이다. 전체 예시는 [API 규칙](../standards/api-conventions.md#에러-응답)을 본다.

- 공통 사용자 안내: `message`
- 입력 폼 표시: `errors[*].field`, `errors[*].message`
- 디버깅 공유: `traceId`

`null` 필드는 기본 생략된다. `errors`는 실패 원인이 필드와 직접 연결되지 않아도 빈 배열 `[]`로 온다.

필수 query parameter 누락, path variable 타입 오류, parameter 검증 실패도 같은 에러 응답으로 내려오므로 클라이언트는 별도 예외 처리 없이 `code`, `message`, `errors`를 읽으면 된다.

상태별 기본 처리는 아래처럼 맞춘다.

| 상태 | 프론트 처리 |
| --- | --- |
| 400 | `errors`가 있으면 필드 옆에 표시하고, 없으면 `message`를 보여준다. |
| 401 | 저장한 토큰을 지우고 로그인 화면이나 로그인 유도 상태로 보낸다. |
| 403 | 권한이 없다는 안내를 보여준다. |
| 404 | 없는 리소스 화면이나 토스트로 처리한다. |
| 409 | 중복/충돌 메시지를 보여주고 사용자가 수정할 수 있게 한다. |
| 500 | `traceId`와 함께 백엔드에 공유한다. |

백엔드에 오류를 전달할 때는 아래 정보를 같이 보낸다.

- 요청 URL
- 요청 메서드
- 요청 본문
- 응답 상태
- 응답 `code`
- 응답 `traceId`
- 발생 시간

민감정보는 공유하지 않는다. 비밀번호, 토큰, Authorization 헤더는 제거하거나 마스킹한다.

## API 변경 절차

백엔드가 API를 바꿀 때:

1. PR 템플릿의 API 변경 항목을 체크한다.
2. Swagger를 갱신/확인한다.
3. [API 규칙](../standards/api-conventions.md) 또는 기능 문서를 갱신한다.
4. 웹/Android/iOS 팀 채널에 변경 내용을 공유한다.
5. 이미 붙은 화면이 있으면 적용 마감 시간을 같이 정한다.

클라이언트가 필요한 API를 요청할 때:

1. 화면 이름과 사용자 행동을 설명한다.
2. 필요한 요청/응답 예시를 적는다.
3. 에러 상황을 같이 적는다.
4. 마감 시간을 적는다.

요청 예시:

```text
화면: 마이페이지
행동: 사용자가 내 정보를 조회한다.
필요 API: GET /api/v1/me
응답 예시: id, nickname, email
에러: 토큰 만료, 탈퇴한 사용자
마감: 오늘 18:00
```

## 계약 기준

구두 합의보다 Swagger와 문서가 우선이다. 급하게 구두로 바뀐 내용도 10분 안에 Swagger나 문서에 반영한다.

로컬 CORS 기본 허용 Origin은 `localhost:3000`, `localhost:5173`, `127.0.0.1:3000`, `127.0.0.1:5173`이다. 다른 포트를 쓰면 `APP_CORS_ALLOWED_ORIGINS`에 추가한다.

CORS는 브라우저 웹에서만 주로 발생한다. 요청할 때는 `https://example.com`처럼 scheme, host, port가 정확히 일치해야 한다. 프론트 배포 주소가 생기면 백엔드에 Origin 전체를 공유한다.

## 프론트엔드 체크리스트

- 기본 URL 끝에 `/api/v1`을 중복으로 붙이지 않았는가?
- JSON 필드가 camelCase인지 확인했는가?
- 성공 응답에서 `data`가 없을 수 있다는 점을 처리했는가?
- `success=false`일 때 `message`와 `errors[*].message`를 분리해서 처리하는가?
- 401 응답에서 토큰 삭제와 로그인 유도 흐름이 있는가?
- 토큰 저장 위치와 삭제 시점이 팀 보안 기준과 맞는가?
- API 변경 공유를 받으면 Swagger 예시를 기준으로 모의 데이터를 갱신했는가?
