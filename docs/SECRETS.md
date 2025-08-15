# Secrets Setup (CI-safe)

- Put real tokens only in **GitHub → Settings → Secrets and variables → Actions**.
- Never commit `.env` — use `.env.example` for keys with empty values.
- If a secret ever leaks, rotate it and force-push a removal commit.
- CI consumes secrets via `${{ secrets.NAME }}` only in publish/deploy jobs.
