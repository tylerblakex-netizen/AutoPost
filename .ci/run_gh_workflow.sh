#!/usr/bin/env bash
set -Eeuo pipefail

if ! command -v git >/dev/null 2>&1; then
  echo "⚠️  git CLI not found. Please install git." >&2
  exit 1
fi

REPO_SLUG="${GITHUB_REPOSITORY:-tylerblakex-netizen/AutoPost}"
WORKFLOW_FILE="${1:-ci.yml}"

# Determine current branch (works locally and in CI)
branch_name="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")"
if [[ -z "${branch_name}" || "${branch_name}" == "HEAD" ]]; then
  branch_name="${GITHUB_HEAD_REF:-${GITHUB_REF_NAME:-}}"
fi

if [[ -z "${branch_name}" ]]; then
  echo "⚠️  Could not determine branch name (detached HEAD?). Skipping workflow trigger."
  exit 0
fi

echo "🔎 Using branch: ${branch_name}"

# Ensure GH CLI present (non-fatal if missing)
if ! command -v gh >/dev/null 2>&1; then
  echo "⚠️  gh CLI not found. Install and auth to trigger workflows, or run manually:"
  echo "    gh workflow run ${WORKFLOW_FILE} --repo ${REPO_SLUG} --ref ${branch_name}"
  exit 0
fi

# Make sure we're authenticated
if ! gh auth status >/dev/null 2>&1; then
  echo "⚠️  gh not authenticated. Run: gh auth login"
  exit 0
fi

# If the branch doesn't exist on origin, push it first
if ! git ls-remote --exit-code --heads origin "${branch_name}" >/dev/null 2>&1; then
  echo "⬆️  Branch '${branch_name}' not on remote. Pushing…"
  git push -u origin "${branch_name}"
fi

# Trigger the workflow for the current branch
set +e
gh workflow run "${WORKFLOW_FILE}" --repo "${REPO_SLUG}" --ref "${branch_name}"
rc=$?
set -e

if [[ $rc -ne 0 ]]; then
  echo "⚠️  Failed to trigger workflow '${WORKFLOW_FILE}' on '${branch_name}'. Non-fatal; continuing."
  exit 0
fi

echo "✅ Workflow '${WORKFLOW_FILE}' triggered for '${branch_name}'."
