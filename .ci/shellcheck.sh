#!/usr/bin/env bash
set -euo pipefail

# Find all shell scripts tracked by git except gradlew and .git internals
mapfile -t FILES < <(git ls-files '*.sh' ':!:gradlew' ':!:.git/*' || true)

if (( ${#FILES[@]} == 0 )); then
  echo "No shell scripts found. Skipping shellcheck."
  exit 0
fi

echo "Running shellcheck on: ${FILES[*]}"
shellcheck "${FILES[@]}"
