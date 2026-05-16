#!/usr/bin/env bash
set -euo pipefail

# 빠른 개발 확인용 테스트다.
# @Tag("integration") 테스트를 제외하고 돌리므로 DB/Testcontainers가 필요한 테스트보다 빠르다.
# 최종 제출 전에는 ./scripts/verify.sh 로 전체 테스트를 한 번 더 확인한다.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[test-fast] integration 태그를 제외한 빠른 테스트를 실행합니다."
./gradlew test -PexcludeIntegration
