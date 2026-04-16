#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_LOG="$(mktemp /tmp/patrakosh-api.XXXXXX.log)"
WEB_LOG="$(mktemp /tmp/patrakosh-web.XXXXXX.log)"
COOKIE_JAR="$(mktemp /tmp/patrakosh-cookies.XXXXXX.txt)"
SIGNUP_HEADERS="$(mktemp /tmp/patrakosh-signup.XXXXXX.headers)"
SIGNUP_BODY="$(mktemp /tmp/patrakosh-signup.XXXXXX.json)"
API_PID=""
WEB_PID=""

cleanup() {
  if [[ -n "$WEB_PID" ]]; then
    kill "$WEB_PID" >/dev/null 2>&1 || true
    wait "$WEB_PID" >/dev/null 2>&1 || true
  fi
  if [[ -n "$API_PID" ]]; then
    kill "$API_PID" >/dev/null 2>&1 || true
    wait "$API_PID" >/dev/null 2>&1 || true
  fi
}

wait_for_url() {
  local url="$1"
  local attempts="${2:-60}"
  local sleep_seconds="${3:-1}"

  for ((attempt = 1; attempt <= attempts; attempt++)); do
    if curl -k -sS -o /dev/null "$url"; then
      return 0
    fi
    sleep "$sleep_seconds"
  done

  echo "Timed out waiting for $url" >&2
  return 1
}

trap cleanup EXIT

"$REPO_ROOT/scripts/ensure-dev-https-cert.sh"

(
  cd "$REPO_ROOT"
  mvn -Dspring-boot.run.mainClass=com.patrakosh.api.ApiApplication -Dspring-boot.run.profiles=https spring-boot:run
) >"$API_LOG" 2>&1 &
API_PID=$!

(
  cd "$REPO_ROOT/frontend"
  npm run dev
) >"$WEB_LOG" 2>&1 &
WEB_PID=$!

wait_for_url "https://127.0.0.1:8443/api/auth/me"
wait_for_url "https://127.0.0.1:5173/"

curl -k -sS \
  -D "$SIGNUP_HEADERS" \
  -o "$SIGNUP_BODY" \
  -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  --data '{"username":"httpssmoke","email":"httpssmoke@example.com","password":"password123","confirmPassword":"password123"}' \
  https://127.0.0.1:8443/api/auth/signup

grep -q 'Set-Cookie: PATRAKOSH_SESSION=' "$SIGNUP_HEADERS"
grep -q 'Secure' "$SIGNUP_HEADERS"
grep -q 'HttpOnly' "$SIGNUP_HEADERS"

API_ME="$(curl -k -sS -b "$COOKIE_JAR" https://127.0.0.1:8443/api/auth/me)"
WEB_ME="$(curl -k -sS -b "$COOKIE_JAR" https://127.0.0.1:5173/api/auth/me)"

curl -k -sS -o /dev/null -b "$COOKIE_JAR" -X POST https://127.0.0.1:8443/api/auth/logout
LOGOUT_STATUS="$(curl -k -sS -o /dev/null -w '%{http_code}' -b "$COOKIE_JAR" https://127.0.0.1:8443/api/auth/me)"

printf 'API log: %s\n' "$API_LOG"
printf 'Web log: %s\n' "$WEB_LOG"
printf 'Signup headers:\n'
cat "$SIGNUP_HEADERS"
printf '\nSignup body:\n'
cat "$SIGNUP_BODY"
printf '\nAPI /api/auth/me:\n%s\n' "$API_ME"
printf 'Vite proxy /api/auth/me:\n%s\n' "$WEB_ME"
printf 'Post-logout /api/auth/me status: %s\n' "$LOGOUT_STATUS"
