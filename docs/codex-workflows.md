# Codex Workflows

이 문서는 Codex를 해커톤 백엔드 개발에 어떻게 쓸지 정리한다. 지금은 백엔드 템플릿 완성도가 우선이므로, 복잡한 harness/skill/plugin 제작은 후순위로 두고 바로 효과가 있는 문서 기반 컨텍스트부터 적용한다.

## Research Summary

OpenAI Codex 공식 문서 기준으로 핵심은 다음과 같다.

- Codex에는 작업마다 goal, context, constraints, done criteria를 주는 것이 좋다.
- 반복되는 규칙은 긴 프롬프트에 계속 넣기보다 `AGENTS.md`에 둔다.
- `AGENTS.md`는 repo layout, 실행 방법, 테스트 명령, 컨벤션, 금지 규칙, 완료 기준을 담는 데 적합하다.
- 복잡한 작업은 먼저 계획을 만들고 실행한다.
- 코드 변경 후에는 테스트, 확인, 리뷰까지 시킨다.
- 반복되는 워크플로우는 나중에 skill로 만든다.
- 외부 시스템의 최신 context가 필요할 때 MCP를 쓴다.
- subagent는 탐색, 테스트, triage처럼 경계가 분명한 보조 작업에 적합하다.
- automation은 수동으로 안정화된 반복 작업을 예약할 때 쓴다.

참고:

- [OpenAI Codex best practices](https://developers.openai.com/codex/learn/best-practices)
- [OpenAI Codex AGENTS.md guide](https://developers.openai.com/codex/guides/agents-md)

## Current Setup

현재 적용한 것은 "가볍지만 바로 효과가 있는 harness"다.

- `AGENTS.md`: Codex가 자동으로 읽는 repo-level instructions
- `docs/code-review.md`: Codex `/review`나 수동 리뷰 기준
- `docs/backend-conventions.md`: 백엔드 코드 생성/수정 기준
- `docs/api-conventions.md`: API 계약 기준
- `scripts/verify.sh`: 반복 검증 명령

## Default Prompt Template

새 작업을 시킬 때는 아래 구조로 지시한다.

```text
Goal:
- 무엇을 구현/수정할지

Context:
- 관련 파일, API, 에러 로그, 문서

Constraints:
- 지켜야 할 컨벤션
- 건드리면 안 되는 범위

Done when:
- 어떤 테스트가 통과해야 하는지
- 어떤 문서를 갱신해야 하는지
- 어떤 수동 확인이 필요한지
```

## Recommended Work Units

해커톤 중 Codex에게 작업을 줄 때는 큰 기능을 바로 주지 말고 아래 단위로 나눈다.

1. API 계약 설계
2. DTO와 validation 작성
3. service 테스트 작성
4. service 구현
5. controller 테스트 작성
6. controller 구현
7. Swagger 확인
8. 문서 갱신
9. 전체 테스트 실행

## Useful Prompts

API 설계:

```text
이 기능의 API 계약을 먼저 설계해줘. docs/api-conventions.md를 따르고, 요청/응답/에러 코드/Swagger 설명까지 제안해줘. 아직 코드는 수정하지 말고 설계만 해줘.
```

구현:

```text
방금 설계한 API를 구현해줘. service 테스트를 먼저 만들고, 통과시키는 최소 구현을 한 뒤 controller 테스트까지 추가해줘. 완료 전에 ./gradlew test를 실행해줘.
```

리뷰:

```text
현재 변경사항을 docs/code-review.md 기준으로 리뷰해줘. 버그, API 계약 깨짐, 테스트 누락을 우선순위로 봐줘.
```

디버깅:

```text
이 실패 로그를 보고 바로 고치지 말고 원인을 먼저 좁혀줘. 재현 방법, 의심 지점, 최소 수정안을 정리한 뒤 수정해줘.
```

문서화:

```text
이번 변경사항을 웹/모바일 팀원이 이해할 수 있게 docs/client-collaboration.md와 Swagger 기준으로 정리해줘.
```

## Subagent Strategy

지금은 기본 개발 흐름에 subagent를 필수로 쓰지 않는다. 작업 규모가 작고 파일 충돌 비용이 있기 때문이다.

쓸 만한 경우:

- 다른 파일을 읽기만 하는 조사 작업
- 테스트 실패 로그 분석
- 문서 초안 작성
- 기존 코드 패턴 탐색

피해야 할 경우:

- 같은 service/controller 파일을 여러 agent가 동시에 수정
- DB migration 번호가 충돌할 수 있는 작업
- 핵심 설계 결정을 agent마다 따로 내리는 작업

## Future Skills

해커톤 전에 시간이 남으면 다음 skill을 만든다.

| Skill | 목적 | Trigger |
| --- | --- | --- |
| `spring-api-contract` | API 계약, DTO, Swagger, controller test 생성 | "API 설계", "엔드포인트 추가" |
| `spring-error-review` | `ErrorCode`, exception handler, error docs 점검 | "에러 코드 추가", "예외 처리 검토" |
| `HackaThon-deploy-runbook` | EC2/RDS/Docker Compose 배포 점검 | "배포 준비", "서버 점검" |
| `client-sync-note` | 웹/모바일 공유용 변경 요약 작성 | "클라이언트 공유", "API 변경 요약" |

처음부터 plugin까지 만들지 않는다. 한두 번 수동 prompt로 검증한 뒤, 반복 가치가 확인되면 skill로 승격한다.

## Automation Candidates

자동화는 수동 워크플로우가 안정화된 뒤에 만든다.

- 매일 아침 `main` 기준 테스트 실행과 실패 요약
- PR 머지 전 API 변경 문서 누락 점검
- 배포 후 `/actuator/health`와 Swagger URL 확인
- 해커톤 종료 전 최종 보고서 초안 생성

## Operating Rule

Codex에게 맡겨도 책임은 개발자에게 있다. 특히 인증, DB migration, API 응답 형식, 배포 설정은 테스트와 문서로 검증한 뒤 팀에 공유한다.
