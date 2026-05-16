# 인증 개발 가이드

인증을 켠 뒤 기능 API를 개발하고 검증하는 방법을 정리한다. 로그인 요청/응답의 전체 JSON 계약은 [API 규칙](../standards/api-conventions.md), 클라이언트 연동 흐름은 [클라이언트 협업 가이드](../collaboration/client-collaboration.md)를 기준으로 한다.

## 현재 선택

이 템플릿은 해커톤 시연 속도를 위해 자체 회원가입/로그인과 48시간 JWT 액세스 토큰만 제공한다. 클라이언트는 토큰을 `Authorization` 헤더로 보내고, 컨트롤러는 필요한 경우 `@CurrentUser`를 사용한다.

| 항목 | 현재 선택 |
| --- | --- |
| 토큰 종류 | 액세스 토큰 1개 |
| 만료 시간 | 기본 48시간, `APP_JWT_ACCESS_TOKEN_EXPIRATION_MINUTES=2880` |
| 로그아웃 | 서버 API 없음. 클라이언트가 저장한 토큰 삭제 |
| 리프레시 토큰 | 제공하지 않음 |
| 운영 확장 | 필요해질 때 리프레시 토큰 저장/폐기와 폐기 전략을 별도 설계 |

## 언제 인증을 켜나

| 단계 | 언제 쓰나 | 설정 |
| --- | --- | --- |
| 0단계: 인증 없음 | 주제 공개 직후, 화면/API 계약을 빨리 붙일 때 | `APP_SECURITY_PERMIT_ALL=true` |
| 1단계: 단순 JWT | 내 정보, 내 글, 신청/저장처럼 사용자 식별이 필요할 때 | `APP_SECURITY_PERMIT_ALL=false` |

처음부터 모든 API에 인증을 붙이지 않는다. 공개 목록/상세처럼 사용자 식별이 필요 없는 API는 공개로 둔다.

소셜 로그인은 현재 템플릿에 없다. 평가나 주제상 꼭 필요할 때만 별도 기능으로 추가한다.

## 인증 켜기

local 프로필은 기본적으로 인증을 전체 허용한다.

```bash
APP_SECURITY_PERMIT_ALL=true
```

로그인 사용자 기준 기능을 만들기 시작하면 아래처럼 바꾼다.

```bash
APP_SECURITY_PERMIT_ALL=false
```

그 뒤 `/api/v1/auth/login`으로 액세스 토큰을 받고, 보호 API 요청에 헤더를 붙인다.

```http
Authorization: Bearer <accessToken>
```

Swagger에서는 우측 상단 Authorize에 `accessToken` 값만 넣고 테스트한다. 실제 HTTP 요청에서는 `Authorization: Bearer <accessToken>` 헤더를 보낸다. 인증이 필요한 API를 Swagger에서 명확히 보이게 하려면 컨트롤러 메서드에 `@SecurityRequirement(name = "bearerAuth")`를 붙인다.

## 컨트롤러에서 현재 사용자 받기

인증된 사용자가 필요한 API는 `@CurrentUser`를 사용한다.

```java
@GetMapping("/me")
public ApiResponse<MyPageResponse> me(@CurrentUser AuthenticatedUser user) {
    Long memberId = user.memberId();
    return ApiResponse.success(memberService.getMyPage(memberId));
}
```

규칙:

- `user.memberId()`는 인증이 필수인 API에서만 호출한다.
- `APP_SECURITY_PERMIT_ALL=true`이거나 공개 API에서는 `user`가 `null`일 수 있다.
- 서비스에는 보통 `memberId`만 넘기고, 전체 `Member`가 필요하면 서비스에서 DB 조회한다.
- 컨트롤러에서 JWT를 직접 파싱하지 않는다.
- 컨트롤러 테스트에서 현재 사용자가 필요하면 `@WithMockCustomUser`를 붙인다.

공개 API를 추가하려면 `application.yml`의 `app.security.public-endpoints`에 경로를 추가한다.

```yaml
app:
  security:
    public-endpoints:
      - /api/v1/auth/**
      - /api/v1/examples/**
      - /api/v1/posts/public/**
```

## 스모크 테스트

앱 실행 후 인증 흐름은 아래 스크립트로 확인한다.

```bash
./scripts/auth-smoke-test.sh
```

다른 서버를 확인할 때:

```bash
BASE_URL=http://<배포 API 주소>:8080 ./scripts/auth-smoke-test.sh
```

다른 계정을 쓸 때:

```bash
AUTH_SMOKE_EMAIL=user@example.com AUTH_SMOKE_PASSWORD=password123! ./scripts/auth-smoke-test.sh
```

스크립트는 헬스 체크, 로그인, 액세스 토큰 발급, 리프레시 토큰 미포함, 보호 API 호출 가능 여부를 확인한다. 공유 서버나 운영 환경은 보통 `DB_SQL_INIT_MODE=never`이므로 스모크 테스트 전에 회원가입 API나 별도 시드로 계정을 준비한다.

## 자주 나는 인증 문제

| 증상 | 확인 |
| --- | --- |
| 새 API가 401 반환 | `APP_SECURITY_PERMIT_ALL=false`인지, 토큰 헤더가 있는지, 공개 API라면 공개 엔드포인트에 있는지 |
| Swagger에서는 되는데 웹에서는 401 | 웹 요청에 `Authorization: Bearer <accessToken>`이 실제로 들어가는지 |
| 403 반환 | 인증은 됐지만 역할이 부족한 상태다. 예: `ROLE_USER`로 관리자 API 호출 |
| `@CurrentUser`가 null | 인증 전체 허용 상태, 공개 API, 헤더 누락, 토큰 만료 여부 |
| 액세스 토큰 만료 | 리프레시 토큰이 없으므로 다시 로그인해서 새 토큰을 받는다 |

## 개발 순서

1. 사용자 식별이 정말 필요한 API인지 판단한다.
2. 필요하면 `APP_SECURITY_PERMIT_ALL=false`로 실행한다.
3. Swagger에서 로그인하고 액세스 토큰으로 호출한다.
4. 컨트롤러에 `@CurrentUser AuthenticatedUser user`를 추가한다.
5. 서비스에는 `memberId`만 넘긴다.
6. 공개 API가 필요한 경우에만 `public-endpoints`에 추가한다.
7. 변경 사항을 Swagger와 웹/모바일 팀에 공유한다.
