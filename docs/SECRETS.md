# Secrets Configuration

This project requires the following environment variables:

- `SERVICE_PUBLIC_ID`: Public identifier for the external service.
- `SERVICE_SECRET_KEY`: Secret key for the external service.

For local development, create a `.env` file based on `.env.example` and populate these values. The `.env` file is ignored from version control.

In production and CI environments, set these variables using the platform's secret management. For example, in GitHub Actions use `${{ secrets.SERVICE_PUBLIC_ID }}` and `${{ secrets.SERVICE_SECRET_KEY }}`.
