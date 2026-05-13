# Code Review Checklist

해커톤 리뷰는 느리게 막는 절차가 아니라, 장애와 클라이언트 연동 실패를 빨리 잡는 안전장치다.

## Backend Review

- API 경로가 `/api/v1` 아래에 있는가?
- 요청/응답 DTO가 entity를 직접 노출하지 않는가?
- validation이 DTO에 선언되어 있는가?
- 비즈니스 실패가 `BusinessException`과 `ErrorCode`로 표현되는가?
- 예외 응답에 `code`, `message`, `status`, `path`, `traceId`, `timestamp`가 유지되는가?
- 성공 응답이 `ApiResponse<T>` 형식을 유지하는가?
- 트랜잭션 경계가 service에 있는가?
- JPA 연관관계나 fetch 전략이 불필요하게 복잡하지 않은가?
- 마이그레이션이 Flyway SQL로 남아 있는가?
- 보안이 필요한 API가 실수로 `permitAll`에 들어가지 않았는가?

## API Contract Review

- Swagger에서 요청/응답 예시가 이해 가능한가?
- Android/iOS/Web에서 camelCase JSON으로 바로 사용할 수 있는가?
- nullable 필드가 문서나 스키마에서 설명되는가?
- 에러 코드가 새로 생겼다면 문서에 추가되었는가?
- 페이지네이션 응답이면 `PageResponse<T>` 형식을 쓰는가?
- 날짜/시간은 ISO-8601 문자열로 주고받는가?

## Test Review

- 핵심 service 로직에 단위 테스트가 있는가?
- controller 요청/응답 포맷 테스트가 있는가?
- DB/Flyway와 관련된 변경이면 통합 테스트 또는 수동 검증 기록이 있는가?
- 실패 케이스가 최소 1개 이상 포함되어 있는가?

## Practical HackaThon Rule

시간이 부족하면 아래 세 가지만 우선 확인한다.

1. 클라이언트가 호출할 API의 요청/응답이 Swagger와 일치하는가?
2. 실패 응답이 공통 에러 포맷으로 내려가는가?
3. `./gradlew clean test`가 통과하는가?
