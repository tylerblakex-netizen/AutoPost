# AutoPost Development Instructions

**ALWAYS reference these instructions first** and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

AutoPost is a Java-based automated video processing and social media posting application that downloads videos from Google Drive, processes them with ffmpeg, generates AI captions, and posts to X (Twitter) or webhooks.

## Working Effectively

### Bootstrap and Build (REQUIRED FIRST)
```bash
# Install required system dependencies
sudo apt-get update && sudo apt-get install -y ffmpeg

# Verify Java 17+ is available
java -version  # Must show version 17 or higher

# Build the application - NEVER CANCEL: Takes 2-3 minutes. Set timeout to 5+ minutes.
mvn clean package

# Run tests - NEVER CANCEL: Takes 6 seconds. Set timeout to 30+ seconds.
mvn test
```

**CRITICAL BUILD TIMING**: 
- `mvn clean package`: **Takes 2-7 minutes** (2+ minutes on clean env, up to 7 minutes with dependency downloads) - NEVER CANCEL - Set timeout to 600+ seconds
- `mvn test`: **Takes 3-6 seconds** - Set timeout to 30+ seconds
- First builds take longer due to dependency downloads

### Environment Setup
The application requires these environment variables:

**Required Core Variables:**
```bash
export OPENAI_API_KEY="your-openai-key"
export RAW_FOLDER_ID="google-drive-folder-id"  
export EDITS_FOLDER_ID="google-drive-folder-id"
export SERVICE_PUBLIC_ID="pk_xxxxxxxxx"
export SERVICE_SECRET_KEY="sk_xxxxxxxxx"
```

**Required Google Drive Authentication (choose one):**
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
# OR
export GOOGLE_SERVICE_ACCOUNT_JSON='{"type": "service_account", "client_id": "...", "client_email": "...", "private_key": "...", "private_key_id": "..."}'
```

**Optional X/Twitter Variables:**
```bash
export TWITTER_API_KEY="your-api-key"
export TWITTER_API_SECRET="your-api-secret"  
export TWITTER_ACCESS_TOKEN="your-access-token"
export X_ACCESS_TOKEN_SECRET="your-access-secret"
```

**Optional Webhook Alternative:**
```bash
export WEBHOOK_URL="https://your-webhook-endpoint.com"
```

### Running the Application
After building and setting environment variables:

```bash
# Normal operation (processes videos if in optimal time slot)
java -jar target/autopost.jar

# Analyze posting times to determine optimal schedule  
java -jar target/autopost.jar analyze

# Run as Spring Boot web server with REST API endpoints
java -jar target/autopost.jar server
```

## Validation

### CRITICAL Manual Validation Requirements
**ALWAYS run through these complete scenarios after making changes:**

#### Scenario 1: Build and Application Startup
1. Run full build: `mvn clean package` (wait full 2-3 minutes)
2. Set minimal environment variables (use test values)
3. Test normal mode startup: `java -jar target/autopost.jar` (should fail gracefully with credential errors)
4. Test analyze mode startup: `java -jar target/autopost.jar analyze` (should attempt Twitter connection)

#### Scenario 2: Server Mode Full Workflow
1. Set environment variables with test values
2. Start server: `java -jar target/autopost.jar server`
3. Wait for "Started AutoPostApplication" message (takes ~3 seconds)
4. Test endpoints:
   - `curl localhost:8080/` (should return "AutoPost Service - LLM Driven Scheduling")
   - `curl localhost:8080/health` (should return JSON with status "healthy")
5. Stop server with Ctrl+C

#### Scenario 3: Test Suite Validation
1. Run tests: `mvn test` (all tests must pass)
2. Verify ConfigTest specifically tests environment variable loading
3. Check that no tests hardcode credentials

### Pre-Commit Validation
Always run these commands before you are done:
```bash
mvn clean package  # NEVER CANCEL: 2-7 minutes, timeout 600+ seconds
mvn test          # 3-6 seconds
```

The CI pipeline (`.github/workflows/ci.yml`) will fail if build or tests fail.

## Common Tasks

### Repo Structure
```
.
├── README.md               # Project documentation
├── pom.xml                 # Maven build configuration  
├── src/
│   ├── main/java/com/autopost/
│   │   ├── App.java        # Main application entry point
│   │   ├── Config.java     # Environment variable configuration
│   │   ├── Runner.java     # Main processing logic
│   │   └── controller/     # Spring Boot REST controllers
│   └── test/java/com/autopost/
│       └── ConfigTest.java # Configuration tests
├── scripts/
│   └── setup-everything.sh # Setup script (BROKEN - uses Gradle instead of Maven)
└── .github/workflows/
    ├── ci.yml              # Build and test pipeline
    └── autopost.yml        # Scheduled automation workflow
