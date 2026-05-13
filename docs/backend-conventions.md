# Backend Conventions

이 문서는 백엔드 개발자가 기능을 추가할 때 지킬 기본 규칙이다. 목적은 빠른 개발보다 느린 절차를 만드는 것이 아니라, 짧은 해커톤 중에도 코드가 무너지지 않게 하는 것이다.

## Package Structure

기본 구조는 package-by-feature다.

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
│   ├── domain
│   ├── dto
│   └── repository
├── member
│   ├── domain
│   └── repository
└── example
    ├── api
    └── dto
```

새 기능은 `feature/api`, `feature/application`, `feature/domain`, `feature/dto`, `feature/repository` 형태로 추가한다.

## Layer Rules

| Layer | 책임 | 규칙 |
| --- | --- | --- |
| `api` | HTTP 요청/응답, validation | 비즈니스 로직을 넣지 않는다. |
| `application` | 유스케이스, 트랜잭션 | repository와 domain을 조합한다. |
| `domain` | entity, enum, 도메인 규칙 | HTTP나 Spring MVC 타입을 모른다. |
| `repository` | DB 접근 | Spring Data JPA repository를 둔다. |
| `dto` | 요청/응답 모델 | entity를 직접 노출하지 않는다. |

## Naming

- Controller: `AuthController`
- Service: `AuthService`
- Request DTO: `SignUpRequest`
- Response DTO: `TokenResponse`
- Repository: `MemberRepository`
- Entity: `Member`
- Enum: `Role`
- Test: `AuthServiceTest`, `AuthControllerTest`

## Transaction

- 조회 전용 service method는 `@Transactional(readOnly = true)`를 기본으로 한다.
- 쓰기 method는 method 단위로 `@Transactional`을 붙인다.
- Controller에는 transaction을 두지 않는다.

## DTO and Validation

- Controller request body는 record DTO를 사용한다.
- 필수값은 `@NotBlank`, 범위는 `@Size`, 이메일은 `@Email`을 사용한다.
- validation 메시지는 클라이언트가 그대로 이해할 수 있게 한국어로 작성한다.
- response DTO는 클라이언트 화면에 필요한 값만 담는다.

## Error Handling

비즈니스 실패는 `BusinessException`으로 던진다.

```java
throw new BusinessException(ErrorCode.MEMBER_DUPLICATE_EMAIL);
```

`ErrorCode`에는 다음 정보를 둔다.

- HTTP status
- 내부 error code
- 기본 message
- log level

새 실패 케이스가 생기면 먼저 `ErrorCode`에 추가하고, API 문서에도 반영한다.

## Response

성공 응답은 `ApiResponse<T>`를 사용한다.

```java
return ApiResponse.success(response);
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
```

페이지 응답은 `PageResponse<T>`를 사용한다. Spring `Page<T>`를 그대로 외부에 노출하지 않는다.

## Persistence

- schema는 Flyway migration으로 관리한다.
- `ddl-auto`는 `validate`를 기본으로 한다.
- migration 파일명은 `V{number}__{description}.sql` 형식이다.
- MySQL 문자셋은 `utf8mb4`를 기준으로 한다.
- 해커톤 초기에는 복잡한 foreign key보다 명확한 service 검증을 우선한다. 단, 주제가 확정되고 데이터 정합성이 중요하면 foreign key를 추가한다.

## Test

- service 분기 로직: unit test
- controller 요청/응답 계약: MockMvc test
- DB/Flyway 동작: Testcontainers integration test

권장 순서:

1. 실패 케이스를 테스트로 먼저 적는다.
2. 최소 구현으로 통과시킨다.
3. 전체 테스트를 실행한다.
4. Swagger와 문서를 맞춘다.

## Dependency Rule

새 의존성은 아래 질문에 답할 수 있을 때만 추가한다.

- 해커톤 안에 실제로 쓸 기능인가?
- 직접 구현하면 버그 위험이 더 큰가?
- 팀원이 10분 안에 이해할 수 있는가?
- 운영/배포 복잡도를 올리지 않는가?

초기 제외: Redis, QueryDSL, Kafka, Elasticsearch, Kubernetes, Terraform, 복잡한 Gradle multi-module.
