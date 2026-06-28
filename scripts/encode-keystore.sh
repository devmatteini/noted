#!/usr/bin/env bash

set -euo pipefail

KEYSTORE_FILE="${1:-$HOME/.local/share/noted/noted-keystore.jks}"

function usage() {
    echo "usage $(basename "$0") [keystore-file]" >&2
    echo >&2
    echo "ARGS:" >&2
    echo "    [keystore-file]    keystore to encode. Defaults to Noted keystore path" >&2
    exit 1
}

if [[ ! -f $KEYSTORE_FILE ]]; then
    echo "Keystore file not found: $KEYSTORE_FILE" >&2
    usage
fi

base64 --wrap=0 "$KEYSTORE_FILE"
echo
