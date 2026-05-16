# API 규칙

웹, Android, iOS 팀과의 API 계약은 Swagger/OpenAPI와 이 문서를 기준으로 한다. 기능별 설명보다 공통 응답 형식, 인증 방식, 변경 절차를 우선 정의한다.

## 기본 정보

- 로컬 API: `http://localhost:8080`
- API 접두사: `/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 헬스 체크: `http://localhost:8080/actuator/health`

## JSON 규칙

- 필드 이름: camelCase
- Content-Type: `application/json`
- 인코딩: UTF-8
- 시간: ISO-8601 문자열, UTC 기준
- null 필드: 기본 생략. 목록은 값이 없으면 `[]`로 내려보낸다.

## 응답 구조 요약

| 구분 | 항상 있는 필드 | 있을 때만 있는 필드 |
| --- | --- | --- |
| 성공 | `success`, `code`, `message` | `data` |
| 에러 | `success`, `status`, `code`, `message`, `path`, `traceId`, `timestamp`, `errors` | `errors[*].rejectedValue` |

## 성공 응답

모든 일반 성공 응답은 `ApiResponse<T>` 형식을 사용한다. 컨트롤러에서 엔티티를 직접 반환하지 않는다.

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

생성 응답은 `201 Created`와 `ApiResponse.created(data)`를 사용한다.

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

빈 성공 응답은 HTTP 200 또는 204 정책 중 엔드포인트 성격에 맞춰 정하되, 본문을 내려보내는 경우 `ApiResponse.noContent()` 계열의 공통 메시지를 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS_204",
  "message": "요청이 성공했고 응답 데이터가 없습니다."
}
```

## 에러 응답

모든 에러 응답은 공통 형식과 trace id를 유지한다.

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

클라이언트는 사용자 메시지로 `message`를 우선 사용하고, 필드 단위 표시는 `errors[*].message`를 사용한다. `password`, `token`, `secret`, `credential`, `authorization` 같은 민감 필드의 `rejectedValue`는 `[MASKED]`로 내려간다.

잘못 보낸 값이 없거나 보여줄 필요가 없으면 `rejectedValue`는 생략될 수 있다.

`@RequestBody` 검증 실패뿐 아니라 필수 query parameter 누락, query/path variable 타입 오류, `@Min` 같은 parameter 제약 위반도 같은 에러 형식으로 내려간다.

비즈니스 실패는 `BusinessException`과 도메인별 `ErrorCode` enum으로 표현한다. 새 에러 코드가 생기면 Swagger 예시와 이 문서 또는 기능 문서에 반영한다.

## 상태 코드

| 상태 | 의미 | 사용 예시 |
| --- | --- | --- |
| 200 | 성공 | 조회, 로그인 |
| 201 | 생성 | 회원가입, 리소스 생성 |
| 400 | 잘못된 요청 | 검증 실패 |
| 401 | 인증 실패 | 토큰 없음, 토큰 만료 |
| 403 | 권한 없음 | 관리자 API 접근 실패 |
| 404 | 없음 | 리소스 없음 |
| 409 | 충돌 | 중복 이메일 |
| 500 | 서버 오류 | 알 수 없는 서버 실패 |

## 인증

로그인/회원가입 응답은 해커톤 시연용 48시간 액세스 토큰만 반환한다. 현재 템플릿은 리프레시 토큰, 재발급 엔드포인트, 서버 로그아웃 API를 제공하지 않는다.

로그인은 `200 OK`와 `SUCCESS_200`을 사용한다.

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

회원가입은 사용자가 새로 만들어지므로 `201 Created`와 `SUCCESS_201`을 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS_201",
  "message": "리소스가 생성되었습니다.",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "eyJ..."
  }
}
```

인증이 필요한 API 요청:

```http
Authorization: Bearer <accessToken>
```

액세스 토큰이 만료되면 다시 로그인하고, 로그아웃은 클라이언트가 저장한 토큰을 삭제한다.

추후 리프레시 토큰이 필요해지면 `auth/domain`, `auth/repository`, 토큰 저장소, 교체 정책, 로그아웃/탈취 대응 정책을 함께 설계한 뒤 별도 PR로 추가한다. 임시로 재발급 응답 필드만 먼저 열지 않는다.

## 페이지네이션

페이지 응답은 `PageResponse<T>`를 사용한다. Spring `Page<T>` 내부 필드를 그대로 노출하지 않는다.

```json
{
  "success": true,
  "code": "SUCCESS_200",
  "message": "요청이 성공했습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "example"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "numberOfElements": 1,
      "totalElements": 1,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  }
}
```

쿼리 파라미터 기본값은 `page=0`, `size=20`이며 `sort`는 필요할 때만 사용한다.

목록 API는 정렬 기준을 문서화한다. 정렬 안정성이 필요한 화면은 `createdAt,id`처럼 tie-breaker를 둔다.

## CORS와 파일 업로드

로컬 CORS 기본값과 Origin 추가 기준은 [클라이언트 협업 가이드](../collaboration/client-collaboration.md)와 [프로필과 환경변수 가이드](../operations/profile-env-guide.md)를 따른다.

파일 업로드가 필요하면 S3 presigned URL 방식을 우선한다. 초기 템플릿에는 S3 구현을 넣지 않고, 필요할 때 `file` 패키지를 확장한다.

## API 변경 규칙

API 변경이 있으면 PR에 반드시 표시한다. 구두 합의만으로 API를 바꾸지 않는다.

- 요청 필드 추가/삭제/이름 변경
- 응답 필드 추가/삭제/이름 변경
- 에러 코드 추가/변경
- 인증 필요 여부 변경
- 경로 또는 메서드 변경
- 페이지네이션, 정렬, 널 허용 정책 변경
- 토큰 만료 시간 또는 인증 헤더 변경

변경 후에는 Swagger를 확인하고 웹/모바일 팀에게 공유한다.

## Swagger 규칙

- 새 공개 API는 작업 요약과 요청/응답 예시를 둔다.
- 인증이 필요한 API는 Swagger에서 Bearer 인증 여부가 드러나야 한다.
- 널 허용, enum 값, page/sort 기본값은 스키마나 설명에 남긴다.
- 실제 응답 형식과 Swagger 예시가 다르면 실제 코드를 고치거나 문서를 즉시 갱신한다.
