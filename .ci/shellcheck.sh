#!/usr/bin/env bash
set -Eeuo pipefail

# Lint all repo shell scripts but ignore known noise:
# - SC1091: sourcing non-constant file
# - SC2034: assigned but unused (common in env templates)
# - SC2155: declare and assign
DISABLES="SC1091,SC2034,SC2155"

# Find scripts (skip hidden, node_modules, gradle caches, build artifacts)
MAPFILE=()
while IFS= read -r -d '' f; do MAPFILE+=("$f"); done < <(
  git ls-files | grep -E '\.sh$' | grep -vE '(^\.|/\.|node_modules/|\.gradle/|build/)' | tr '\n' '\0'
)

if (("${#MAPFILE[@]}"==0)); then
  echo "No shell scripts found."
  exit 0
fi

echo "ShellCheck on ${#MAPFILE[@]} file(s)"
shellcheck --exclude="$DISABLES" "${MAPFILE[@]}"
