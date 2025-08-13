#!/usr/bin/env bash
# scripts/setup-everything.sh
# One big, bossy setup script for AutoPost (Spring Boot).
# It validates the env, fixes config, writes missing files, and builds the app.
# Run: bash scripts/setup-everything.sh
# Env:
#   AUTO_POST_CRON="0 5 0 * * *"   # default if not set
#   SKIP_TESTS="1"                 # optional; set to skip unit tests
#   JAVA_HOME=/path/to/java        # optional; if multiple JDKs installed

set -euo pipefail

### ────────────────────────────────────────────────────────────────────────────
### Styling / logging
### ────────────────────────────────────────────────────────────────────────────
if [ -t 1 ]; then
  BOLD="$(printf '\033[1m')"; DIM="$(printf '\033[2m')"; RED="$(printf '\033[31m')"
  GRN="$(printf '\033[32m')"; YLW="$(printf '\033[33m')"; BLU="$(printf '\033[34m')"
  MAG="$(printf '\033[35m')"; CYN="$(printf '\033[36m')"; RST="$(printf '\033[0m')"
else
  BOLD=""; DIM=""; RED=""; GRN=""; YLW=""; BLU=""; MAG=""; CYN=""; RST=""
fi

log()      { printf "%b\n" "${1-}"; }
info()     { log "${CYN}ℹ︎${RST} $*"; }
good()     { log "${GRN}✔${RST} $*"; }
warn()     { log "${YLW}⚠${RST} $*"; }
die()      { log "${RED}✖${RST} $*"; exit 1; }

### ────────────────────────────────────────────────────────────────────────────
### Repo sanity
### ────────────────────────────────────────────────────────────────────────────
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

PROJECT_NAME="AutoPost"
SRC_MAIN_JAVA="src/main/java"
SRC_MAIN_RES="src/main/resources"
PACKAGE_DIR="$SRC_MAIN_JAVA/com/autopost"
CONFIG_DIR="$PACKAGE_DIR/config"

good "Starting ${BOLD}${PROJECT_NAME}${RST} setup at ${BOLD}$ROOT_DIR${RST}"

### ────────────────────────────────────────────────────────────────────────────
### Tool checks
### ────────────────────────────────────────────────────────────────────────────
need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Missing required tool: ${BOLD}$1${RST}"
}

need_cmd bash
need_cmd grep
need_cmd awk
need_cmd sed

if command -v java >/dev/null 2>&1; then
  JAVA_VER_RAW="$(java -version 2>&1 | head -n1)"
  JAVA_VER="$(echo "$JAVA_VER_RAW" | sed -E 's/.*version "([0-9]+).*/\1/')" || true
  if [[ -z "${JAVA_VER:-}" || "$JAVA_VER" -lt 17 ]]; then
    die "Java 17+ required. Found: ${JAVA_VER_RAW}"
  fi
  good "Java OK → ${DIM}${JAVA_VER_RAW}${RST}"
else
  die "Java not found. Install Temurin 17+ or set JAVA_HOME."
fi

# Gradle wrapper is the norm for Spring Boot repos; check it.
if [[ ! -x "./gradlew" ]]; then
  warn "gradlew missing; attempting to bootstrap Gradle wrapper…"
  need_cmd gradle
  gradle wrapper || die "Could not create gradle wrapper"
fi
chmod +x ./gradlew

### ────────────────────────────────────────────────────────────────────────────
### Environment & defaults
### ────────────────────────────────────────────────────────────────────────────
AUTO_POST_CRON_DEFAULT="0 5 0 * * *"
AUTO_POST_CRON_VAL="${AUTO_POST_CRON:-$AUTO_POST_CRON_DEFAULT}"

info "Using cron: ${BOLD}${AUTO_POST_CRON_VAL}${RST}"

### ────────────────────────────────────────────────────────────────────────────
### Cron validation (basic)
### ────────────────────────────────────────────────────────────────────────────
# Spring Cron: sec min hour day month dayOfWeek
# Example: 0 5 0 * * *  (00:05 UTC daily)
validate_cron() {
  local expr="$1"
  # Very loose validation: 6 space-separated fields with valid chars
  if ! echo "$expr" | grep -Eq '^[0-9*/,\-?]+[[:space:]]+[0-9*/,\-?]+[[:space:]]+[0-9*/,\-?]+[[:space:]]+([0-9*/,\-?LWC#]+)[[:space:]]+([0-9*/,\-?]+)[[:space:]]+([0-9*/,\-?LWC#]+)$'; then
    die "Cron looks invalid for Spring format: ${BOLD}$expr${RST}"
  fi
}
validate_cron "$AUTO_POST_CRON_VAL"
good "Cron expression looks valid."

### ────────────────────────────────────────────────────────────────────────────
### .gitignore cleanup
### ────────────────────────────────────────────────────────────────────────────
GITIGNORE_FILE=".gitignore"
if [[ -f "$GITIGNORE_FILE" ]]; then
  if ! grep -q '^application-\*\.yml$' "$GITIGNORE_FILE"; then
    echo "application-*.yml" >> "$GITIGNORE_FILE"
    info "Added to .gitignore: application-*.yml"
  fi
  if ! grep -q '^application-\*\.properties$' "$GITIGNORE_FILE"; then
    echo "application-*.properties" >> "$GITIGNORE_FILE"
    info "Added to .gitignore: application-*.properties"
  fi
  if ! grep -q '^classpath\.txt$' "$GITIGNORE_FILE"; then
    echo "classpath.txt" >> "$GITIGNORE_FILE"
    info "Added to .gitignore: classpath.txt"
  fi
