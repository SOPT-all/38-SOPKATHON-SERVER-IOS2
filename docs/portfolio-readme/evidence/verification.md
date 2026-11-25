# P!NGO! 검증 기록

2026-11-25 KST · 서버 main d4059a3 · iOS main aa9e0e4. 38기 솝커톤 iOS 2팀 프로젝트이며 개인 템플릿과 39기 프로젝트는 검토 대상이 아니다.

## 화면과 서버의 확인 범위

| 흐름 | 실행과 마지막 확인 |
| --- | --- |
| 팀 iOS: 장소 발견·읽기 | [팀 원본](https://github.com/user-attachments/assets/6ffa14c2-3be2-4907-9062-4f3438931ca1)의 한 세션에서 앱 진입 → 지도 핀 → 장소 카드 미리보기 → 전문의 제목과 본문을 확인했다. |
| 팀 iOS: 감정·쓰기 | 원본에는 감정 UI·작성 화면과 입력·지도 복귀가 나온다. 현재 iOS main의 감정 버튼은 클라이언트 화면에서 처리되고 작성 완료 핸들러는 화면을 닫는다. 서버 반영, 새 글 조회, 댓글 쓰기, 실제 위치 검증은 이 영상과 iOS 소스에서 확인하지 못했다. |
| 로컬 서버: 조회·반응 | 실제 Spring/Swagger와 임시 MySQL에서 최신·인기 목록을 읽고 상세를 확인했다. 같은 사용자·이야기로 LIKE 추가 → SAD 전환 → SAD 재요청으로 취소했고, 별도 GET에서 초기 반응수와 내 선택 없음이 복원됐다. Swagger 영상은 해당 UI 흐름을 직접 수행한 결과다. |
| 로컬 서버: 쓰기 | POST /api/v1/spots/stories **201**, 생성 ID **13**, 상세 제목·본문·유형 일치, 최신순 첫 ID **13**. 공백 제목은 **400 COMMON_VALIDATION_FAILED**. 검증용으로 적은 ‘빗소리’ 이야기를 사용했다. |

- 공개 iOS 설정의 호스트에서 읽기 전용 점검을 시도했으나 응답을 확인하지 못했다. 따라서 팀 원본과 격리 로컬 실행을 따로 기록했다. 운영 DB를 접속하거나 바꾸지 않았다.
- 팀 iOS 원본: 332×720, 224프레임 / 22.75초, 평균 약 9.85fps 가변 프레임. 움직임에도 시간 간격 공백이 있어 24–30fps 숫자만 맞추지 않았고 원본 MP4와 스틸로 제공했다.
- iOS 재촬영 시도: SnapKit 5.7.1과 Then 3.0.0 의존성이 해석됐다. 임시 복사본에서 URL을 로컬로, 최소 타깃만 iOS 18.6으로 바꿨다. Xcode의 26.2 시뮬레이터 SDK와 설치된 18.6 런타임 조합이 에셋 카탈로그를 컴파일하지 못해 실행·재촬영은 남았다. 결과 상태를 강제로 넣거나 대신할 앱을 만들지 않았다.
- 로컬 Swagger: Playwright 헤드리스 Chrome 기록과 GIF 모두 **217프레임 / 8.68초 = 25fps**, 760×630. 실제 요청과 스크롤을 촬영했다. [촬영 MP4](api-reaction-source.mp4) · [취소 결과](api-reaction-final.png).

## 로컬 실행 및 검증

서버 main을 별도 복사본으로 만들고 Gradle 9.4.1과 Java 21.0.3으로 `./gradlew bootJar -x test --no-daemon`를 실행했다. Java 소스 변경 없음. Spring Boot 4.0.6, MySQL 8.4.13. DB 서버와 Spring 모두 루프백 바인딩. 운영 데이터 대신 저장소의 `pingo.sql`을 새 로컬 DB에 적용했다.

1. 빈 MySQL 8.4 스키마를 로컬 포트 23387에 준비한다.
2. `DB_URL`을 로컬 스키마로, `DB_USERNAME`과 `DB_PASSWORD`를 해당 DB 전용 값으로 두고, `SERVER_ADDRESS=127.0.0.1`, `SERVER_PORT=18038`, `DB_SQL_INIT_DATA_LOCATIONS=classpath:db/seed/pingo.sql`, `APP_SWAGGER_SERVER_URL=http://127.0.0.1:18038`로 JAR를 실행한다.
3. 스크립트 위치에서 `python3 smoke.py`를 실행한다. 상세 API의 기존 상태를 읽고 쓰기 후 응답과 재조회까지 확인한다. [실행 출력](api-run.jsonl)
4. [Swagger](http://localhost:18038/swagger-ui.html)로 반응 API에서 `storyId=1`, `userId=30`, 감정 LIKE → SAD → SAD를 차례로 보내고 `reactionCounts`와 `myReactionType`을 비교한다.

별도로 `./gradlew test -PexcludeIntegration --no-daemon`은 **41개 중 8개 실패**했다. 일부 MVC 테스트 컨텍스트가 반응 서비스를 불러오면 해당 슬라이스에 `StoryRepository` 빈이 없어 시작하지 못한다. API 런타임 성공과 기존 테스트 스위트 실패를 구분했다. 동시 요청, 실서비스 인증, 위치 검증은 테스트 범위가 아니다.

## 근거와 귀속

- [반응 #7](https://github.com/SOPT-all/38-SOPKATHON-SERVER-IOS2/pull/7), [스토리 #9](https://github.com/SOPT-all/38-SOPKATHON-SERVER-IOS2/pull/9), [시드 #10](https://github.com/SOPT-all/38-SOPKATHON-SERVER-IOS2/pull/10), [iOS 팀 README](https://github.com/SOPT-all/38-SOPKATHON-iOS-iOS2).
- 5월 당시 작업 이력에서 Swagger를 팀이 공용으로 접근하려는 요구, 인증 없이 MVP를 먼저 개발하고 필요한 시점에 전환하려는 제약, 고정된 위치별 이야기와 감정 시드 요구를 확인했다. 최종 구현은 커밋과 서버 계약으로 다시 확인했다. 당시 대화 원문과 비공개 자료는 넣지 않았다.
- 팀영상은 iOS 팀의 시연으로 표시했습니다.

## README 재생용 편집

14.92초 / 373프레임, 요청 본문과 반응별 개수·최종 취소 상태를 연장. 전체는 25fps이며 화면 전환의 원래 속도는 유지했다. 편집으로 추가한 정지는 서비스 지연 시간이 아니다. GIF 아래 10px 여백에 재생 진행 바를 넣었으며 앱 화면은 가리지 않는다.
