#!/usr/bin/env bash
set -euo pipefail

echo "🧹 Part 2/2: cleaning workflows + verifying + pushing..."

# --- 1) Purge SERVICE_* from all workflows ---
if [ -d .github/workflows ]; then
  MATCHED="$(grep -rlE 'SERVICE_(PUBLIC_ID|SECRET_KEY)' .github/workflows || true)"
  if [ -n "$MATCHED" ]; then
    echo "$MATCHED" | xargs -r sed -i.bak -e '/SERVICE_PUBLIC_ID/d' -e '/SERVICE_SECRET_KEY/d'
  fi
fi

# --- 2) Sanity: ensure nothing remains (excluding this script) ---
if git grep -nE 'SERVICE_(PUBLIC_ID|SECRET_KEY)' -- . -- ':!purge-service-secrets.sh' >/dev/null 2>&1; then
  echo "❌ Still found SERVICE_* references:"
  git grep -nE 'SERVICE_(PUBLIC_ID|SECRET_KEY)' -- . -- ':!purge-service-secrets.sh'
  exit 1
fi
echo "✅ No SERVICE_* references left."

# --- 3) Build (mvn wrapper if present) ---
if [ -x ./mvnw ]; then
  ./mvnw -q -DskipTests package
else
  mvn -q -DskipTests package
fi

# --- 4) Commit & push ---
git add -A
git commit -m "chore: purge unused SERVICE_* secrets; update Config/docs/scripts/workflows" || echo "Nothing to commit."
git push || echo "⚠️ Push failed (check your remote/permissions)."

echo "🎉 Done. Clean build, no zombie secrets. Ship it."