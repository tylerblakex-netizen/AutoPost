# Secrets

The application expects the following environment variables:

- `SERVICE_PUBLIC_ID` – public identifier for the external service.
- `SERVICE_SECRET_KEY` – secret key used to authenticate with the service.

Set these values using your environment or your platform's secret manager (e.g. GitHub Secrets).

For local development, copy `.env.example` to `.env` and fill in your credentials.
