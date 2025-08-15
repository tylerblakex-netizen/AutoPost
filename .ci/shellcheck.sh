#!/usr/bin/env bash
set -euo pipefail

for cmd in git shellcheck; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Error: command not found: $cmd" >&2
    exit 1
  fi
done

# Find all shell scripts tracked by git except gradlew and .git internals
set +e
mapfile -t FILES < <(git ls-files '*.sh' ':!:gradlew' ':!:.git/*')
GIT_EC=$?
set -e

# Exit on real git error (not “no files”)
if [[ $GIT_EC -ne 0 && $GIT_EC -ne 1 ]]; then
  echo "git ls-files failed with exit code $GIT_EC"
  exit $GIT_EC
fi

if (( ${#FILES[@]} == 0 )); then
  echo "No shell scripts found. Skipping shellcheck."
  exit 0
fi

echo "Running shellcheck on: ${FILES[*]}"
shellcheck "${FILES[@]}"
