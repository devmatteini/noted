#!/usr/bin/env bash

set -euo pipefail

function usage() {
    echo "usage $(basename "$0") <version>" >&2
    echo >&2
    echo "ARGS:" >&2
    echo "    <version>    release version x.y.z (e.g 1.3.0)" >&2
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
release_message=$(git log -1 --pretty=%B)

echo "version $version"

optional_flags=()
if [[ $version =~ alpha|beta ]]; then
    echo "This is a pre-release"
    optional_flags+=("--prerelease")
fi

gh release create "$version" \
    --title "$version" \
    --notes "$release_message" \
    --repo "$GITHUB_REPOSITORY" \
    "${optional_flags[@]}"
