#!/usr/bin/env bash
set -euo pipefail

missing=0

ok(){ printf 'OK      %s\n' "$1"; }
miss(){ printf 'MISSING %s\n' "$1"; missing=1; }

check_any(){
  k1="$1"; k2="$2"
  if [[ -n ""${!k1:-}" || -n ""${!k2:-}" ]]; then
    ok "$k1 or $k2"
  else
    miss "$k1 or $k2"
  fi
}

# Required singles
for k in \
  OPENAI_API_KEY \
  AUTO_POST_CRON \
  TWITTER_API_KEY \
  TWITTER_API_SECRET \
  TWITTER_ACCESS_TOKEN \
  TWITTER_ACCESS_SECRET
 do
  if [[ -n ""${!k:-}" ]]; then ok "$k"; else miss "$k"; fi
 done

# Google Drive folders (at least one per pair)
check_any GOOGLE_RAW_FOLDER_ID RAW_FOLDER_ID
check_any GOOGLE_EDITS_FOLDER_ID EDITS_FOLDER_ID

# Service account JSON (required single)
if [[ -n ""${GOOGLE_SERVICE_ACCOUNT_JSON:-}" ]]; then
  ok GOOGLE_SERVICE_ACCOUNT_JSON
else
  miss GOOGLE_SERVICE_ACCOUNT_JSON
fi

if [[ "$missing" -eq 1 ]]; then
  echo "Environment check failed: one or more required variables are missing."
  exit 1
else
  echo "Environment check passed: all required variables are set."
fi
