#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CERT_DIR="${PATRAKOSH_CERT_DIR:-$REPO_ROOT/.certs}"
KEYSTORE_PATH="${PATRAKOSH_SSL_KEY_STORE:-$CERT_DIR/patrakosh-dev.p12}"
KEYSTORE_PASSWORD="${PATRAKOSH_SSL_KEY_STORE_PASSWORD:-changeit}"
KEY_ALIAS="${PATRAKOSH_SSL_KEY_ALIAS:-patrakosh-dev}"

mkdir -p "$(dirname "$KEYSTORE_PATH")"

if [[ -f "$KEYSTORE_PATH" ]]; then
  echo "Using existing dev TLS certificate at $KEYSTORE_PATH"
  exit 0
fi

keytool -genkeypair \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$KEYSTORE_PASSWORD" \
  -keypass "$KEYSTORE_PASSWORD" \
  -validity 3650 \
  -dname "CN=localhost, OU=Development, O=PatraKosh, L=Local, ST=Local, C=IN" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"

echo "Generated dev TLS certificate at $KEYSTORE_PATH"
