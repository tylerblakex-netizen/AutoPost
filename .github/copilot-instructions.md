# Copilot Code Review — AutoPost (Codespaces-first)

**How to respond**
- Start with a 1–2 sentence summary, then a **bullet checklist**.
- Give **actionable diffs/code** (no vague vibes).
- If something’s unclear, add a line starting **“Unsure:”** and say why.

---

## Repo context
- Repo: `tylerblakex-netizen/AutoPost`
- Goal: cut 3×20s + 1×180s teaser, generate safe captions/hashtags (≤3), schedule & post to X.
- Stack: Kotlin/Java (primary), Python utils, Bash scripts, GitHub Actions CI.
- Storage: Google Drive (RAW→EDITS).
- Secrets live in **GitHub Codespaces development environment secrets** and/or **repo/org secrets** — **never** committed.

---

## Codespaces secrets policy (Copilot must enforce)

- Secrets are provided to codespaces as **environment variables** after the codespace is running. They are **not available at build time**; if a secret is created while a codespace is running, **stop + restart** to use it.  [oai_citation:0‡GitHub Docs](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-your-account-specific-secrets-for-github-codespaces)
- **Access control:** Account-level secrets must be granted **repository access** to `AutoPost` (you pick repos per secret). Repo/org secrets can be set on the repo/org directly.  [oai_citation:1‡GitHub Docs](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-your-account-specific-secrets-for-github-codespaces)
- **Naming rules:** Alphanumeric + `_`; can’t start with `GITHUB_` or a number; case-insensitive; **lower-level wins** on name conflicts (repo overrides org). Limits: **100 secrets**, **≤48 KB** each.  [oai_citation:2‡GitHub Docs](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-your-account-specific-secrets-for-github-codespaces)
- **Dev-container note:** Secrets **cannot** be used in Dockerfile or dev-container “features”; only at runtime (postStart/lifecycle scripts/terminal).  [oai_citation:3‡GitHub Docs](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-your-account-specific-secrets-for-github-codespaces)

**Expected env vars for this project (names only; values in Secrets):**

OPENAI_API_KEY
ANTHROPIC_API_KEY
GROK_API_KEY

X_BEARER_TOKEN
X_API_KEY
X_API_SECRET
X_ACCESS_TOKEN
X_ACCESS_TOKEN_SECRET

GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
GOOGLE_SERVICE_ACCOUNT_JSON   # paste the full JSON as a single value
GOOGLE_RAW_FOLDER_ID
GOOGLE_EDITS_FOLDER_ID

AUTO_POST_CRON                 # e.g. 0 18 * * *  (UTC)
MAX_HASHTAGS                   # e.g. 3
POST_ONE_TEASER_PER_DAY        # e.g. true

Copilot should **reject** any PR that hardcodes secrets, paths leaking IDs/emails, or assumes build-time access to secrets.

---

## Always review for

1) **Security**
- No keys/tokens/emails/PII in code, logs, tests, or examples.
- Config must come from env/secret manager; **validate on startup** and fail fast with a helpful error.
- OAuth redirect URIs **configurable**, not hardcoded.
- Bash: `set -euo pipefail`, **quote variables**, guard against injection.

2) **Scheduling & Time**
- Use **`AUTO_POST_CRON`** (UTC). No hardcoded 9AMs.
- Parse & test cron; convert to local time only at display.
- Respect **one-teaser-per-day** constraint.

3) **Reliability**
- All external calls (Drive/LLM/X/FFmpeg): **timeouts**, **retry with jittered backoff**, and clear error paths.
- No empty catches; logs must not leak secret values.

4) **Testability**
- Tests for: clip selection, hashtag limit (≤3), daily-teaser gate, cron parsing/TZ, X rate-limit behavior.
- Mock external APIs; deterministic times.

5) **Performance**
- Stream files (no full video in RAM).
- Reuse clients; cap safe concurrency.

6) **Code Quality**
- Prefer **Kotlin**; avoid `!!`; use `Result<T>`/sealed results.
- Python: type hints, Black/Ruff, small pure functions, no global mutable state.
- Bash: POSIX-portable, safe temp files (`mktemp`).

7) **Clarity**
- Public functions: one-line purpose + inputs/outputs/invariants.
- No nested ternaries/clever one-liners. Name things clearly.

8) **Policy Fit**
- Captions must be **algorithm-safe** (no explicit NSFW), emoji allowed, **≤3 hashtags**.
- Collaborator tagging is **config-driven** (no hardcoded handles).
- If X creds missing: **queue but don’t post**, emit a clear warning.

---

## LLM rules
- Prompts live in **versioned templates**; include rails (“non-explicit, max length, ≤3 hashtags”).
- Log only **prompt IDs** + token counts — **never** raw prompts/completions.

---

## Dependency policy
- Pin versions; minimal deps; no `curl | bash`.
- New dep requires purpose, maintenance state, size, security notes.

---

## PR checklist Copilot must output
- [ ] No secrets/identifiers committed (.env/keys ignored by Git).
- [ ] Secrets only consumed via env; **no build-time dependency on secrets**.  [oai_citation:4‡GitHub Docs](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-your-account-specific-secrets-for-github-codespaces)
- [ ] Account secrets have **repo access** to `AutoPost`, or repo/org secrets are set.  [oai_citation:5‡GitHub Docs](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-your-account-specific-secrets-for-github-codespaces)
- [ ] Names follow rules; size/count limits respected.  [oai_citation:6‡GitHub Docs](https://docs.github.com/en/codespaces/managing-codespaces-for-your-organization/managing-development-environment-secrets-for-your-repository-or-organization)
- [ ] Schedule via `AUTO_POST_CRON` (UTC) with tests.
- [ ] External calls: timeouts + retries + clear errors.
- [ ] Enforce **≤3 hashtags** and **one teaser/day** (tested).
- [ ] No hardcoded handles/redirect URIs; all configurable.
- [ ] Streaming I/O; clients reused; concurrency bounded.
- [ ] Sanitized logging; no sensitive data.
- [ ] LLM prompts templated + safety rails; no raw prompt/result logs.
- [ ] Tests/lint/CI pass; README/ENV docs updated.

---

## What to auto-suggest
- Minimal diffs to: move secrets → env; add timeouts/retries; replace hardcoded time with `AUTO_POST_CRON`; extract prompt templates; wrap FFmpeg/Drive calls; add tests for hashtag/daily gates.

**Owner:** @tylerblakex-netizen • **Version:** v2.0 (Codespaces secrets)