```

### Key Configuration Files
- `pom.xml`: Maven build configuration with dependencies
- `src/main/resources/application.properties`: Spring Boot configuration (if exists)
- `.env.example`: Example environment variables (minimal set)

### Important Notes
- **Build System**: Uses **Maven**, not Gradle (despite what `scripts/setup-everything.sh` assumes)
- **No Linting**: No checkstyle, spotbugs, or PMD configured currently
- **Tests**: Only basic configuration tests exist in `ConfigTest.java`
- **Three Modes**: App supports normal processing, analysis, and server modes

### Known Issues to Document
- `scripts/setup-everything.sh` incorrectly assumes Gradle build system - **it will fail**
- Script attempts to run `gradle wrapper` but project uses Maven  
- No Java code linting tools configured currently
- Minimal test coverage currently exists
- Warning about proxy port in `.mvn/settings.xml` can be ignored

### Dependencies Installation
```bash
# System dependencies  
sudo apt-get update && sudo apt-get install -y ffmpeg

# Java dependencies managed by Maven - run:
mvn dependency:resolve  # Download all dependencies
```

### Build Troubleshooting
**If `scripts/setup-everything.sh` was run by mistake:**
```bash
# It will fail with "Directory does not contain a Gradle build"
# Just ignore the error and use Maven commands instead
mvn clean package  # Use this instead of script
```

**If `mvn package` fails:**
1. Check Java version: `java -version` (must be 17+)
2. Check Maven version: `mvn -version` (should be 3.6+)
3. Clear cache: `mvn clean`
4. Re-run with debug: `mvn clean package -X`

**If tests fail:**
1. Check environment: Tests require `SERVICE_PUBLIC_ID` and `SERVICE_SECRET_KEY` system properties
2. Run single test: `mvn test -Dtest=ConfigTest`
3. Check test output in `target/surefire-reports/`

**If proxy warnings appear:**
- Warnings about `${env.PROXY_PORT}` in `.mvn/settings.xml` can be safely ignored

### Expected Command Outputs

#### `mvn clean package` (Success)
```
[INFO] BUILD SUCCESS
[INFO] Total time:  6.073 s  (can take up to 7 minutes on first run)
```

#### `mvn test` (Success)  
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time: 1.633 s
```

#### `java -jar target/autopost.jar server` (Success)
```
Started AutoPostApplication in 2.495 seconds
Tomcat started on port 8080 (http)
```

#### `curl localhost:8080/health` (Success)
```json
{"next_run_at":"not scheduled","strategy":"llm","status":"healthy","timestamp":"2025-08-15T00:35:28.534241687Z[Etc/UTC]"}
```

## Important Locations

### Key Source Files
- `src/main/java/com/autopost/App.java`: Application entry point with mode switching
- `src/main/java/com/autopost/Config.java`: Environment variable loading and validation
- `src/main/java/com/autopost/Runner.java`: Main video processing logic
- `src/main/java/com/autopost/VideoProcessor.java`: ffmpeg video processing
- `src/main/java/com/autopost/controller/HealthController.java`: REST API endpoints

### Configuration Management
- Environment variables loaded in `Config.java` using `Config.loadFromEnv()`
- Server configuration in Spring Boot auto-configuration
- No external config files needed - all environment-driven

### Testing Infrastructure  
- `src/test/java/com/autopost/ConfigTest.java`: Tests environment variable loading
- Uses JUnit 5 (`junit-jupiter`)
- Maven Surefire plugin for test execution
- Test reports in `target/surefire-reports/`

Remember: **ALWAYS** build and exercise your changes manually through complete user scenarios. The instructions above provide the exact commands and expected outputs that work every time.