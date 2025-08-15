#!/usr/bin/env bash
set -euo pipefail

# Find all shell scripts tracked by git except gradlew and .git internals
set +e
mapfile -t FILES < <(git ls-files '*.sh' ':!:gradlew' ':!:.git/*')
GIT_LS_FILES_EC=$?
set -e

# Exit if git ls-files had a real error (not exit code 1 for 'no files found')
if [[ $GIT_LS_FILES_EC -ne 0 && $GIT_LS_FILES_EC -ne 1 ]]; then
  exit $GIT_LS_FILES_EC
fi

if (( ${#FILES[@]} == 0 )); then
  echo "No shell scripts found. Skipping shellcheck."
  exit 0
fi

echo "Running shellcheck on: ${FILES[*]}"
shellcheck "${FILES[@]}"
