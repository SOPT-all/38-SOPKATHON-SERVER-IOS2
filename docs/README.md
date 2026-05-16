# 문서 포털

SOPKATHON 백엔드 템플릿의 팀 공유 문서 입구다. 해커톤 중 바로 필요한 실행, 개발, 협업, 운영 문서만 `docs/`에 둔다.

해커톤 당일에는 먼저 [해커톤 진행 가이드](HACKATHON_GUIDE.md)를 본다. 이 문서가 진행 순서별로 필요한 상세 문서를 연결한다.

## 어디서 시작할까

| 상황 | 먼저 볼 문서 | 이어서 볼 문서 |
| --- | --- | --- |
| 해커톤 당일 전체 흐름을 본다 | [해커톤 진행 가이드](HACKATHON_GUIDE.md) | 필요한 문서만 단계별로 이동 |
| 현재 설정이 뭐가 있는지 본다 | [인프라 구조](operations/infra.md) | [프로필과 환경변수 가이드](operations/profile-env-guide.md), [MVP 개발 가이드](guides/mvp-development-guide.md) |
| 프로젝트를 처음 실행한다 | [../README.md](../README.md) | [학생 개발 안내서](guides/student-development-handbook.md) |
| 백엔드 API를 추가한다 | [백엔드 규칙](standards/backend-conventions.md) | [API 규칙](standards/api-conventions.md), [인증 개발 가이드](guides/auth-development-guide.md) |
| 프론트엔드에서 API를 연동한다 | [클라이언트 협업 가이드](collaboration/client-collaboration.md) | [API 규칙](standards/api-conventions.md) |
| 해커톤 중 설정을 바꾼다 | [MVP 개발 가이드](guides/mvp-development-guide.md) | [프로필과 환경변수 가이드](operations/profile-env-guide.md) |
| EC2/RDS에 배포한다 | [배포 준비 가이드](runbooks/deploy.md) | [인프라 구조](operations/infra.md), [GitHub Actions 설정값](operations/github-actions-settings.md) |
| PR을 리뷰한다 | [코드 리뷰 체크리스트](standards/code-review.md) | [아키텍처 개요](architecture/overview.md) |

## 설정 빠른 지도

| 알고 싶은 것 | 문서 |
| --- | --- |
| 로컬/원격 실행 구조와 제공 파일 | [인프라 구조](operations/infra.md) |
| 프로필, `.env`, Variables/Secrets 기준 | [프로필과 환경변수 가이드](operations/profile-env-guide.md) |
| DB 초기화, 시드, 인증 허용, CORS 전환 시점 | [MVP 개발 가이드](guides/mvp-development-guide.md) |
| EC2/RDS 배포 준비와 실행 절차 | [배포 준비 가이드](runbooks/deploy.md) |
| GitHub Actions에 넣을 값 | [GitHub Actions 설정값](operations/github-actions-settings.md) |

## 분류

| 디렉터리 | 책임 |
| --- | --- |
| `guides/` | 처음 개발, 인증, MVP 설정처럼 작업 흐름을 설명한다. |
| `standards/` | API, 백엔드 코드, 리뷰처럼 지켜야 하는 규칙을 둔다. |
| `collaboration/` | 클라이언트 협업과 GitHub 작업 규칙을 둔다. |
| `operations/` | 로컬/원격 인프라, 프로필/환경변수, 무시 규칙 기준을 둔다. |
| `runbooks/` | 실제 명령 순서가 중요한 운영 절차를 둔다. |
| `architecture/` | 기술 선택과 구조 판단을 설명한다. |

## 관리 원칙

- 같은 설명은 한 문서에만 자세히 쓰고, 다른 문서에서는 링크한다.
- `README.md`는 빠른 실행, `docs/README.md`는 문서 길찾기, `HACKATHON_GUIDE.md`는 당일 진행 순서만 담당한다.
- 팀원이 같이 봐야 하는 실행/API/인증/배포/협업 문서만 `docs/`에 둔다.
- 개인 리서치, 에이전트 작업 흐름, 내부 계획 문서는 Git에 올리지 않는 `docs-local/`에 둔다.
