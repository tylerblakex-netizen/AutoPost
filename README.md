# AutoPost (Java)

AutoPost is an automated video processing and social media posting application that:

- Downloads raw videos from Google Drive
- Uses ffmpeg to detect scenes and cut them into clips (3×20s + 1×180s teaser) 
- Converts videos to 1080p60
- Generates captions using OpenAI
- Posts to X (Twitter) with OAuth1 or sends to webhook
- Moves processed files to EDITS folder
- Learns optimal posting times and only posts during best hours (Europe/London timezone)

## Features

- **Scene Detection**: Automatically detects scene changes and creates clips
- **Video Processing**: Converts to 1080p60 with proper encoding settings
- **AI Captions**: Generates engaging captions using OpenAI GPT models
- **Smart Timing**: Analyzes posting performance to determine best posting times
- **Multiple Outputs**: Supports both X/Twitter posting and webhook delivery
- **File Management**: Automatically organizes processed files
- **Dual Mode**: Can run as CLI application or Spring Boot web server with REST API

## Quick Start

1. Build:
   ```bash
   mvn package
   ```
2. Environment variables:
   ```bash
   # Required
   export OPENAI_API_KEY="your-openai-key"
   export RAW_FOLDER_ID="google-drive-folder-id"
   export EDITS_FOLDER_ID="google-drive-folder-id"

   # Google Drive auth (choose one)
   export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
   # OR inline JSON
   export GOOGLE_SERVICE_ACCOUNT_JSON='{"type":"service_account", ...}'

   # X/Twitter (optional)
   export TWITTER_API_KEY="your-api-key"
   export TWITTER_API_SECRET="your-api-secret"
   export TWITTER_ACCESS_TOKEN="your-access-token"
   export TWITTER_ACCESS_SECRET="your-access-secret"

   # Webhook fallback (optional)
   export WEBHOOK_URL="https://your-webhook-endpoint.com"
   ```
3. Run:
   ```bash
   # Standard (will check best time gating)
   java -jar target/autopost.jar

   # Analyze posting performance
   java -jar target/autopost.jar analyze

   # (If server mode is implemented later)
   java -jar target/autopost.jar server
   ```

## Configuration Options

| Variable | Description | Required |
|----------|-------------|----------|
| OPENAI_API_KEY | OpenAI API key | Yes |
| RAW_FOLDER_ID | Google Drive folder for raw input videos | Yes |
| EDITS_FOLDER_ID | Google Drive folder for processed/archived files | Yes |
| GOOGLE_APPLICATION_CREDENTIALS | Path to service account JSON | One of path or inline |
| GOOGLE_SERVICE_ACCOUNT_JSON | Inline JSON credentials | One of path or inline |
| WEBHOOK_URL | Fallback / alternative posting target | No |
| TWITTER_* | X/Twitter OAuth1 creds | Optional |
| CLIP_DURATION_SEC | Short clip length (default 20) | No |
| TEASER_DURATION_SEC | Teaser clip length (default 180) | No |
| NUM_CLIPS | Number of short clips (default 3) | No |
| SCENE_THRESHOLD | Scene change sensitivity (default 0.4) | No |

## Requirements

- Java 17+
- Maven 3.6+
- ffmpeg & ffprobe installed
- Google Drive service account with access to both folders
- OpenAI API key
- Optional: X/Twitter API credentials or webhook endpoint

## Building

```bash
git clone <repository>
cd AutoPost
mvn package
```

Result: `target/autopost.jar`.

## Automation Workflows

Two coordinated GitHub Actions workflows manage analysis and posting:

| Workflow | Purpose | Gated? | Triggers |
|----------|---------|--------|----------|
| `autopost.yml` | Scheduled analysis + time‑gated posting | Yes (08:00 UTC post; Mon 03:00 UTC analysis unless forced) | schedule, workflow_dispatch, repository_dispatch |
| `agent-trigger.yml` | Immediate manual/agent run (post or analyze) | No | workflow_dispatch, repository_dispatch |

### Gated Workflow (autopost.yml)

Runs every hour; actually performs:
- Analysis if Monday 03:00 UTC OR mode=analyze/both OR force=true
- Post if 08:00 UTC OR force=true (and mode=run/both)

Inputs (workflow_dispatch):
- mode: run | analyze | both
- dry_run: true/false
- force: true/false

repository_dispatch events:
- `autopost_run`
- `autopost_analyze`
Optional `client_payload`: mode, force, dry_run.

### Direct Workflow (agent-trigger.yml)

Always runs immediately.
repository_dispatch:
- `agent_autopost`
- `agent_analyze`

workflow_dispatch inputs:
- action: autopost | analyze
- dry_run: true/false

### Examples

```bash
# Gated (may skip if not at target time)
curl -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
  -d '{"event_type":"autopost_run"}' \
  https://api.github.com/repos/OWNER/REPO/dispatches

# Force gated run now
curl -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
  -d '{"event_type":"autopost_run","client_payload":{"force":"true"}}' \
  https://api.github.com/repos/OWNER/REPO/dispatches

# Immediate run (no gating)
curl -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
  -d '{"event_type":"agent_autopost"}' \
  https://api.github.com/repos/OWNER/REPO/dispatches

# Immediate analysis
curl -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
  -d '{"event_type":"agent_analyze"}' \
  https://api.github.com/repos/OWNER/REPO/dispatches

# Combined analyze + run with overrides (gated workflow)
curl -H "Authorization: Bearer $TOKEN" -H "Accept: application/vnd.github+json" \
  -d '{"event_type":"autopost_run","client_payload":{"mode":"both","dry_run":"true","force":"true"}}' \
  https://api.github.com/repos/OWNER/REPO/dispatches
```

### Choosing a Trigger

| Goal | Use |
|------|-----|
| Respect daily timing | `autopost_run` |
| Ignore timing & run now | `agent_autopost` or `autopost_run` + force:true |
| Immediate analysis | `agent_analyze` |
| Force analyze + run | `autopost_run` client_payload mode=both + force:true |
| Safe dry test | Add dry_run:true |

### Dry Run

`dry_run=true` sets `DRY_RUN` and your code should avoid external posting.

### Force

`force=true` bypasses internal time gating in `autopost.yml`.

### Artifacts & Commits

- `best_slots.json` / `analysis.md` committed only if changed.
- Artifacts uploaded for both analysis and autopost runs if present.

## License

This project is provided as-is for educational and personal use.
