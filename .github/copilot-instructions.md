# AutoPost - Automated Video Processing and Social Media Posting

AutoPost is a Java 17 application for automated video processing and social media posting. It downloads videos from Google Drive, uses ffmpeg for scene detection and cutting, generates AI captions with OpenAI, and posts to X/Twitter with intelligent timing. The application supports both CLI mode and Spring Boot web server mode.

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Prerequisites and Installation
- Install Java 17+ and Maven 3.6+:
  ```bash
  # Ubuntu/Debian
  sudo apt-get update
  sudo apt-get install -y openjdk-17-jdk maven
  
  # Verify installation
  java -version    # Should show OpenJDK 17.x.x
  mvn -version     # Should show Maven 3.6+
  ```

- Install ffmpeg (required for video processing):
  ```bash
  sudo apt-get install -y ffmpeg
  
  # Verify installation
  which ffmpeg     # Should show /usr/bin/ffmpeg
  which ffprobe    # Should show /usr/bin/ffprobe
  ffmpeg -version  # Should show version 6.x or higher
  ```

### Build Process
- **NEVER CANCEL** the Maven build - first build takes approximately 2-3 minutes, subsequent builds ~6 seconds
- Set timeout to 5+ minutes for build commands (especially first build)
- Build the application:
  ```bash
  mvn clean package -DskipTests
  ```
- **Build time**: 
  - First build: 2-3 minutes (downloads dependencies). **NEVER CANCEL** - wait for completion
  - Subsequent builds: ~6 seconds
- The shaded JAR will be created at `target/autopost.jar` (~31MB)

### Running the Application

**Important**: The application requires several environment variables. Without them, it will fail gracefully with descriptive error messages.

#### CLI Mode (Default)
```bash
java -jar target/autopost.jar
```
- Processes videos only during optimal posting times (learned behavior)
- Downloads from Google Drive RAW folder
- Creates 3×20s clips + 1×180s teaser
- Posts to X/Twitter or sends to webhook
- Moves processed files to EDITS folder

#### Analysis Mode
```bash
java -jar target/autopost.jar analyze
```
- Analyzes Twitter posting history to determine optimal posting times
- Creates `best_slots.json` and `analysis.md` files
- **Requires**: OPENAI_API_KEY, TWITTER_* credentials

#### Server Mode (Spring Boot Web API)
```bash
java -jar target/autopost.jar server
```
- Starts Spring Boot web server on port 8080
- Provides REST API endpoints and health checks
- Access health status: `curl http://localhost:8080/health`
- **Requires**: All environment variables to be set

### Testing and Validation

**No test suite exists** - this is normal for this project type. Validation is done through:

1. **Build validation**: Ensure `mvn clean package -DskipTests` succeeds
2. **Runtime validation**: Test each mode:
   ```bash
   # Test CLI mode (will skip if not in optimal time slot)
   java -jar target/autopost.jar
   
   # Test analyze mode (will fail without API keys - expected)
   java -jar target/autopost.jar analyze
   
   # Test server mode (will fail without env vars - expected)
   timeout 10 java -jar target/autopost.jar server
   ```

3. **Manual scenario testing**: To fully validate changes:
   - Set up environment variables (see Configuration section)
   - Test video processing workflow
   - Verify API integrations work
   - Check file management operations

### Configuration

All configuration is done via environment variables:

#### Required Variables
```bash
export OPENAI_API_KEY="your-openai-api-key"
export RAW_FOLDER_ID="google-drive-folder-id"
export EDITS_FOLDER_ID="google-drive-folder-id"

# Google Drive authentication (choose one)
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
# OR
export GOOGLE_SERVICE_ACCOUNT_JSON='{"type": "service_account", ...}'
```

#### Optional Variables
```bash
# X/Twitter integration
export TWITTER_API_KEY="your-api-key"
export TWITTER_API_SECRET="your-api-secret"  
export TWITTER_ACCESS_TOKEN="your-access-token"
export TWITTER_ACCESS_SECRET="your-access-secret"

# Webhook alternative
export WEBHOOK_URL="https://your-webhook-endpoint.com"

# Video processing settings
export FFMPEG_PATH="ffmpeg"
export FFPROBE_PATH="ffprobe"
export SCENE_THRESHOLD="0.4"
export CLIP_DURATION_SEC="20"
export TEASER_DURATION_SEC="180"
export NUM_CLIPS="3"
```

## Repository Structure and Key Files

### Project Scaffolding
- Use `./create-java.sh` to regenerate the entire project structure
- This script creates all source files, Maven configuration, and CI workflows
- **Run time**: ~1 second

### Key Directories
```
src/main/java/com/autopost/     # Main application code
├── App.java                    # Main entry point
├── AutoPostApplication.java    # Spring Boot application
├── Config.java                 # Environment variable configuration
├── Runner.java                 # CLI mode implementation
├── XAnalyzer.java             # Twitter analytics
├── service/                   # Spring Boot services
├── controller/                # REST API controllers
└── [other services]           # Video, Drive, Caption services

src/main/resources/
├── application.properties      # Spring Boot configuration
└── collabs.json               # Collaborator mappings

.github/workflows/             # CI/CD workflows
├── autopost-hourly.yml       # Scheduled posting
├── analyze-times.yml          # Weekly analytics
└── [other workflows]

target/                        # Build output
└── autopost.jar              # Shaded executable JAR
```

