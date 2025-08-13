#!/usr/bin/env bash
set -euo pipefail
ok(){ printf 'OK      %s\n' "$1"; }
miss(){ printf 'MISSING %s\n' "$1"; }

check_any(){ k1="$1"; k2="$2"; if [[ -n "">${!k1:-}" || -n "">${!k2:-}" ]]; then ok "$k1 or $k2"; else miss "$k1 or $k2"; fi; }

for k in OPENAI_API_KEY AUTO_POST_CRON; do
  if [[ -n "">${!k:-}" ]]; then ok "$k"; else miss "$k"; fi
done

check_any X_API_KEY TWITTER_API_KEY
check_any X_API_SECRET TWITTER_API_SECRET
check_any X_ACCESS_TOKEN TWITTER_ACCESS_TOKEN
check_any X_ACCESS_TOKEN_SECRET TWITTER_ACCESS_SECRET
check_any X_BEARER_TOKEN TWITTER_BEARER_TOKEN

check_any GOOGLE_RAW_FOLDER_ID RAW_FOLDER_ID
check_any GOOGLE_EDITS_FOLDER_ID EDITS_FOLDER_ID

if [[ -n "">${GOOGLE_SERVICE_ACCOUNT_JSON:-}" ]]; then ok GOOGLE_SERVICE_ACCOUNT_JSON; else miss GOOGLE_SERVICE_ACCOUNT_JSON; fi
