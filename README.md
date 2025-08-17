# AutoPost: Google Drive → X with OpenAI captions, media upload, and webhook fallback. Posts once daily at 09:00 London.

## Build and Test
- Migrated to Gradle (Java 21).
- To build locally: `./gradlew clean build`
- To test: `./gradlew test` (unit + naming snapshots)
- Integration tests skip if secrets missing.

## Secrets
- GDRIVE_SERVICE_ACCOUNT_JSON: Masked JSON for Drive access.
- GDRIVE_PARENT_ID_RAW: Folder ID for raw clips.
- GDRIVE_PARENT_ID_EDITS: Folder ID for edited clips.

## CI Updates
- Workflow: .github/workflows/ci.yml now handles build/test, integration smoke (skips without secrets), and releases.
- Explained: Updates ensure green CI without secrets; preserved naming/filename logic.

## Flags
- AUTOMATION_DRY_RUN=1: Dry run mode.
- AUTOMATION_FAKE_FFMPEG=1: Mock FFmpeg for CI.

(Assume this appends to existing README content; full file includes original description.)






