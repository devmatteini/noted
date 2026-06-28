#!/usr/bin/env bash

set -euo pipefail

function usage() {
    echo "usage $(basename "$0") <version> <assets>..." >&2
    echo >&2
    echo "ARGS:" >&2
    echo "    <version>    release version x.y.z (e.g 1.3.0)" >&2
    echo "    <assets>...  list of assets paths" >&2
    exit 1
}

if [[ -z ${1:-} ]]; then usage; fi
if [[ $1 == *v* ]]; then
    echo "Version format must be: x.y.z (e.g 1.3.0)" >&2
    usage
fi
if ! command -v gh >/dev/null; then
    echo "Missing gh command" >&2
    usage
fi
if [[ -z ${GITHUB_REPOSITORY:-} ]]; then
    echo "Missing GITHUB_REPOSITORY env variable. Format must be owner/repo" >&2
    usage
fi

version="$1"

gh release upload "$version" \
    --repo "$GITHUB_REPOSITORY" \
    "${@:2}" # assets
