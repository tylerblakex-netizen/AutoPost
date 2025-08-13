# AutoPost (Java)

AutoPost is an automated video processing and social media posting application that:

- Downloads raw videos from Google Drive
- Uses ffmpeg to detect scenes and cut them into clips (3×20s + 1×180s teaser) 
- Converts videos to 1080p60
- Generates captions using OpenAI with safety rails and hashtag limits
- Posts to X (Twitter) with OAuth1 or sends to webhook
- Moves processed files to EDITS folder
- Learns optimal posting times and only posts during best hours (configurable via UTC cron)
- Features reproducible builds, robust error handling, and comprehensive security

## Features

- **Scene Detection**: Automatically detects scene changes and creates clips
- **Video Processing**: Converts to 1080p60 with proper encoding settings
- **AI Captions**: Generates engaging, algorithm-safe captions using templated prompts
- **Smart Timing**: Uses AUTO_POST_CRON (UTC) with optional LLM-optimized scheduling
- **Multiple Outputs**: Supports both X/Twitter posting and webhook delivery
- **File Management**: Automatically organizes processed files with atomic operations
- **Dual Mode**: Can run as CLI application or Spring Boot web server with REST API
- **Security First**: No secrets in code/logs, comprehensive redaction, fail-fast validation
- **Reproducible Builds**: Deterministic JAR outputs, pinned dependencies, quality gates
- **Containerized**: Multi-stage Docker image, Codespaces-ready devcontainer

## Quick Start

