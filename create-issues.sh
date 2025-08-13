#!/bin/bash

# One-shot GitHub Issues creation script for AutoPost
# This script creates all the issues specified in issue #19

set -euo pipefail

# Configuration
REPO="tylerblakex-netizen/AutoPost"
USER_CARLOS="github-username-carlos"
USER_MEILIN="github-username-meilin"
USER_JAMAL="github-username-jamal"
USER_CHLOE="github-username-chloe"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if gh CLI is available and authenticated
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command -v gh &> /dev/null; then
        print_error "GitHub CLI (gh) is not installed. Please install it first."
        exit 1
    fi
    
    if ! gh auth status &> /dev/null; then
        print_error "GitHub CLI is not authenticated. Please run 'gh auth login' first."
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Function to create a single issue
create_issue() {
    local title="$1"
    local body="$2"
    local labels="$3"
    local assignee="$4"
    
    print_status "Creating issue: $title"
    
    # Build the gh issue create command
    local cmd="gh issue create -R \"$REPO\" -t \"$title\" -b \"$body\" -l \"$labels\""
    
    # Add assignee if provided
    if [[ -n "$assignee" ]]; then
        cmd="$cmd -a \"$assignee\""
    fi
    
    # Execute the command
    if eval "$cmd"; then
        print_success "Created issue: $title"
    else
        print_error "Failed to create issue: $title"
        return 1
    fi
}

# Function to create all issues
create_all_issues() {
    print_status "Starting to create GitHub issues for AutoPost..."
    
    # A1: Add Codespaces secrets for X + OpenAI
    create_issue \
        "A1: Add Codespaces secrets for X + OpenAI" \
        "Add Codespaces secrets \`X_BEARER_TOKEN\`, \`OPENAI_API_KEY\`. Due: 2025-08-13.
Refs: 00:02" \
        "security,secrets" \
        "$USER_CARLOS"
    
    # A2: Env-driven scheduling + AUTO_POST_CRON=18:00 UTC
    create_issue \
        "A2: Env-driven scheduling + AUTO_POST_CRON=18:00 UTC" \
        "Move scheduling to env only. Set \`AUTO_POST_CRON=18:00 UTC\` for next Friday. Due: 2025-08-15.
Refs: 00:05, 00:23, 00:26" \
        "scheduling,config" \
        ""
    
    # A3: Update .env.example + README (scheduling rules)
    create_issue \
        "A3: Update .env.example + README (scheduling rules)" \
        "Document required env vars and scheduling rules; open PR by Monday 17:00. Due: 2025-08-18.
Depends on: A2
Ref: 00:07" \
        "docs" \
        "$USER_MEILIN"
    
    # A4: Drive uploads — retries (3), jitter, 10s timeout
    create_issue \
        "A4: Drive uploads — retries (3), jitter, 10s timeout" \
        "Add reliability wrapper for Drive uploads. Due: 2025-08-20.
Ref: 00:10" \
        "reliability,drive" \
        "$USER_JAMAL"
    
    # A5: LLM prompt templates with guardrails
    create_issue \
        "A5: LLM prompt templates with guardrails" \
        "Create non-explicit, ≤3 hashtag guardrails; link templates in repo. Due: 2025-08-19.
Ref: 00:12" \
        "llm,safety" \
        "$USER_CHLOE"
    
    # A6: OAuth redirect URI from env + tests
    create_issue \
        "A6: OAuth redirect URI from env + tests" \
        "Make redirect URI configurable; add tests. Due: 2025-08-15.
Ref: 00:18–00:19" \
        "security,oauth,tests" \
        "$USER_CARLOS"
    
    # A7: Mask secrets in logs
    create_issue \
        "A7: Mask secrets in logs" \
        "Ensure no tokens/PII ever logged. Ref: 00:24" \
        "security,logging" \
        "$USER_JAMAL"
    
    # A8: Quick load test plan + run
    create_issue \
        "A8: Quick load test plan + run" \
        "Plan and execute a lightweight load test next week. Owner TBD.
Ref: 00:21" \
        "perf,testing" \
        ""
    
    # A9: Config-driven collaborator tagging
    create_issue \
        "A9: Config-driven collaborator tagging" \
        "Implement tagging via config (no hardcoded handles). Ref: 00:29" \
        "policy,config" \
        ""
    
    print_success "All GitHub issues have been created successfully!"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "One-shot GitHub Issues creation script for AutoPost"
    echo ""
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo "  -d, --dry-run  Show what would be created without actually creating issues"
    echo "  -r, --repo     Override repository (default: $REPO)"
    echo ""
    echo "Examples:"
    echo "  $0                    # Create all issues"
    echo "  $0 --dry-run          # Show what would be created"
    echo "  $0 --repo user/repo   # Use different repository"
}

# Function for dry run
dry_run() {
    print_warning "DRY RUN MODE - No issues will be created"
    echo ""
    echo "The following issues would be created in repository: $REPO"
    echo ""
    
    echo "1. A1: Add Codespaces secrets for X + OpenAI"
    echo "   Labels: security,secrets"
    echo "   Assignee: $USER_CARLOS"
    echo ""
    
    echo "2. A2: Env-driven scheduling + AUTO_POST_CRON=18:00 UTC"
    echo "   Labels: scheduling,config"
    echo "   Assignee: (none)"
    echo ""
    
    echo "3. A3: Update .env.example + README (scheduling rules)"
    echo "   Labels: docs"
    echo "   Assignee: $USER_MEILIN"
    echo ""
    
    echo "4. A4: Drive uploads — retries (3), jitter, 10s timeout"
    echo "   Labels: reliability,drive"
    echo "   Assignee: $USER_JAMAL"
    echo ""
    
    echo "5. A5: LLM prompt templates with guardrails"
    echo "   Labels: llm,safety"
    echo "   Assignee: $USER_CHLOE"
    echo ""
    
    echo "6. A6: OAuth redirect URI from env + tests"
    echo "   Labels: security,oauth,tests"
    echo "   Assignee: $USER_CARLOS"
    echo ""
    
    echo "7. A7: Mask secrets in logs"
    echo "   Labels: security,logging"
    echo "   Assignee: $USER_JAMAL"
    echo ""
    
    echo "8. A8: Quick load test plan + run"
    echo "   Labels: perf,testing"
    echo "   Assignee: (none)"
    echo ""
    
    echo "9. A9: Config-driven collaborator tagging"
    echo "   Labels: policy,config"
    echo "   Assignee: (none)"
    echo ""
    
    print_warning "To create these issues, run the script without --dry-run"
}

# Main script logic
main() {
    local dry_run_mode=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_usage
                exit 0
                ;;
            -d|--dry-run)
                dry_run_mode=true
                shift
                ;;
            -r|--repo)
                if [[ -z "${2:-}" ]]; then
                    print_error "Repository argument is required"
                    exit 1
                fi
                REPO="$2"
                shift 2
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Check prerequisites (except in dry run mode)
    if [[ "$dry_run_mode" == false ]]; then
        check_prerequisites
    fi
    
    # Execute based on mode
    if [[ "$dry_run_mode" == true ]]; then
        dry_run
    else
        create_all_issues
    fi
}

# Run the script
main "$@"