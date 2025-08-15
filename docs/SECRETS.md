# Secrets

The application expects the following environment variables:

## Core Required Variables
- `SERVICE_PUBLIC_ID` – public identifier for the external service.
- `SERVICE_SECRET_KEY` – secret key used to authenticate with the service.
- `OPENAI_API_KEY` – OpenAI API key for LLM features.
- `RAW_FOLDER_ID` – Google Drive folder ID for raw video files.
- `EDITS_FOLDER_ID` – Google Drive folder ID for edited video files.

## Optional X/Twitter Variables (all use X_ prefix)
- `X_API_KEY` – X/Twitter API key.
- `X_API_SECRET` – X/Twitter API secret.
- `X_ACCESS_TOKEN` – X/Twitter access token.
- `X_ACCESS_TOKEN_SECRET` – X/Twitter access token secret.

Set these values using your environment or your platform's secret manager (e.g. GitHub Secrets).

For local development, copy `.env.example` to `.env` and fill in your credentials.