1. **Set up environment variables** (see [Secrets Setup](#secrets-setup) below)
2. **Build the application:**
   ```bash
   git clone <repository>
   cd AutoPost
   ./mvnw clean package
   ```
3. **Run the application:**
   ```bash
   # CLI mode (default)
   java -jar target/autopost.jar
   
   # Analyze posting times to determine optimal schedule
   java -jar target/autopost.jar analyze
   
   # Run as Spring Boot web server with REST API endpoints
   java -jar target/autopost.jar server
   ```

## Secrets Setup

### Required Environment Variables

All secrets are provided via environment variables. **Never commit secrets to code.**

**OpenAI Configuration:**
- `OPENAI_API_KEY` - Your OpenAI API key (required)

**X/Twitter Configuration:**
- `X_API_KEY` - Twitter/X API key (required)
- `X_API_SECRET` - Twitter/X API secret (required)  
- `X_ACCESS_TOKEN` - Twitter/X OAuth access token (required)
- `X_ACCESS_TOKEN_SECRET` - Twitter/X OAuth access token secret (required)

**Google Drive Configuration:**
- `GOOGLE_SERVICE_ACCOUNT_JSON` - Service account JSON as a single string (required)
- `GOOGLE_RAW_FOLDER_ID` - Google Drive RAW folder ID (required)
- `GOOGLE_EDITS_FOLDER_ID` - Google Drive EDITS folder ID (required)

### Optional Environment Variables

**Additional AI APIs:**
- `ANTHROPIC_API_KEY` - Anthropic API key (optional)
- `GROK_API_KEY` - Grok API key (optional)

**Additional X/Twitter:**
- `X_BEARER_TOKEN` - Twitter/X Bearer token (optional)

**Additional Google:**
- `GOOGLE_CLIENT_ID` - Google OAuth client ID (optional)
- `GOOGLE_CLIENT_SECRET` - Google OAuth client secret (optional)

**Application Configuration:**
- `AUTO_POST_CRON` - Posting schedule in UTC (default: "0 0 9 * * *" = 09:00 UTC daily)
- `MAX_HASHTAGS` - Maximum hashtags per post (default: 3, max: 10)
- `POST_ONE_TEASER_PER_DAY` - Enforce one teaser per day (default: true)
- `FFMPEG_PATH` - Path to ffmpeg binary (default: "ffmpeg")
- `FFPROBE_PATH` - Path to ffprobe binary (default: "ffprobe")
- `WEBHOOK_URL` - Webhook URL for posting fallback (optional)

### Backward Compatibility

The following legacy environment variable names are still supported:
- `RAW_FOLDER_ID` → `GOOGLE_RAW_FOLDER_ID`
- `EDITS_FOLDER_ID` → `GOOGLE_EDITS_FOLDER_ID`

### GitHub Codespaces Setup

1. **Go to your GitHub Codespaces secrets settings**
2. **Add each required secret** with repository access to 'AutoPost'
3. **IMPORTANT: Stop and restart your Codespace** after adding/updating secrets
4. Secrets are only available at runtime, not during container build

### Local Development Setup

Create a `.env` file (excluded by `.gitignore`):
```bash
# .env file (DO NOT COMMIT)
export OPENAI_API_KEY="your-openai-key"
export X_API_KEY="your-x-api-key"
export X_API_SECRET="your-x-api-secret"
export X_ACCESS_TOKEN="your-x-access-token"
export X_ACCESS_TOKEN_SECRET="your-x-access-token-secret"
export GOOGLE_SERVICE_ACCOUNT_JSON='{"type":"service_account",...}'
export GOOGLE_RAW_FOLDER_ID="your-raw-folder-id"
export GOOGLE_EDITS_FOLDER_ID="your-edits-folder-id"
```

Then source it: `source .env && java -jar target/autopost.jar`

## Requirements

- **Java 17+** (tested with Java 17 and 21)
- **Maven 3.6.3+** (Maven Wrapper included)
- **ffmpeg and ffprobe** installed and accessible
- **Google Drive service account** with folder access
- **OpenAI API access**
- **X/Twitter API credentials** or webhook endpoint

## Building

### Standard Build
```bash
# Build with all quality checks
./mvnw clean package

# Fast build (skip quality checks)
./mvnw clean package -Pfast

# Build for specific Java version
./mvnw clean package -Djava.version=21
```

### Code Quality
```bash
# Format code
./mvnw spotless:apply

# Check formatting, SpotBugs, and Checkstyle
./mvnw spotless:check spotbugs:check checkstyle:check

# Run tests
./mvnw test

# Run integration tests
./mvnw verify
```

### Security Scanning
```bash
# Install gitleaks (if not already available)
# See: https://github.com/gitleaks/gitleaks#installation

# Run security scan
gitleaks detect --config .gitleaks.toml
```

## Docker Usage

### Build Image
```bash
docker build -t autopost:latest .
```

### Run Container
```bash
# CLI mode
docker run --rm \
  -e OPENAI_API_KEY="$OPENAI_API_KEY" \
  -e X_API_KEY="$X_API_KEY" \
  -e X_API_SECRET="$X_API_SECRET" \
  -e X_ACCESS_TOKEN="$X_ACCESS_TOKEN" \
  -e X_ACCESS_TOKEN_SECRET="$X_ACCESS_TOKEN_SECRET" \
  -e GOOGLE_SERVICE_ACCOUNT_JSON="$GOOGLE_SERVICE_ACCOUNT_JSON" \
  -e GOOGLE_RAW_FOLDER_ID="$GOOGLE_RAW_FOLDER_ID" \
  -e GOOGLE_EDITS_FOLDER_ID="$GOOGLE_EDITS_FOLDER_ID" \
  autopost:latest

# Server mode
docker run -d \
  -p 8080:8080 \
  -e OPENAI_API_KEY="$OPENAI_API_KEY" \
  -e X_API_KEY="$X_API_KEY" \
  -e X_API_SECRET="$X_API_SECRET" \
  -e X_ACCESS_TOKEN="$X_ACCESS_TOKEN" \
  -e X_ACCESS_TOKEN_SECRET="$X_ACCESS_TOKEN_SECRET" \
  -e GOOGLE_SERVICE_ACCOUNT_JSON="$GOOGLE_SERVICE_ACCOUNT_JSON" \
  -e GOOGLE_RAW_FOLDER_ID="$GOOGLE_RAW_FOLDER_ID" \
  -e GOOGLE_EDITS_FOLDER_ID="$GOOGLE_EDITS_FOLDER_ID" \
  autopost:latest server
```

## Configuration Options

### Cron Expression Format

`AUTO_POST_CRON` uses Spring cron format (6 fields):
```
# Format: second minute hour day month dayOfWeek
# Examples:
"0 0 9 * * *"      # Daily at 09:00 UTC
"0 30 14 * * MON"  # Mondays at 14:30 UTC  
"0 0 8,20 * * *"   # Daily at 08:00 and 20:00 UTC
```

### Hashtag Policy

- Maximum hashtags enforced via `MAX_HASHTAGS` (default: 3)
- LLM prompts include safety rails for algorithm-safe content
- Hashtags are validated and limited automatically

### Posting Gates

- `POST_ONE_TEASER_PER_DAY=true` prevents multiple posts per day
- Scheduling respects UTC timezone for consistency
- Failed posts are logged with redacted sensitive information

## Automation

This repository uses a robust GitHub Actions CI/CD pipeline:

| Function | Trigger | File |
|----------|---------|------|
| **Clean Build** | push/PR to main | `.github/workflows/ci.yml` |
| **Cached Build** | push/PR to main | `.github/workflows/ci.yml` |
| **Security Scan** | push/PR to main | Gitleaks in CI |
| **Daily Posting** | Scheduled (configurable) | Application cron |

### CI Features
- **Java 17 & 21 support** with matrix builds
- **Reproducible builds** verification
- **Code quality gates** (Spotless, SpotBugs, Checkstyle)
- **Security scanning** with Gitleaks
- **Fast cached builds** for development
- **Comprehensive testing** with mocked external services

### Manual Operations
The application supports various manual modes:
- `java -jar autopost.jar` - Standard posting mode
- `java -jar autopost.jar analyze` - Posting time analysis
- `java -jar autopost.jar server` - REST API server mode

## Security

- **No secrets in code** - All configuration via environment variables
- **Comprehensive redaction** - Sensitive data masked in logs
- **Fail-fast validation** - Missing configuration detected on startup
- **Gitleaks scanning** - Automated secret detection in CI
- **Secure containers** - Non-root user, minimal attack surface
- **Audit trail** - All external calls logged (safely)

## Development

### Pre-commit Setup (Optional)
```bash
# Install gitleaks for local secret scanning
# macOS
brew install gitleaks

# Linux
curl -sSfL https://github.com/gitleaks/gitleaks/releases/latest/download/gitleaks-linux-amd64.tar.gz | tar -xz -C /usr/local/bin

# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
set -euo pipefail
echo "Running gitleaks scan..."
gitleaks detect --config .gitleaks.toml --staged --redact
EOF
chmod +x .git/hooks/pre-commit
```

### IDE Setup
- **IntelliJ IDEA**: Import as Maven project, configure Google Java Style
- **VS Code**: Use the provided devcontainer for automatic setup
- **Eclipse**: Import Maven project, install Checkstyle plugin

<!-- AUTOGEN SECTION (do not edit manually; future tooling may update) -->
<!-- /AUTOGEN SECTION -->

## License

This project is provided as-is for educational and personal use.

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
