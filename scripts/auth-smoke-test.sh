#!/usr/bin/env bash
set -euo pipefail

# 실행 중인 서버에 실제 HTTP 요청을 보내 인증 흐름을 확인하는 스모크 테스트다.
# 단위 테스트가 "코드 조각"을 검증한다면, 이 스크립트는 "떠 있는 서버"가
# health -> login -> token -> protected API 흐름을 실제로 처리하는지 확인한다.
#
# 기본 실행:
#   ./scripts/auth-smoke-test.sh
#
# 원격 서버 확인:
#   BASE_URL=http://<EC2-IP>:8080 ./scripts/auth-smoke-test.sh
#
# 로그인 계정 변경:
#   AUTH_SMOKE_EMAIL=user@example.com AUTH_SMOKE_PASSWORD=password123! ./scripts/auth-smoke-test.sh

BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="${AUTH_SMOKE_EMAIL:-test@sopt.org}"
PASSWORD="${AUTH_SMOKE_PASSWORD:-password123!}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

fail() {
  echo "[auth-smoke] 실패: $1" >&2
  exit 1
}

print_response_body() {
  local name="$1"
  local file="$2"

  if [[ -s "$file" ]]; then
    echo "[auth-smoke] $name 응답 본문:" >&2
    sed -n '1,80p' "$file" >&2
  else
    echo "[auth-smoke] $name 응답 본문이 비어 있습니다." >&2
  fi
}

explain_http_status() {
  local status="$1"
  local context="$2"

  case "$status" in
    000)
      echo "[auth-smoke] 원인 힌트: HTTP 응답을 받지 못했습니다. 서버 실행 여부, BASE_URL, 포트, EC2 보안 그룹, Caddy/리버스 프록시 설정을 확인하세요." >&2
      ;;
    400)
      echo "[auth-smoke] 원인 힌트: 요청 형식이 서버 검증을 통과하지 못했습니다. 요청 JSON, 필수 필드, 이메일/비밀번호 형식을 확인하세요." >&2
      ;;
    401)
      echo "[auth-smoke] 원인 힌트: 인증에 실패했습니다. 계정/비밀번호, seed 실행 여부, Authorization 헤더, APP_JWT_SECRET 설정을 확인하세요." >&2
      ;;
    403)
      echo "[auth-smoke] 원인 힌트: 로그인은 되었지만 권한이 부족할 수 있습니다. 테스트 계정의 role/권한 정책을 확인하세요." >&2
      ;;
    404)
      echo "[auth-smoke] 원인 힌트: API 경로가 없거나 오래된 이미지가 떠 있을 수 있습니다. $context 경로, /api/v1 prefix, 최신 배포 여부를 확인하세요." >&2
      ;;
    409)
      echo "[auth-smoke] 원인 힌트: 데이터 중복/상태 충돌 가능성이 있습니다. 테스트 계정이나 seed 데이터 상태를 확인하세요." >&2
      ;;
    5??)
      echo "[auth-smoke] 원인 힌트: 서버 내부 오류입니다. EC2 애플리케이션 로그, DB 접속 정보, APP_JWT_SECRET, ddl-auto/sql-init 설정을 확인하세요." >&2
      ;;
    *)
      echo "[auth-smoke] 원인 힌트: 예상하지 못한 HTTP 상태입니다. $context 응답 본문과 서버 로그를 함께 확인하세요." >&2
      ;;
  esac
}

explain_curl_error() {
  local exit_code="$1"
  local method="$2"
  local url="$3"

  case "$exit_code" in
    6)
      echo "[auth-smoke] 원인 힌트: 호스트 이름을 찾지 못했습니다. BASE_URL 도메인/IP 오타와 DNS 설정을 확인하세요." >&2
      ;;
    7)
      echo "[auth-smoke] 원인 힌트: 서버에 연결하지 못했습니다. 애플리케이션 실행 여부, 포트, EC2 보안 그룹, 방화벽을 확인하세요." >&2
      ;;
    28)
      echo "[auth-smoke] 원인 힌트: 요청 시간이 초과되었습니다. 서버 부팅 중인지, DB 연결 대기 중인지, 네트워크 접근이 막혔는지 확인하세요." >&2
      ;;
    35 | 60)
      echo "[auth-smoke] 원인 힌트: HTTPS/TLS 연결 문제입니다. 인증서, 도메인, Caddy HTTPS 설정을 확인하세요." >&2
      ;;
    *)
      echo "[auth-smoke] 원인 힌트: curl 종료 코드=$exit_code 입니다. 네트워크, URL, 프록시, 서버 로그를 확인하세요." >&2
      ;;
  esac

  fail "HTTP 요청 자체가 실패했습니다. 요청=$method $url"
}

require_status() {
  local name="$1"
  local expected="$2"
  local actual="$3"

  if [[ "$actual" != "$expected" ]]; then
    explain_http_status "$actual" "$name"
    fail "$name HTTP 상태가 예상과 다릅니다. 예상=$expected 실제=$actual"
  fi
}

extract_json_string() {
  local key="$1"
  sed -n "s/.*\"$key\":\"\\([^\"]*\\)\".*/\\1/p"
}

