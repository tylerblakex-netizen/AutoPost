# Secrets

The application expects the following environment variables:

### Required

- `OPENAI_API_KEY` – API key for the OpenAI model.
- `RAW_FOLDER_ID` – Google Drive folder containing raw media.
- `EDITS_FOLDER_ID` – Google Drive folder containing edited media.
- One of the following is required for Google API access:
  - GOOGLE_SERVICE_ACCOUNT_JSON – inline JSON credentials.
  - GOOGLE_APPLICATION_CREDENTIALS – path to a service account JSON file.

### Optional

- `SERVICE_PUBLIC_ID` – public identifier for the external service.
- `SERVICE_SECRET_KEY` – secret key used to authenticate with the service.
- `WEBHOOK_URL` – Discord webhook for status updates.
- TWITTER_API_KEY, TWITTER_API_SECRET, TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_TOKEN_SECRET – X/Twitter credentials.

Set these values using your environment or your platform's secret manager (e.g. GitHub Secrets). For local development, copy `.env.example` to `.env` and fill in your credentials.

Never commit real credentials to the repository. The application only reads them from environment variables.
