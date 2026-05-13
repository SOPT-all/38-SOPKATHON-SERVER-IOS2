# Client Collaboration Guide

이 문서는 웹, Android, iOS 팀원이 백엔드와 연동할 때 보는 문서다.

## URLs

| 환경 | URL |
| --- | --- |
| Local API | `http://localhost:8080` |
| Local Swagger | `http://localhost:8080/swagger-ui.html` |
| Local Health | `http://localhost:8080/actuator/health` |
| Dev API | 해커톤 배포 후 공유 |
| Dev Swagger | `http://<EC2 주소>:8080/swagger-ui.html` |

모든 API는 `/api/v1` 아래에 있다.

## Test Account

로컬 DB migration에 테스트 계정이 들어간다.

| Field | Value |
| --- | --- |
| Email | `test@sopt.org` |
| Password | `password123!` |
| Role | `ROLE_USER` |

직접 회원가입해서 새 계정을 만들어도 된다.

## Quick Start for Clients

1. Swagger UI를 연다.
2. `/api/v1/auth/login` 또는 `/api/v1/auth/signup`으로 토큰을 받는다.
3. Swagger 우측 상단 Authorize에 access token을 넣는다.
4. 인증이 필요한 API는 `Authorization: Bearer <accessToken>` 헤더를 붙인다.
5. access token이 만료되면 `/api/v1/auth/refresh`에 refresh token을 보낸다.

## Example Login

Request:

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

Response:

```json
{
  "success": true,
  "code": "SUCCESS_200",
  "message": "요청이 성공했습니다.",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

## Error Handling on Client

백엔드 에러 응답은 항상 같은 모양이다.

- 공통 사용자 안내: `message`
- 입력 폼 표시: `errors[*].field`, `errors[*].message`
- 디버깅 공유: `traceId`

백엔드에 오류를 전달할 때는 다음을 같이 보내면 가장 빠르다.

- 요청 URL
- 요청 body
- 응답 status
- 응답 `code`
- 응답 `traceId`
- 발생 시간

## API Change Process

백엔드가 API를 바꿀 때:

1. PR template의 API 변경 항목을 체크한다.
2. Swagger를 갱신/확인한다.
3. `docs/api-conventions.md` 또는 기능 문서를 갱신한다.
4. 웹/Android/iOS 팀 채널에 변경 내용을 공유한다.

클라이언트가 필요한 API를 요청할 때:

1. 화면 이름과 사용자 행동을 설명한다.
2. 필요한 request/response 예시를 적는다.
3. 에러 상황을 같이 적는다.
4. 마감 시간을 적는다.

## Contract Rule

구두 합의보다 Swagger와 문서가 우선이다. 급하게 구두로 바뀐 내용도 10분 안에 Swagger나 문서에 반영한다.

## Local CORS

기본 허용:

- Web Vite: `http://localhost:5173`
- Web Next/React: `http://localhost:3000`

다른 포트를 쓰면 백엔드 `.env` 또는 실행 환경의 `APP_CORS_ALLOWED_ORIGINS`에 추가한다.
