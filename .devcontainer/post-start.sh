#!/bin/bash
set -euo pipefail

# Post-start script for AutoPost devcontainer
# This script runs after the container starts and provides guidance on secrets setup

echo "🚀 AutoPost DevContainer started!"
echo ""

# Check if this is the first run or if secrets might be missing
if [[ ! -f ~/.autopost-setup-complete ]]; then
    echo "📋 SETUP INSTRUCTIONS"
    echo "===================="
    echo ""
    echo "This is your first time running the AutoPost devcontainer."
    echo "To use the application, you need to configure the following environment variables"
    echo "as Codespaces secrets (they are NOT available during container build):"
    echo ""
    
    echo "🔑 REQUIRED SECRETS:"
    echo "  • OPENAI_API_KEY - Your OpenAI API key"
    echo "  • X_API_KEY - Twitter/X API key"
    echo "  • X_API_SECRET - Twitter/X API secret"
    echo "  • X_ACCESS_TOKEN - Twitter/X access token"
    echo "  • X_ACCESS_TOKEN_SECRET - Twitter/X access token secret"
    echo "  • GOOGLE_SERVICE_ACCOUNT_JSON - Google service account JSON (as a single line)"
    echo "  • GOOGLE_RAW_FOLDER_ID - Google Drive RAW folder ID"
    echo "  • GOOGLE_EDITS_FOLDER_ID - Google Drive EDITS folder ID"
    echo ""
    
    echo "🔧 OPTIONAL SECRETS:"
    echo "  • ANTHROPIC_API_KEY - Anthropic API key (optional)"
    echo "  • GROK_API_KEY - Grok API key (optional)"
    echo "  • X_BEARER_TOKEN - Twitter/X Bearer token (optional)"
    echo "  • GOOGLE_CLIENT_ID - Google OAuth client ID (optional)"
    echo "  • GOOGLE_CLIENT_SECRET - Google OAuth client secret (optional)"
    echo "  • AUTO_POST_CRON - Posting schedule in UTC (default: '0 0 9 * * *')"
    echo "  • MAX_HASHTAGS - Maximum hashtags per post (default: 3)"
    echo "  • POST_ONE_TEASER_PER_DAY - One teaser per day (default: true)"
    echo "  • WEBHOOK_URL - Webhook URL for posting fallback"
    echo ""
    
    echo "📚 HOW TO ADD SECRETS:"
    echo "  1. Go to your GitHub Codespaces settings"
    echo "  2. Add each secret with repository access to 'AutoPost'"
    echo "  3. STOP and RESTART this Codespace to load new secrets"
    echo "  4. Secrets are only available at runtime, not during build"
    echo ""
    
    echo "⚠️  IMPORTANT: After adding/updating secrets, you MUST restart the Codespace!"
    echo ""
    
    # Mark setup as shown
    touch ~/.autopost-setup-complete
fi

# Check for environment variables and provide status
echo "🔍 ENVIRONMENT STATUS"
echo "===================="

# Function to check if env var exists and is not empty
check_env() {
    local var_name="$1"
    local required="$2"
    
    if [[ -n "${!var_name:-}" ]]; then
        echo "  ✅ $var_name - Available"
    elif [[ "$required" == "true" ]]; then
        echo "  ❌ $var_name - MISSING (required)"
        return 1
    else
        echo "  ⚪ $var_name - Not set (optional)"
    fi
    return 0
}

missing_required=0

# Check required variables
check_env "OPENAI_API_KEY" "true" || ((missing_required++))
check_env "X_API_KEY" "true" || ((missing_required++))
check_env "X_API_SECRET" "true" || ((missing_required++))
check_env "X_ACCESS_TOKEN" "true" || ((missing_required++))
check_env "X_ACCESS_TOKEN_SECRET" "true" || ((missing_required++))
check_env "GOOGLE_SERVICE_ACCOUNT_JSON" "true" || ((missing_required++))
check_env "GOOGLE_RAW_FOLDER_ID" "true" || ((missing_required++))
check_env "GOOGLE_EDITS_FOLDER_ID" "true" || ((missing_required++))

echo ""

# Check optional variables
check_env "ANTHROPIC_API_KEY" "false" || true
check_env "GROK_API_KEY" "false" || true
check_env "X_BEARER_TOKEN" "false" || true
check_env "GOOGLE_CLIENT_ID" "false" || true
check_env "GOOGLE_CLIENT_SECRET" "false" || true
check_env "AUTO_POST_CRON" "false" || true
check_env "MAX_HASHTAGS" "false" || true
check_env "POST_ONE_TEASER_PER_DAY" "false" || true
check_env "WEBHOOK_URL" "false" || true

echo ""

if [[ $missing_required -gt 0 ]]; then
    echo "⚠️  $missing_required required environment variable(s) missing!"
    echo "   The application will fail to start until these are provided."
    echo "   Add them as Codespaces secrets and restart the container."
else
    echo "✅ All required environment variables are available!"
    echo "   You can now build and run the application."
fi

echo ""
echo "🛠️  QUICK COMMANDS"
echo "=================="
echo "  • Build: ./mvnw clean package"
echo "  • Run CLI: java -jar target/autopost.jar"
echo "  • Run Server: java -jar target/autopost.jar server"
echo "  • Run Analysis: java -jar target/autopost.jar analyze"
echo "  • Run Tests: ./mvnw test"
echo "  • Format Code: ./mvnw spotless:apply"
echo "  • Quality Check: ./mvnw spotless:check spotbugs:check checkstyle:check"
echo ""

# Make sure Maven wrapper is executable
chmod +x mvnw 2>/dev/null || true

echo "Happy coding! 🎉"