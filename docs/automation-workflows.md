# Automation Workflows

This repository includes two GitHub Actions workflows for content analysis and automated posting:

| Workflow | File | Purpose | Gating | Triggers |
|----------|------|---------|--------|----------|
| AutoPost & Analyze | `.github/workflows/autopost.yml` | Scheduled analysis + time‑gated posting | YES (time checks unless forced) | schedule, workflow_dispatch, repository_dispatch |
| Agent Trigger | `.github/workflows/agent-trigger.yml` | Immediate manual/agent execution (run or analyze) | NO (always runs) | workflow_dispatch, repository_dispatch |

## 1. Gated Workflow: autopost.yml

Runs hourly (cron), but:
- Analysis actually executes only if:
  - Monday 03:00 UTC OR
  - mode=analyze / both OR
  - force=true
- Posting (run) executes only if:
  - 08:00 UTC (TARGET) OR
  - force=true
  - And mode=run / both

### Triggers

#### Scheduled (hourly)
Automatically fires; internal gating decides.

#### Manual (workflow_dispatch)

```bash
gh workflow run autopost.yml -f mode=run -f dry_run=false -f force=false
```

#### repository_dispatch

Event types:
- `autopost_run`
- `autopost_analyze`

Optional `client_payload` keys: `mode`, `force`, `dry_run`.

Examples:

```bash
# Time-gated run (may skip if not target time)
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/OWNER/REPO/dispatches \
  -d '{"event_type":"autopost_run"}'

# Force a run immediately (ignores time gating)
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/OWNER/REPO/dispatches \
  -d '{"event_type":"autopost_run","client_payload":{"force":"true"}}'

# Analysis (always runs since mode override)
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/OWNER/REPO/dispatches \
  -d '{"event_type":"autopost_analyze"}'

# Combined + dry run + force
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/OWNER/REPO/dispatches \
  -d '{"event_type":"autopost_run","client_payload":{"mode":"both","dry_run":"true","force":"true"}}'
```

## 2. Direct Workflow: agent-trigger.yml

Always executes immediately—no time gating.

### Manual (workflow_dispatch)

```bash
# Autopost
gh workflow run "Agent Trigger (Direct AutoPost / Analyze)" -f action=autopost -f dry_run=false
# Analyze
gh workflow run "Agent Trigger (Direct AutoPost / Analyze)" -f action=analyze
```

### repository_dispatch

Event types:
- `agent_autopost`
- `agent_analyze`

Examples:

```bash
# Immediate autopost dry run false
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/OWNER/REPO/dispatches \
  -d '{"event_type":"agent_autopost","client_payload":{"dry_run":false}}'

# Immediate analysis
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/OWNER/REPO/dispatches \
  -d '{"event_type":"agent_analyze"}'
```

## Modes

| Mode | Meaning |
|------|---------|
| run | Perform posting pipeline |
| analyze | Generate/refresh best posting time data |
| both | (autopost.yml only) Run analysis + run job (analysis first) |

## Environment / Secrets

Required:
- `OPENAI_API_KEY`
- `GOOGLE_SERVICE_ACCOUNT_JSON`
- `RAW_FOLDER_ID`
- `EDITS_FOLDER_ID`

Optional:
- `WEBHOOK_URL`
- `TWITTER_API_KEY`, `TWITTER_API_SECRET`, `TWITTER_ACCESS_TOKEN`, `TWITTER_ACCESS_SECRET`

## Dry Run

`dry_run=true` sets `DRY_RUN` env; Java logic should skip external posting.

## Force

`force=true` bypasses time gating inside `autopost.yml`.

## Artifacts & Commits

- `analysis.md` and `best_slots.json` committed only if changed.
- Artifacts uploaded:
  - Analysis: `best_slots.json`, `analysis.md`
  - Autopost: same (if present)

## Typical Use Cases

| Scenario | Recommended Trigger |
|----------|---------------------|
| Daily scheduled post at target time | Let cron + gating handle |
| Immediate manual post (ignore schedule) | `agent_autopost` |
| Weekly analysis (automatic) | Monday 03:00 UTC schedule |
| On-demand analysis right now | `agent_analyze` |
| Debug logic without posting | Any trigger with `dry_run=true` |

## Troubleshooting

| Symptom | Cause | Action |
|---------|-------|--------|
| Job skipped (Ran: false) | Time gating not satisfied | Add `force:true` |
| Secrets not found | Not configured | Add secrets |
| Posting step fails | ffmpeg or credentials missing | Ensure install & secrets |
| No commit | No file changes | Expected |

## Future Extensions

- Additional event types (e.g., `autopost_teaser`)
- Slack/Discord notifications
- Auto-cancel older runs (`cancel-in-progress: true`)
- Matrix run for multiple clip durations

README snippet (for quick insertion):

```markdown
### Automation Workflows

Workflows:
1. `autopost.yml` (gated) — scheduled hourly, runs only at configured times unless `force=true`.
2. `agent-trigger.yml` (direct) — runs immediately on dispatch.

See docs/automation-workflows.md for full details.
```