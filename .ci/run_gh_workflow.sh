#!/usr/bin/env bash
set -euo pipefail

WF_PATH="${1:-}"
if [[ -z "${WF_PATH}" ]]; then
  echo "Usage: $0 <workflow-file-path>"
  exit 64
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: GitHub CLI (gh) not found. Install it before running this script."
  exit 127
fi

: "${GITHUB_TOKEN:?GITHUB_TOKEN is required (provided automatically in GitHub Actions)}"

REPO="${GITHUB_REPOSITORY:-$(git config --get remote.origin.url | sed -E 's#.*github.com[:/](.+/.+)(\.git)?#\1#')}"
REF="${GITHUB_REF_NAME:-$(git rev-parse --abbrev-ref HEAD)}"

echo "Dispatching workflow '${WF_PATH}' on ${REPO}@${REF}"
gh auth status || gh auth login --with-token <<<"${GITHUB_TOKEN}"

gh workflow run "${WF_PATH}" --repo "${REPO}" --ref "${REF}" || {
  echo "WARNING: gh workflow run returned non-zero. This can happen if the workflow is the current one or lacks permissions."
  exit 0
}

echo "Workflow dispatch requested successfully."
