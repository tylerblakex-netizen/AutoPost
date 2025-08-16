#!/usr/bin/env bash
set -euo pipefail

trim() {
  local s="${1:-}"
  s="${s#"${s%%[![:space:]]*}"}"
  s="${s%"${s##*[![:space:]]}"}"
  printf '%s' "$s"
}

readarray -td, REQUIRED_ARR < <(printf '%s' "${REQUIRED_SECRETS:-}" && printf ',')
readarray -td, OPTIONAL_ARR < <(printf '%s' "${OPTIONAL_SECRETS:-}" && printf ',')

missing_required=()
missing_optional=()
present_required=()
present_optional=()

for key in "${REQUIRED_ARR[@]}"; do
  key="$(trim "$key")"
  [[ -z "$key" ]] && continue
  val="${!key-}"
  if [[ -z "${val:-}" ]]; then
    missing_required+=("$key")
  else
    present_required+=("$key")
  fi
done

for key in "${OPTIONAL_ARR[@]}"; do
  key="$(trim "$key")"
  [[ -z "$key" ]] && continue
  val="${!key-}"
  if [[ -z "${val:-}" ]]; then
    missing_optional+=("$key")
  else
    present_optional+=("$key")
  fi
done

{
  echo "### Environment secrets check"
  echo
  if ((${#present_required[@]})); then
    printf -- "- Present (required): %s\n" "$(IFS=', '; echo "${present_required[*]}")"
  else
    echo "- Present (required): none"
  fi
  if ((${#missing_required[@]})); then
    printf -- "- Missing (required): %s\n" "$(IFS=', '; echo "${missing_required[*]}")"
  else
    echo "- Missing (required): none"
  fi
  if ((${#present_optional[@]})); then
    printf -- "- Present (optional): %s\n" "$(IFS=', '; echo "${present_optional[*]}")"
  else
    echo "- Present (optional): none"
  fi
  if ((${#missing_optional[@]})); then
    printf -- "- Missing (optional): %s\n" "$(IFS=', '; echo "${missing_optional[*]}")"
  else
    echo "- Missing (optional): none"
  fi
} >> "$GITHUB_STEP_SUMMARY"

if ((${#missing_required[@]})); then
  echo "Missing required secrets: ${missing_required[*]}"
  exit 1
fi

echo "All required secrets are present."
