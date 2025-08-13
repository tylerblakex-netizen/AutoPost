# Multi-stage Dockerfile for AutoPost
# Build stage uses Maven Wrapper, runtime stage uses slim JRE
# No secrets are embedded at build time - all secrets come from environment at runtime

# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /build

# Install necessary tools
RUN apk add --no-cache \
    git \
    && rm -rf /var/cache/apk/*

# Copy Maven wrapper and configuration
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./
RUN chmod +x mvnw

# Download dependencies (for better layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/
COPY spotbugs-exclude.xml ./

# Build the application with reproducible builds
# Skip quality checks in Docker build for speed - they should run in CI
RUN ./mvnw clean package -DskipTests -Pfast -B \
    && java -Djarmode=layertools -jar target/autopost.jar extract

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -g 1001 autopost \
    && adduser -D -u 1001 -G autopost autopost

# Install ffmpeg and other runtime dependencies
RUN apk add --no-cache \
    ffmpeg \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Set working directory
WORKDIR /app

# Copy application layers from builder stage
COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

# Create required directories
RUN mkdir -p /app/clips /app/tmp /app/state \
    && chown -R autopost:autopost /app

# Switch to non-root user
USER autopost

# Set timezone to UTC (can be overridden with TZ environment variable)
ENV TZ=UTC

# Health check endpoint (if running in server mode)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port for server mode
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -Djava.security.egd=file:/dev/./urandom"

# Entry point with support for different modes
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher $@", "AutoPost"]

# Default command runs in CLI mode (can be overridden)
CMD []

# Labels for metadata
LABEL maintainer="AutoPost Team" \
      version="1.1.0" \
      description="Automated video processing and social media posting application" \
      org.opencontainers.image.title="AutoPost" \
      org.opencontainers.image.description="Automated video processing and social media posting" \
      org.opencontainers.image.vendor="AutoPost" \
      org.opencontainers.image.version="1.1.0" \
      org.opencontainers.image.licenses="Custom"

# Documentation for required environment variables
# NOTE: These are provided at runtime, never at build time
# Required:
# - OPENAI_API_KEY
# - X_API_KEY, X_API_SECRET, X_ACCESS_TOKEN, X_ACCESS_TOKEN_SECRET  
# - GOOGLE_SERVICE_ACCOUNT_JSON
# - GOOGLE_RAW_FOLDER_ID, GOOGLE_EDITS_FOLDER_ID
# Optional:
# - ANTHROPIC_API_KEY, GROK_API_KEY
# - X_BEARER_TOKEN
# - GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
# - AUTO_POST_CRON (default: "0 0 9 * * *")
# - MAX_HASHTAGS (default: 3)
# - POST_ONE_TEASER_PER_DAY (default: true)
# - FFMPEG_PATH, FFPROBE_PATH
# - WEBHOOK_URL