else
  info "No .gitignore found; creating a sane default."
  cat > "$GITIGNORE_FILE" <<'IG'
# Java / Gradle
/build/
/out/
/.gradle/
*.iml
*.class

# IDE
.idea/
.vscode/

# Spring Boot
application-*.yml
application-*.properties

# Misc build artifacts
classpath.txt
IG
fi

### ────────────────────────────────────────────────────────────────────────────
### Ensure package structure
### ────────────────────────────────────────────────────────────────────────────
mkdir -p "$CONFIG_DIR"
mkdir -p "$SRC_MAIN_RES"

### ────────────────────────────────────────────────────────────────────────────
### Write ScheduleValidationConfig.java (idempotent)
### ────────────────────────────────────────────────────────────────────────────
SVC_FILE="$CONFIG_DIR/ScheduleValidationConfig.java"
cat > "$SVC_FILE" <<'JAVA'
package com.autopost.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.support.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ScheduleValidationConfig {

    private static final Logger log = LoggerFactory.getLogger(ScheduleValidationConfig.class);

    @Value("${autopost.cron}")
    private String cronExpression;

    @PostConstruct
    public void validateCron() {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }
        log.info("✅ Valid cron expression: {}", cronExpression);
    }
}
JAVA
good "Wrote ${BOLD}$SVC_FILE${RST}"

### ────────────────────────────────────────────────────────────────────────────
### Ensure application.properties with env-driven cron
### ────────────────────────────────────────────────────────────────────────────
APP_PROPS="$SRC_MAIN_RES/application.properties"

# If file exists, patch/replace autopost.cron line. Otherwise create fresh.
if [[ -f "$APP_PROPS" ]]; then
  if grep -q '^autopost\.cron=' "$APP_PROPS"; then
    sed -i.bak "s|^autopost\.cron=.*$|autopost.cron=${AUTO_POST_CRON_VAL}|g" "$APP_PROPS"
    rm -f "${APP_PROPS}.bak"
  else
    printf "\nautopost.cron=%s\n" "$AUTO_POST_CRON_VAL" >> "$APP_PROPS"
  fi
  good "Patched ${BOLD}autopost.cron${RST} in ${BOLD}$APP_PROPS${RST}"
else
  cat > "$APP_PROPS" <<PROPS
# === AutoPost application properties ===
# Cron format (Spring): sec min hour day-of-month month day-of-week
autopost.cron=${AUTO_POST_CRON_VAL}

# Example additional props you might have:
# server.port=8080
# logging.level.root=INFO
PROPS
  good "Created ${BOLD}$APP_PROPS${RST}"
fi

### ────────────────────────────────────────────────────────────────────────────
### Gradle build
### ────────────────────────────────────────────────────────────────────────────
GRADLE_CMD=( "./gradlew" "--no-daemon" "--stacktrace" )

if [[ "${SKIP_TESTS:-0}" == "1" ]]; then
  info "Building WITHOUT tests (SKIP_TESTS=1)…"
  "${GRADLE_CMD[@]}" clean bootJar -x test
else
  info "Building WITH tests… (set SKIP_TESTS=1 to skip)"
  "${GRADLE_CMD[@]}" clean test bootJar
fi

# Find the jar
JAR_PATH="$(ls -1 build/libs/*-SNAPSHOT.jar 2>/dev/null || true)"
if [[ -z "$JAR_PATH" ]]; then
  # fallback to any jar
  JAR_PATH="$(ls -1 build/libs/*.jar 2>/dev/null || true)"
fi
[[ -n "$JAR_PATH" ]] || die "Build finished but no jar found in build/libs."

good "Build OK → ${BOLD}$JAR_PATH${RST}"

### ────────────────────────────────────────────────────────────────────────────
### Summary
### ────────────────────────────────────────────────────────────────────────────
cat <<SUM

${GRN}All set.${RST} What I did:
  • Validated tools (Java 17+, Gradle wrapper)
  • Ensured .gitignore has Spring Boot patterns
  • Created/updated ${BOLD}ScheduleValidationConfig.java${RST} for cron validation
  • Set ${BOLD}autopost.cron=${AUTO_POST_CRON_VAL}${RST} in ${BOLD}$APP_PROPS${RST}
  • Built the app → ${BOLD}$JAR_PATH${RST}

Next steps:
  1) Run locally:
       ${BOLD}java -jar "$JAR_PATH"${RST}
  2) In CI, call this script from your workflow:
       - name: Setup AutoPost
         run: bash scripts/setup-everything.sh
  3) Change the schedule? Re-run with:
       ${BOLD}AUTO_POST_CRON="0 0 18 * * *" bash scripts/setup-everything.sh${RST}

If CI was failing due to an overlong YAML block, this script keeps the big logic in a file,
so your workflow stays tiny and safe. You’re welcome. 🔥
SUM
