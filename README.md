# AutoPost — Gradle Build

- Migrated from Maven to Gradle (Java 21).
- CI compiles first and uploads `compile-log` for quick missing-deps debug.
- Secrets are **not** in the repo. Use GitHub Secrets:
  - OPENAI_API_KEY, ANTHROPIC_API_KEY, X_API_KEY
  - RAW_FOLDER_ID, EDITS_FOLDER_ID, GOOGLE_SERVICE_ACCOUNT_JSON

Add missing dependencies in `build.gradle.kts` as compile errors surface.
Example:
implementation("org.apache.commons:commons-lang3:3.14.0")