### Important Files to Watch
- **pom.xml**: Maven dependencies and build configuration
- **Config.java**: Environment variable handling - check here when adding new config options
- **application.properties**: Spring Boot configuration - update when adding server features
- **Runner.java**: Main CLI logic - modify for posting workflow changes
- **AutoPostApplication.java**: Spring Boot entry point

## CI/CD and Workflows

### GitHub Actions Workflows
- **autopost-hourly.yml**: Runs every hour, posts only during optimal times
- **analyze-times.yml**: Runs weekly on Mondays to analyze posting performance
- **setup-everything.yml**: Complete environment setup and dependency installation

### Build Requirements in CI
- Java 17+ (Temurin distribution recommended)
- Maven 3.6+
- ffmpeg package installation
- Service account JSON for Google Drive

## Common Development Tasks

### Quick Validation Commands
These commands should ALWAYS work and complete successfully:
```bash
# Environment check
java -version                    # Should show OpenJDK 17.x.x
mvn -version                    # Should show Maven 3.6+
which ffmpeg && which ffprobe   # Should show /usr/bin paths

# Build and test
mvn clean package -DskipTests   # Should complete in 6 seconds (or 2-3 min first time)
java -jar target/autopost.jar   # Should run and either process videos or skip due to timing

# File validation
ls -la target/autopost.jar      # Should show ~31MB JAR file
file target/autopost.jar        # Should show "Java archive data (JAR)"
```

### Adding New Video Processing Features
1. Modify `VideoProcessor.java` for ffmpeg operations
2. Update environment variables in `Config.java` if needed
3. Test with actual video files if possible
4. Build and test: `mvn clean package -DskipTests && java -jar target/autopost.jar`

### Adding New API Integrations
1. Add dependencies to `pom.xml`
2. Create service class in `src/main/java/com/autopost/`
3. Add configuration to `Config.java` and `application.properties`
4. Update CI workflows if new environment variables needed

### Adding REST API Endpoints
1. Create controller in `src/main/java/com/autopost/controller/`
2. Add services in `src/main/java/com/autopost/service/`
3. Test server mode: `java -jar target/autopost.jar server`
4. Verify endpoint: `curl http://localhost:8080/your-endpoint`

### Environment Variable Changes
1. Update `Config.java` for new variables
2. Update `application.properties` for Spring Boot mapping
3. Update CI workflow files (.github/workflows/)
4. Update README.md documentation

## Validation Scenarios

**CRITICAL**: Always test these scenarios after making changes:

### Scenario 1: Basic Build and Execution
```bash
# Build (should complete in 2-3 minutes)
mvn clean package -DskipTests

# Test basic execution (should run without errors)
java -jar target/autopost.jar
```

### Scenario 2: Server Mode Validation
```bash
# Start server (will fail without env vars - this is expected)
timeout 10 java -jar target/autopost.jar server

# Look for Spring Boot startup logs
# Should see Tomcat initialization before environment variable error
```

### Scenario 3: Configuration Validation
```bash
# Test with minimal valid environment
export OPENAI_API_KEY="test-key"
export RAW_FOLDER_ID="test-folder"
export EDITS_FOLDER_ID="test-folder"
export GOOGLE_SERVICE_ACCOUNT_JSON='{"type": "service_account", "project_id": "test"}'

# Should fail gracefully with authentication errors (expected)
java -jar target/autopost.jar analyze
```

## Timing Expectations

- **Maven build**: 
  - First build: 2-3 minutes (downloads dependencies). **NEVER CANCEL** - set timeout to 5+ minutes
  - Subsequent builds: ~6 seconds
- **Maven test**: <5 seconds (no tests exist)
- **Application startup**: <10 seconds for CLI mode, <30 seconds for server mode
- **ffmpeg installation**: 1-2 minutes depending on network
- **Scaffold script**: <1 second

## Known Limitations and Workarounds

- **No test suite**: Validation is done through manual execution and integration testing
- **Large JAR size**: ~31MB due to shaded dependencies - this is normal
- **External dependencies**: Requires Google Drive API, OpenAI API, and Twitter API credentials for full functionality
- **Timezone dependency**: Posting times are calculated in Europe/London timezone
- **ffmpeg dependency**: Must be installed system-wide and accessible in PATH

## Troubleshooting

### Build Failures
- Ensure Java 17+ is installed and active
- Check Maven version is 3.6+
- Clear Maven cache: `mvn dependency:purge-local-repository`

### Runtime Failures
- Verify ffmpeg installation: `which ffmpeg && ffmpeg -version`
- Check environment variables are set correctly
- Validate Google service account JSON format
- Test API credentials separately

### CI/CD Issues
- Check GitHub secrets are configured
- Verify workflow file syntax
- Ensure Ubuntu runner has required packages

Always build and test your changes before committing. The application should build successfully and execute each mode without critical errors (API authentication failures are expected without proper credentials).