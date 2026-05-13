# GitHub Conventions

이 문서는 해커톤 동안 GitHub에서 충돌을 줄이고 리뷰 가능한 단위로 일하기 위한 최소 규칙이다.

## Workflow

GitHub Flow를 사용한다.

1. `main`은 항상 실행 가능하고 배포 가능한 상태로 둔다.
2. 모든 작업은 `main`에서 새 브랜치를 만든다.
3. 작업 단위가 보이면 Pull Request를 연다. 빠른 피드백이 필요하면 Draft PR을 먼저 연다.
4. CI가 통과하고 리뷰가 끝나면 squash merge한다.
5. 머지된 브랜치는 삭제한다.

## Branch Naming

브랜치명은 `type/short-description` 형식을 쓴다.

| Type | 용도 | 예시 |
| --- | --- | --- |
| `feat` | 기능 추가 | `feat/auth-login` |
| `fix` | 버그 수정 | `fix/jwt-expiration` |
| `docs` | 문서 변경 | `docs/api-conventions` |
| `chore` | 빌드, 설정, 잡일 | `chore/github-actions` |
| `test` | 테스트 추가/수정 | `test/auth-service` |
| `refactor` | 동작 변경 없는 구조 개선 | `refactor/error-response` |

## Commit Message

Conventional Commits 1.0.0을 따른다.

```text
<type>[optional scope]: <description>
```

예시:

```text
feat(auth): add refresh token rotation
fix(error): include trace id in validation response
docs(api): document pagination contract
chore(ci): run Gradle tests on pull requests
```

자주 쓰는 type:

| Type | 의미 |
| --- | --- |
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서만 변경 |
| `test` | 테스트 추가/수정 |
| `refactor` | 동작 변경 없는 개선 |
| `chore` | 설정, 빌드, 의존성 |

## Pull Request Rule

PR은 작게 만든다. 해커톤 중에는 완벽한 분리보다 빠른 리뷰가 중요하므로, 한 PR은 "한 기능 또는 한 문제"만 다룬다.

PR 본문에는 다음을 남긴다.

- 무엇을 바꿨는지
- API 변경이 있는지
- 웹/모바일 팀이 알아야 할 변경이 있는지
- 어떤 테스트를 통과했는지
- 리뷰어가 집중해서 봐야 할 부분

## Main Branch Protection

GitHub 저장소 설정에서 `main`에 다음 규칙을 권장한다.

- Require a pull request before merging
- Require approvals: 1명
- Require status checks to pass before merging
- Required status check: `test`
- Require conversation resolution before merging
- Require linear history
- Allow squash merging
- Disable force push
- Disable branch deletion

해커톤 중 속도가 너무 느려지면 "승인 1명" 규칙만 잠시 완화하고, required status check는 유지한다.

## Merge Strategy

기본은 squash merge다. 이유는 짧은 시간에 커밋이 지저분해지기 쉽고, 평가자는 최종 히스토리에서 기능 단위를 읽는 편이 더 쉽기 때문이다.

Squash merge 제목도 Conventional Commits 형식으로 정리한다.
