# 백엔드 규칙

이 문서는 백엔드 기능을 추가할 때 지킬 기준이다. 목적은 느린 절차를 만드는 것이 아니라, 짧은 해커톤 중에도 API 계약과 코드 구조가 무너지지 않게 하는 것이다.

## 패키지 구조

기본 구조는 기능별 패키지(package-by-feature)다.

```text
org.sopt.sopkathon
├── global
│   ├── config
│   ├── error
│   ├── response
│   ├── security
│   └── web
├── auth
│   ├── api
│   ├── application
│   ├── dto
│   └── error
├── member
│   ├── domain
│   ├── error
│   └── repository
└── example
    ├── api
    └── dto
```

새 기능은 필요에 따라 `feature/api`, `feature/application`, `feature/domain`, `feature/dto`, `feature/error`, `feature/repository` 형태로 추가한다. DB 엔티티나 리포지토리가 없는 기능은 `domain`, `repository`를 억지로 만들지 않는다. 예를 들어 현재 `auth`는 JWT 발급 유스케이스만 가지므로 `auth/domain`, `auth/repository`가 없다.

## 계층 규칙

| 계층 | 책임 | 규칙 |
| --- | --- | --- |
| `api` | HTTP 요청/응답, 검증 | 비즈니스 로직을 넣지 않는다. |
| `application` | 유스케이스, 트랜잭션 | 리포지토리와 도메인을 조합한다. |
| `domain` | 엔티티, enum, 도메인 규칙 | HTTP나 Spring MVC 타입을 모른다. |
| `repository` | DB 접근 | Spring Data JPA 리포지토리를 둔다. |
| `dto` | 요청/응답 모델 | 엔티티를 직접 노출하지 않는다. |
| `error` | 도메인별 실패 코드 | `ErrorCode` enum을 둔다. |

새 패키지를 추가할 때는 아래 기준을 따른다.

- `api`: 외부 HTTP 엔드포인트가 있을 때만 둔다.
- `application`: 하나 이상의 유스케이스나 트랜잭션 경계가 있으면 둔다.
- `domain`: 엔티티, enum, 값 객체, 도메인 규칙이 있을 때 둔다.
- `repository`: DB 조회/저장이 필요할 때 둔다.
- `dto`: 요청/응답 모델이 있을 때 둔다.
- `error`: 클라이언트가 구분해야 하는 실패 케이스가 있을 때 둔다.

## 이름 규칙

- Controller: `AuthController`
- Service: `AuthService`
- Request DTO: `SignUpRequest`
- Response DTO: `TokenResponse`
- Repository: `MemberRepository`
- Entity: `Member`
- Enum: `Role`
- Test: `AuthServiceTest`, `AuthControllerTest`

## Java 코드 스타일

- 기본 포맷은 `.editorconfig`를 따른다. Java는 space 4칸, UTF-8, LF, 파일 끝 개행을 사용한다.
- 클래스 멤버 순서는 상수, 필드, 생성자, 정적 팩터리, public 메서드, private 메서드 순서로 둔다.
- 의존성 주입은 생성자 주입을 사용하고, 주입받는 필드는 `final`로 둔다. Field injection은 사용하지 않는다.
- DTO는 가능하면 record로 작성한다. 엔티티는 JPA 요구 때문에 class로 작성한다.
- JPA 엔티티는 protected 기본 생성자, 의미 있는 정적 팩터리, 필요한 getter만 둔다.
- Lombok은 기본으로 쓰지 않는다. 사용하더라도 엔티티에 `@Data`, `@Setter`처럼 변경 범위가 큰 애너테이션은 붙이지 않는다.
- `Optional`은 repository 조회 결과처럼 부재를 표현하는 경계에서만 사용한다. DTO 필드나 엔티티 필드에는 쓰지 않는다.
- 목록 응답은 `null` 대신 빈 리스트를 사용한다.
- 주석은 코드만 봐서는 놓치기 쉬운 의도나 운영상 주의점에만 남긴다.

## 트랜잭션

- 조회 전용 서비스 메서드는 `@Transactional(readOnly = true)`를 기본으로 한다.
- 쓰기 메서드는 메서드 단위로 `@Transactional`을 붙인다.
- Controller에는 transaction을 두지 않는다.

## DTO와 검증

- 컨트롤러 요청 본문은 record DTO를 사용한다.
- 필수값은 `@NotBlank`, 범위는 `@Size`, 이메일은 `@Email`을 사용한다.
- 검증 메시지는 클라이언트가 그대로 보여줘도 이해할 수 있게 한국어로 작성한다.
- 응답 DTO는 클라이언트 화면에 필요한 값만 담는다.
- 비밀번호, secret 같은 민감값은 응답 DTO와 로그에 넣지 않는다.
- 토큰은 로그인/회원가입처럼 발급 API 응답에만 담고, 로그나 일반 응답 DTO에는 넣지 않는다.

