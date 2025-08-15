#!/usr/bin/env bash
set -euo pipefail

if command -v shellcheck >/dev/null 2>&1; then
  echo "Running shellcheck…"
  mapfile -t files < <(git ls-files | grep -E '(\.sh)$' | grep -v -E '^(node_modules/|build/|\.git/)')
  if [ "${#files[@]}" -gt 0 ]; then
    shellcheck -x "${files[@]}"
  else
    echo "No shell scripts found to check."
  fi
else
  echo "WARNING: shellcheck not installed; skipping lint."
fi
