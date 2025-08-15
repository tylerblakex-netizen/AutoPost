# AutoPost — Gradle Build

- Migrated from Maven to Gradle (Java 21).
- CI compiles first and uploads `compile-log` for quick missing-deps debug.
- Secrets are **not** in the repo. Use GitHub Secrets:
  - OPENAI_API_KEY, ANTHROPIC_API_KEY, X_API_KEY
  - RAW_FOLDER_ID, EDITS_FOLDER_ID, GOOGLE_SERVICE_ACCOUNT_JSON

Add missing dependencies in `build.gradle.kts` as compile errors surface.
Example:
implementation("org.apache.commons:commons-lang3:3.14.0")

### Optional external service
This project previously required `SERVICE_PUBLIC_ID` and `SERVICE_SECRET_KEY`. They’re now **optional**:
- CI/CD never requires them.
- If you add repo **Variables** with those names, deploy/integration steps will auto-enable.
- If omitted, those steps skip and the app runs with a no-op client.
