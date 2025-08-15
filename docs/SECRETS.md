# Secrets

The application expects the following environment variables for various integrations:

- `OPENAI_API_KEY` – API key for OpenAI services (required)
- `RAW_FOLDER_ID` – Google Drive folder ID for raw video files (required)
- `EDITS_FOLDER_ID` – Google Drive folder ID for edited video files (required)
- `GOOGLE_SERVICE_ACCOUNT_JSON` – Google Service Account JSON as string (optional)
- `GOOGLE_APPLICATION_CREDENTIALS` – Path to Google Service Account JSON file (optional)
- `TWITTER_API_KEY` – Twitter API key (optional)
- `TWITTER_API_SECRET` – Twitter API secret (optional)
- `TWITTER_ACCESS_TOKEN` – Twitter access token (optional)
- `X_ACCESS_TOKEN_SECRET` – Twitter access token secret (optional)

Set these values using your environment or your platform's secret manager (e.g. GitHub Secrets).

For local development, copy `.env.example` to `.env` and fill in your credentials.
