# Secrets Management

The application relies on several environment variables. For local development, copy `.env.example` to `.env` and fill in your own values.

| Variable | Description |
|----------|-------------|
| `OPENAI_API_KEY` | OpenAI API key |
| `RAW_FOLDER_ID` | Google Drive folder for raw videos |
| `EDITS_FOLDER_ID` | Google Drive folder for edited videos |
| `WEBHOOK_URL` | Optional webhook endpoint |
| `GOOGLE_APPLICATION_CREDENTIALS` | Path to service account JSON |
| `GOOGLE_SERVICE_ACCOUNT_JSON` | Inline service account JSON |
| `TWITTER_API_KEY` | Twitter API key |
| `TWITTER_API_SECRET` | Twitter API secret |
| `TWITTER_ACCESS_TOKEN` | Twitter access token |
| `TWITTER_ACCESS_SECRET` | Twitter access secret |
| `SERVICE_PUBLIC_ID` | Public identifier for external service |
| `SERVICE_SECRET_KEY` | Secret key for external service |

In CI, configure these as [GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets) so workflows can access them without exposing values.