## 에러 처리

비즈니스 실패는 `BusinessException`으로 던진다.

```java
throw new BusinessException(MemberErrorCode.MEMBER_DUPLICATE_EMAIL);
```

에러 코드는 도메인별 enum으로 나누고 `ErrorCode` 인터페이스를 구현한다. 각 enum에는 다음 정보를 둔다.

- HTTP 상태
- 내부 에러 코드
- 기본 메시지
- 로그 레벨

새 실패 케이스가 생기면 먼저 해당 도메인의 에러 코드 enum에 추가하고, API 문서에도 반영한다. 공통 HTTP/검증 실패는 `CommonErrorCode`, 보안 필터에서 직접 만드는 인증/인가 실패는 `SecurityErrorCode`에 둔다.

예상 가능한 실패를 `IllegalArgumentException`, `RuntimeException`으로 흘리지 않는다. 클라이언트가 분기해야 하는 실패는 고유한 `ErrorCode`를 갖는다.

## 응답

성공 응답은 `ApiResponse<T>`를 사용한다.

```java
return ApiResponse.success(response);
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
```

페이지 응답은 `PageResponse<T>`를 사용한다. Spring `Page<T>`를 그대로 외부에 노출하지 않는다.

```java
return ApiResponse.success(PageResponse.from(page));
```

`PageResponse.page.number`는 Spring 기준이라 0부터 시작한다. 클라이언트가 1부터 보여줘야 하면 프론트에서 표시값만 변환한다.

## 인증 사용자 편의 도구

인증된 사용자가 필요한 컨트롤러는 `@CurrentUser AuthenticatedUser user`를 받는다.

```java
public ApiResponse<MyResponse> me(@CurrentUser AuthenticatedUser user) {
    return ApiResponse.success(service.getMyPage(user.memberId()));
}
```

- `@CurrentUser`는 인증 필수 API에서 사용한다.
- `APP_SECURITY_PERMIT_ALL=true` 또는 공개 API에서는 `user`가 `null`일 수 있다.
- 컨트롤러에서 JWT를 직접 파싱하지 않는다.
- 테스트에서 로그인 사용자가 필요하면 `@WithMockCustomUser`를 사용한다.

## DB와 영속성

- 프로필, DB URL, DDL 전략의 상세 기준은 [프로필과 환경변수 가이드](../operations/profile-env-guide.md)를 따른다.
- 해커톤 초반에는 `DB_DDL_AUTO=create`로 스키마와 데이터를 초기화할 수 있다.
- 데이터를 보존해야 하는 순간 `DB_DDL_AUTO=update`로 바꾸고, 제출 전에는 `DB_DDL_AUTO=validate`로 스키마 불일치를 확인한다.
- 로컬 테스트 계정은 `src/main/resources/db/seed/local-test-account.sql`에서 관리한다.
- 공유 테스트 서버나 운영 시드는 `DB_SQL_INIT_MODE`와 `DB_SQL_INIT_DATA_LOCATIONS`로 명시적으로 켠다. 기본은 `never`다.
- MySQL 문자셋은 `utf8mb4`를 기준으로 한다.
- 해커톤 초기에는 복잡한 외래 키보다 명확한 서비스 검증을 우선한다. 단, 주제가 확정되고 데이터 정합성이 중요하면 외래 키를 추가한다.

## 테스트

- 서비스 분기 로직은 단위 테스트로 검증한다.
- 새 공개 API는 MockMvc 컨트롤러 테스트로 응답 형식을 검증한다.
- 인증 사용자가 필요한 컨트롤러 테스트는 `@WithMockCustomUser`로 SecurityContext를 준비한다.
- DB/JPA 동작이 중요하면 통합 테스트 또는 명시적인 수동 검증 기록을 남긴다.
- MySQL이 필요한 통합 테스트는 `IntegrationTestSupport`를 상속한다. 이 테스트는 `@Tag("integration")`으로 분리된다.

권장 순서:

1. 실패 케이스를 테스트로 먼저 적는다.
2. 최소 구현으로 통과시킨다.
3. 전체 테스트를 실행한다.
4. Swagger와 문서를 맞춘다.

문서만 바꿀 때도 API 규칙이 바뀌면 [API 규칙](api-conventions.md), 클라이언트 연동 방식이 바뀌면 [클라이언트 협업 가이드](../collaboration/client-collaboration.md)를 같이 확인한다.

## 의존성 규칙

새 의존성은 아래 질문에 답할 수 있을 때만 추가한다.

- 해커톤 안에 실제로 쓸 기능인가?
- 직접 구현하면 버그 위험이 더 큰가?
- 팀원이 10분 안에 이해할 수 있는가?
- 운영/배포 복잡도를 올리지 않는가?

초기 제외: Redis, QueryDSL, Kafka, Elasticsearch, Kubernetes, Terraform, 복잡한 Gradle 멀티 모듈.
