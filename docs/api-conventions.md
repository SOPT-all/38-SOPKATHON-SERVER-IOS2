# API Conventions

웹, Android, iOS 팀과의 계약은 Swagger/OpenAPI와 이 문서를 기준으로 한다.

## Base

- Local API: `http://localhost:8080`
- API prefix: `/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## JSON

- Field naming: camelCase
- Content-Type: `application/json`
- Charset: UTF-8
- Time: ISO-8601 string

예시:

```json
{
  "createdAt": "2026-05-13T00:00:00Z"
}
```

## Success Response

모든 일반 성공 응답은 `ApiResponse<T>` 형식을 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS_200",
  "message": "요청이 성공했습니다.",
  "data": {
    "id": 1
  }
}
```

생성 응답:

```json
{
  "success": true,
  "code": "SUCCESS_201",
  "message": "리소스가 생성되었습니다.",
  "data": {
    "id": 1
  }
}
```

빈 성공 응답:

```json
{
  "success": true,
  "code": "SUCCESS_204",
  "message": "요청이 성공했고 응답 데이터가 없습니다."
}
```

## Error Response

모든 에러 응답은 trace id를 포함한다.

```json
{
  "success": false,
  "status": 400,
  "code": "COMMON_VALIDATION_FAILED",
  "message": "요청 값 검증에 실패했습니다.",
  "path": "/api/v1/auth/signup",
  "traceId": "8e4c6c6f-4e1b-4f02-80f5-77f6f31f174e",
  "timestamp": "2026-05-13T00:00:00Z",
  "errors": [
    {
      "field": "email",
      "message": "이메일 형식이 올바르지 않습니다.",
      "rejectedValue": "not-email"
    }
  ]
}
```

클라이언트는 사용자 메시지로 `message`를 우선 사용하고, 필드 단위 표시는 `errors[*].message`를 사용한다.

## Status Code

| Status | 의미 | 사용 예시 |
| --- | --- | --- |
| 200 | 성공 | 조회, 로그인 |
| 201 | 생성 | 회원가입, 리소스 생성 |
| 400 | 잘못된 요청 | validation 실패 |
| 401 | 인증 실패 | 토큰 없음, 토큰 만료 |
| 403 | 권한 없음 | 관리자 API 접근 실패 |
| 404 | 없음 | 리소스 없음 |
| 409 | 충돌 | 중복 이메일 |
| 500 | 서버 오류 | 알 수 없는 서버 실패 |

## Auth

로그인/회원가입 응답은 access token과 refresh token을 반환한다.

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

인증이 필요한 API 요청:

```http
Authorization: Bearer <accessToken>
```

refresh token은 `/api/v1/auth/refresh`로 재발급한다. 재발급 시 refresh token은 회전된다. 이전 refresh token은 다시 쓰면 안 된다.

## Pagination

페이지 응답은 `PageResponse<T>`를 사용한다.

```json
{
  "content": [
    {
      "id": 1,
      "title": "example"
    }
  ],
  "page": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

쿼리 파라미터 기본값:

- `page`: 0부터 시작
- `size`: 기본 20
- `sort`: 필요할 때만 사용

## CORS

로컬 기본 허용 origin:

- `http://localhost:3000`
- `http://localhost:5173`

해커톤 당일 배포 URL이 생기면 `APP_CORS_ALLOWED_ORIGINS`에 쉼표로 추가한다.

## File Upload

파일 업로드가 필요하면 S3 presigned URL 방식을 우선한다.

권장 흐름:

1. 클라이언트가 백엔드에 파일명, content type을 보낸다.
2. 백엔드는 presigned upload URL을 발급한다.
3. 클라이언트가 S3에 직접 업로드한다.
4. 클라이언트가 업로드 완료 API로 key를 백엔드에 저장한다.

초기 템플릿에는 S3 구현을 넣지 않고, `file` 패키지를 확장 지점으로 둔다.

## API Change Rule

API 변경이 있으면 PR에 반드시 표시한다.

- 요청 필드 추가/삭제/이름 변경
- 응답 필드 추가/삭제/이름 변경
- 에러 코드 추가/변경
- 인증 필요 여부 변경
- path 또는 method 변경

변경 후에는 Swagger를 확인하고 웹/모바일 팀에게 공유한다.
