#!/usr/bin/env bash
set -euo pipefail

# 팀 최종 확인용 전체 테스트다.
# clean test를 실행하므로 빠른 개발 확인보다 오래 걸릴 수 있다.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[verify] clean test 전체 검증을 실행합니다."
./gradlew clean test
