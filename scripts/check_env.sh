#!/usr/bin/env bash
# scripts/check_env.sh
# Validates environment variables required for AutoPost.
# Run: bash scripts/check_env.sh

set -euo pipefail

### ────────────────────────────────────────────────────────────────────────────
### Styling / logging
### ────────────────────────────────────────────────────────────────────────────
if [ -t 1 ]; then
  BOLD="$(printf '\033[1m')"; RED="$(printf '\033[31m')"
  GRN="$(printf '\033[32m')"; YLW="$(printf '\033[33m')"; RST="$(printf '\033[0m')"
else
  BOLD=""; RED=""; GRN=""; YLW=""; RST=""
fi

ok() { printf 'OK      %s\n' "$1"; }
miss() { printf 'MISSING %s\n' "$1"; missing=1; }
warn() { printf 'WARNING %s\n' "$1"; }

### ────────────────────────────────────────────────────────────────────────────
### Environment validation
### ────────────────────────────────────────────────────────────────────────────
missing=0

# Check if either of two variables is set
check_any() {
  local k1="$1"
  local k2="$2"
  local desc="$3"

  if [[ -n "${!k1:-}" || -n "${!k2:-}" ]]; then
    ok "$desc"
  else
    miss "$desc ($k1 or $k2)"
  fi
}

# Check single required variable
check_single() {
  local k="$1"
  local desc="$2"

  if [[ -n "${!k:-}" ]]; then
    ok "$desc"
  else
    miss "$desc ($k)"
  fi
}

# Check optional variable with warning if missing
check_optional() {
  local k="$1"
  local desc="$2"

  if [[ -n "${!k:-}" ]]; then
    ok "$desc"
  else
    warn "$desc ($k) - optional but recommended"
  fi
}

printf "${BOLD}AutoPost Environment Check${RST}\n"
printf "─────────────────────────────────────\n"

# Core required variables
check_single "OPENAI_API_KEY" "OpenAI API access"

# Google Drive folder IDs (accept either naming convention)
check_any "RAW_FOLDER_ID" "GOOGLE_RAW_FOLDER_ID" "Raw video folder ID"
check_any "EDITS_FOLDER_ID" "GOOGLE_EDITS_FOLDER_ID" "Processed video folder ID"

# Google service account (either path or inline JSON)
check_any "GOOGLE_APPLICATION_CREDENTIALS" "GOOGLE_SERVICE_ACCOUNT_JSON" "Google service account credentials"

# Twitter/X API credentials - all or none
twitter_vars=("TWITTER_API_KEY" "TWITTER_API_SECRET" "TWITTER_ACCESS_TOKEN" "TWITTER_ACCESS_SECRET")
twitter_count=0
for var in "${twitter_vars[@]}"; do
  if [[ -n "${!var:-}" ]]; then
    twitter_count=$((twitter_count + 1))
  fi
done

if [[ $twitter_count -eq 4 ]]; then
  ok "Twitter/X API credentials (all 4 variables)"
elif [[ $twitter_count -eq 0 ]]; then
  warn "Twitter/X API credentials - not configured (posts will use webhook if available)"
else
  miss "Twitter/X API credentials (have $twitter_count/4 variables: ${twitter_vars[*]})"
fi

# Optional but commonly used variables
check_optional "AUTO_POST_CRON" "Posting schedule"
check_optional "OPENAI_MODEL" "OpenAI model selection"
check_optional "WEBHOOK_URL" "Alternative posting webhook"

printf "─────────────────────────────────────\n"

if [[ $missing -eq 1 ]]; then
  printf "${RED}✖${RST} Environment check failed - see missing variables above\n"
  exit 1
else
  printf "${GRN}✔${RST} Environment check passed\n"
fi