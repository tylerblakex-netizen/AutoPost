#!/usr/bin/env bash
set -euo pipefail
if ! command -v shellcheck >/dev/null 2>&1; then
  sudo apt-get update -y
  sudo apt-get install -y shellcheck
fi
shopt -s globstar nullglob
files=(**/*.sh)
if [ ${#files[@]} -eq 0 ]; then
  echo "No shell scripts to lint."
  exit 0
fi
shellcheck "${files[@]}"
echo "ShellCheck passed ✅"
