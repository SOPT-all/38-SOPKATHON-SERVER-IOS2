# 코드 리뷰 체크리스트

해커톤 리뷰는 느리게 막는 절차가 아니라, 장애와 클라이언트 연동 실패를 빨리 잡는 안전장치다. 리뷰어는 아래 항목 중 변경 범위에 해당하는 것만 집중해서 본다.

## 백엔드 리뷰

- API 경로가 `/api/v1` 아래에 있는가?
- 컨트롤러가 `ApiResponse<T>` 또는 `ResponseEntity<ApiResponse<T>>`를 반환하는가?
- 요청/응답 DTO가 엔티티를 직접 노출하지 않는가?
- 검증이 DTO에 선언되어 있는가?
- 비즈니스 실패가 `BusinessException`과 도메인별 `ErrorCode` enum으로 표현되는가?
- 예외 응답에 `success`, `status`, `code`, `message`, `path`, `traceId`, `timestamp`, `errors`가 유지되는가?
- 성공 응답이 `ApiResponse<T>` 형식을 유지하는가?
- 트랜잭션 경계가 서비스에 있는가?
- 새 패키지가 실제 책임에 맞게 `api`, `application`, `domain`, `dto`, `error`, `repository`로 나뉘었는가?
- Java 코드 스타일이 [백엔드 규칙](backend-conventions.md)의 생성자 주입, record DTO, 엔티티 작성 기준과 맞는가?
- JPA 연관관계나 페치 전략이 불필요하게 복잡하지 않은가?
- 엔티티 변경 후 앱을 띄워 DB 스키마가 정상 반영되는가?
- 제출 전 `DB_DDL_AUTO=validate`로 실행해도 기동되는가?
- 보안이 필요한 API가 실수로 `permitAll`에 들어가지 않았는가?
- 비밀번호, secret, authorization 같은 민감값이 응답이나 로그에 노출되지 않는가?
- 토큰은 로그인/회원가입 같은 발급 응답 외에 노출되지 않는가?

## API 계약 리뷰

- Swagger에서 요청/응답 예시가 이해 가능한가?
- Android/iOS/Web에서 camelCase JSON으로 바로 사용할 수 있는가?
- 널 허용 필드가 문서나 스키마에서 설명되는가?
- 에러 코드가 새로 생겼다면 문서에 추가되었는가?
- 페이지네이션 응답이면 `PageResponse<T>` 형식을 쓰는가?
- 날짜/시간은 ISO-8601 문자열로 주고받는가?
- 인증 필요 여부, 토큰 만료 정책, 로그아웃 방식이 [API 규칙](api-conventions.md)과 맞는가?
- API 변경이 있으면 PR 본문과 클라이언트 공유 채널에 표시되었는가?

## 테스트 리뷰

- 핵심 서비스 로직에 단위 테스트가 있는가?
- 컨트롤러 요청/응답 포맷 테스트가 있는가?
- DB/JPA와 관련된 변경이면 통합 테스트 또는 수동 검증 기록이 있는가?
- 실패 케이스가 최소 1개 이상 포함되어 있는가?
- 문서만 바꾼 PR이라도 링크와 경로가 깨지지 않는가?

## 문서 리뷰

- 팀 공유 내용은 `docs/`에 있고, 개인/AI 작업 맥락은 `docs-local/`에 남아 있는가?
- 허브 문서가 긴 규칙을 반복하지 않고 상세 문서로 연결하는가?
- API/백엔드/리뷰 규칙 변경은 `docs/standards/*`에 반영되었는가?
- 클라이언트 연동 절차 변경은 `docs/collaboration/client-collaboration.md`에 반영되었는가?
- GitHub 작업 규칙 변경은 `docs/collaboration/github-conventions.md`에 반영되었는가?
- 리프레시 토큰 제거 이후 존재하지 않는 `auth/domain`, `auth/repository` 같은 패키지를 현재 구조처럼 설명하지 않는가?

## 해커톤 실전 기준

시간이 부족하면 아래 세 가지만 우선 확인한다.

1. 클라이언트가 호출할 API의 요청/응답이 Swagger와 일치하는가?
2. 실패 응답이 공통 에러 포맷으로 내려가는가?
3. 최소한 `./gradlew bootJar -x test`가 통과하고, 배포 후 `/actuator/health`가 `UP`인가?

테스트 코드는 품질 신호로 계속 중요하다. 다만 시연 직전 장애 대응처럼 시간이 극단적으로 부족한 경우에만, CD 게이트에서는 실제 빌드와 운영 환경 기동 성공을 우선하고 테스트 실패 원인을 PR이나 팀 채널에 남긴다.

문서만 바꾼 PR은 `./gradlew bootJar -x test`까지 매번 요구하지 않는다. 단, 문서가 실제 명령이나 API 형식을 바꿔 설명한다면 담당자가 해당 명령을 직접 확인하거나 수동 검증 공백을 적는다.
