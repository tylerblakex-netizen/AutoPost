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

1. **Build the application:**
   ```bash
   mvn package
   ```

2. **Set up environment variables:**
   ```bash
   # Required
   export OPENAI_API_KEY="your-openai-key"
   export RAW_FOLDER_ID="google-drive-folder-id"
   export EDITS_FOLDER_ID="google-drive-folder-id"
   
   # Google Drive authentication (choose one)
   export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
   # OR
   export GOOGLE_SERVICE_ACCOUNT_JSON='{"type": "service_account", ...}'
   
   # X/Twitter (optional)
   export TWITTER_API_KEY="your-api-key"
   export TWITTER_API_SECRET="your-api-secret"
   export TWITTER_ACCESS_TOKEN="your-access-token" 
   export TWITTER_ACCESS_SECRET="your-access-secret"
   
   # Webhook alternative (optional)
   export WEBHOOK_URL="https://your-webhook-endpoint.com"
   ```

3. **Run the application:**
   ```bash
   # Normal operation (processes videos if in optimal time slot)
   java -jar target/autopost.jar
   
   # Analyze posting times to determine optimal schedule
   java -jar target/autopost.jar analyze
   
   # Run as Spring Boot web server with REST API endpoints
   java -jar target/autopost.jar server
   ```

## Configuration Options

All configuration is done via environment variables:

### Core Settings
- `OPENAI_API_KEY` - OpenAI API key (required)
- `OPENAI_MODEL` - OpenAI model to use (default: gpt-4o-mini)
- `RAW_FOLDER_ID` - Google Drive folder ID for raw videos (required)
- `EDITS_FOLDER_ID` - Google Drive folder ID for processed videos (required)

### Authentication
- `GOOGLE_APPLICATION_CREDENTIALS` - Path to service account JSON file
- `GOOGLE_SERVICE_ACCOUNT_JSON` - Service account JSON content as string

### Social Media
- `TWITTER_API_KEY`, `TWITTER_API_SECRET`, `TWITTER_ACCESS_TOKEN`, `TWITTER_ACCESS_SECRET` - X/Twitter credentials
- `WEBHOOK_URL` - Alternative webhook endpoint for posting

### Scheduling
- `AUTO_POST_CRON` - Cron expression for automated posting schedule (default: "0 5 0 * * *" - daily at 00:05 UTC)

### Video Processing
- `FFMPEG_PATH` - Path to ffmpeg binary (default: ffmpeg)
- `FFPROBE_PATH` - Path to ffprobe binary (default: ffprobe) 
- `FFMPEG_TEMP_DIR` - Temporary directory for video processing
- `SCENE_THRESHOLD` - Scene detection sensitivity (default: 0.4)
- `CLIP_DURATION_SEC` - Length of short clips in seconds (default: 20)
- `TEASER_DURATION_SEC` - Length of teaser clip in seconds (default: 180)
- `NUM_CLIPS` - Number of short clips to generate (default: 3)

## Requirements

- Java 17+
- Maven 3.6+
- ffmpeg and ffprobe installed and accessible
- Google Drive service account with folder access
- OpenAI API access
- X/Twitter API credentials or webhook endpoint

## Building

```bash
git clone <repository>
cd AutoPost
./create-java.sh  # Run scaffold script to generate project
mvn package       # Build the application
```

The built JAR will be available at `target/autopost.jar`.

## Automation

This repository uses a consolidated GitHub Actions workflow:

| Function | Trigger | File |
|----------|---------|------|
| Daily posting (09:00 London ≈ 08:00 UTC) | Hourly schedule with gating | `.github/workflows/autopost.yml` |
| Weekly analysis (Mon 03:00 UTC) | Hourly schedule with gating | `.github/workflows/autopost.yml` |
| Manual runs | `workflow_dispatch` inputs | `.github/workflows/autopost.yml` |
| Continuous Integration (build/tests) | push / PR to main | `.github/workflows/ci.yml` |

Manual dispatch inputs:
- `mode`: run | analyze | both
- `dry_run`: true to avoid posting to X
- `force`: true to bypass time gating

`sa.json` (Google service account credentials) is generated at runtime and ignored by Git.

<!-- AUTOGEN SECTION (do not edit manually; future tooling may update) -->
<!-- /AUTOGEN SECTION -->

## License

This project is provided as-is for educational and personal use.
