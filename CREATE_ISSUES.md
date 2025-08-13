# Create Issues Script

This script provides a one-shot solution for creating GitHub issues for the AutoPost project.

## create-issues.sh

A shell script that automatically creates all the GitHub issues specified in issue #19. The script creates 9 issues (A1-A9) related to various improvements and features for the AutoPost system.

### Prerequisites

- GitHub CLI (`gh`) must be installed
- You must be authenticated with GitHub CLI (`gh auth login`)
- Appropriate permissions to create issues in the target repository

### Usage

```bash
# Show help
./create-issues.sh --help

# Preview what would be created (dry run)
./create-issues.sh --dry-run

# Create all issues in the default repository
./create-issues.sh

# Create issues in a different repository
./create-issues.sh --repo username/repository
```

### Issues Created

The script creates the following issues:

1. **A1: Add Codespaces secrets for X + OpenAI** (security,secrets) - Assigned to github-username-carlos
2. **A2: Env-driven scheduling + AUTO_POST_CRON=18:00 UTC** (scheduling,config)
3. **A3: Update .env.example + README (scheduling rules)** (docs) - Assigned to github-username-meilin  
4. **A4: Drive uploads — retries (3), jitter, 10s timeout** (reliability,drive) - Assigned to github-username-jamal
5. **A5: LLM prompt templates with guardrails** (llm,safety) - Assigned to github-username-chloe
6. **A6: OAuth redirect URI from env + tests** (security,oauth,tests) - Assigned to github-username-carlos
7. **A7: Mask secrets in logs** (security,logging) - Assigned to github-username-jamal
8. **A8: Quick load test plan + run** (perf,testing)
9. **A9: Config-driven collaborator tagging** (policy,config)

### Security Features

- Uses `set -euo pipefail` for safe script execution
- Validates prerequisites before execution
- Provides colored output for better visibility
- Supports dry-run mode to preview changes
- Properly quotes variables to prevent injection

### Configuration

The script defines the following default values which can be modified:

- Repository: `tylerblakex-netizen/AutoPost`
- User placeholders: `github-username-carlos`, `github-username-meilin`, etc.

To customize for your environment, edit the configuration section at the top of the script.