curl_status_to_file() {
  local method="$1"
  local url="$2"
  local output_file="$3"
  local status
  local curl_exit
  shift 3

  # curl이 네트워크 오류로 실패하면 set -e 때문에 바로 종료될 수 있으므로,
  # 호출부가 원인을 알 수 있게 여기서 curl 종료 코드를 한글 힌트로 바꾼다.
  # connect/max time을 두어 Actions에서 네트워크 문제로 오래 멈추는 상황도 피한다.
  set +e
  status="$(curl -sS --connect-timeout 5 --max-time 20 -X "$method" -o "$output_file" -w "%{http_code}" "$@" "$url")"
  curl_exit=$?
  set -e

  if [[ "$curl_exit" != "0" ]]; then
    explain_curl_error "$curl_exit" "$method" "$url"
  fi

  printf "%s" "$status"
}

echo "[auth-smoke] 인증 스모크 테스트 시작"
echo "[auth-smoke] 대상 서버: $BASE_URL"
echo "[auth-smoke] 로그인 계정: $EMAIL"

health_body="$TMP_DIR/health.json"
protected_body="$TMP_DIR/protected.json"
login_body_file="$TMP_DIR/login-request.json"
login_response_file="$TMP_DIR/login-response.json"
authorized_body="$TMP_DIR/authorized.json"

echo "[auth-smoke] 1/4 health check 확인: GET /actuator/health"
health_status="$(curl_status_to_file "GET" "$BASE_URL/actuator/health" "$health_body")"
require_status "health check" "200" "$health_status"

echo "[auth-smoke] 2/4 보호 API의 현재 인증 모드 확인: GET /api/v1/protected"
protected_status="$(curl_status_to_file "GET" "$BASE_URL/api/v1/protected" "$protected_body")"
if [[ "$protected_status" == "200" ]]; then
  echo "[auth-smoke] 현재 서버는 permit-all 모드입니다. 보호 API가 토큰 없이 열려 있습니다."
elif [[ "$protected_status" == "401" ]]; then
  echo "[auth-smoke] 현재 서버는 인증 모드입니다. 보호 API가 토큰 없이 401을 반환했습니다."
else
  print_response_body "보호 API" "$protected_body"
  explain_http_status "$protected_status" "보호 API"
  fail "보호 API는 토큰 없이 200 또는 401이어야 합니다. 실제 HTTP 상태=$protected_status"
fi

cat > "$login_body_file" <<JSON
{"email":"$EMAIL","password":"$PASSWORD"}
JSON

echo "[auth-smoke] 3/4 로그인 확인: POST /api/v1/auth/login"
login_status="$(curl_status_to_file "POST" "$BASE_URL/api/v1/auth/login" "$login_response_file" \
  -H "Content-Type: application/json" \
  -d @"$login_body_file")"

if [[ "$login_status" != "200" ]]; then
  print_response_body "로그인" "$login_response_file"
  explain_http_status "$login_status" "로그인"
  fail "로그인 HTTP 상태가 200이 아닙니다. 실제=$login_status. 계정이 없으면 회원가입을 하거나 Reset DB에서 local-test-account seed를 실행하세요."
fi

login_response="$(cat "$login_response_file")"
access_token="$(printf "%s" "$login_response" | extract_json_string "accessToken")"

if [[ -z "$access_token" ]]; then
  print_response_body "로그인" "$login_response_file"
  echo "[auth-smoke] accessToken을 찾지 못했습니다." >&2
  echo "[auth-smoke] 기본 테스트 계정은 local seed 또는 Reset DB의 local-test-account seed가 실행되어야 존재합니다." >&2
  echo "[auth-smoke] 원격 서버에서는 회원가입 API로 계정을 만들거나 AUTH_SMOKE_EMAIL/AUTH_SMOKE_PASSWORD를 지정하세요." >&2
  fail "로그인 응답에 accessToken이 없습니다."
fi

# 이 템플릿은 해커톤 시연용으로 refresh token을 만들지 않는다.
# 로그아웃은 서버 API가 아니라 클라이언트가 저장한 access token을 삭제하는 방식이다.
if printf "%s" "$login_response" | grep -q '"refreshToken"'; then
  print_response_body "로그인" "$login_response_file"
  fail "로그인 응답에 refreshToken이 포함되어 있습니다. 이 템플릿은 access-token-only 구조여야 합니다."
fi

echo "[auth-smoke] 4/4 accessToken으로 보호 API 호출 확인"
authorized_status="$(curl_status_to_file "GET" "$BASE_URL/api/v1/protected" "$authorized_body" \
  -H "Authorization: Bearer $access_token" \
)"

if [[ "$protected_status" == "401" && "$authorized_status" == "401" ]]; then
  print_response_body "보호 API 인증" "$authorized_body"
  explain_http_status "$authorized_status" "보호 API 인증"
  fail "유효한 accessToken을 붙였는데도 보호 API가 401을 반환했습니다. APP_JWT_SECRET, 토큰 issuer, Authorization 헤더 처리를 확인하세요."
fi

if [[ "$authorized_status" != "200" ]]; then
  print_response_body "보호 API 인증" "$authorized_body"
  explain_http_status "$authorized_status" "보호 API 인증"
  fail "accessToken을 붙인 보호 API 호출이 성공하지 않았습니다. HTTP 상태=$authorized_status"
fi

echo "[auth-smoke] 성공: health, login, accessToken 발급, 보호 API 호출 흐름이 모두 확인되었습니다."
