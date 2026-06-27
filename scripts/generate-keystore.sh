#!/usr/bin/env bash

set -euo pipefail

KEYSTORE_DIR="$HOME/.local/share/noted"
KEYSTORE_FILE="$KEYSTORE_DIR/noted-keystore.jks"
KEY_ALIAS="noted"
_100_YEARS=36500

if ! command -v keytool >/dev/null 2>&1; then
    echo "keytool not found. Install a JDK first." >&2
    exit 1
fi

if [[ -e "$KEYSTORE_FILE" ]]; then
    echo "Keystore already exists at $KEYSTORE_FILE" >&2
    echo "Refusing to overwrite it, delete manually if you need to generate a new keystore" >&2
    exit 1
fi

mkdir -p "$KEYSTORE_DIR"
chmod 700 "$KEYSTORE_DIR"

keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 4096 \
    -validity "$_100_YEARS" \
    -dname "CN=Noted"

chmod 600 "$KEYSTORE_FILE"

echo "Created keystore at $KEYSTORE_FILE with alias $KEY_ALIAS"
