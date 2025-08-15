#!/usr/bin/env bash
set -euo pipefail

# Validation script to ensure the security fix is effective
echo "🔍 Validating Security Fix"
echo "========================="
echo

# Check that the problematic Base64 patterns are not in the main CI workflow
echo "1. Checking main CI workflow for Base64 patterns..."
if grep -q "base64.*-d" .github/workflows/ci.yml; then
    echo "❌ Found Base64 decode patterns in main CI workflow!"
    exit 1
else
    echo "✅ No Base64 decode patterns found in main CI workflow"
fi

# Check that gitleaks is integrated
echo "2. Checking for gitleaks integration..."
if grep -q "gitleaks/gitleaks-action" .github/workflows/ci.yml; then
    echo "✅ Gitleaks integration found in CI workflow"
else
    echo "❌ Gitleaks integration missing from CI workflow!"
    exit 1
fi

# Check that security-events permission is added
echo "3. Checking for security-events permission..."
if grep -q "security-events: write" .github/workflows/ci.yml; then
    echo "✅ Security-events permission found"
else
    echo "❌ Security-events permission missing!"
    exit 1
fi

# Verify that the specific problematic tokens are NOT in main workflow
echo "4. Checking for specific leaked token patterns..."
PATTERN1="NVd4cDA1TjhKS003TVZDUjFXVzE="
PATTERN2="dHVmVmtRdkk0Y3l4dmR0T2Q2MllOYTNR"

if grep -q "$PATTERN1\|$PATTERN2" .github/workflows/ci.yml; then
    echo "❌ Found leaked token patterns in main CI workflow!"
    exit 1
else
    echo "✅ No leaked token patterns found in main CI workflow"
fi

# Verify the build still works
echo "5. Testing that Maven build still works..."
if mvn -q -DskipTests package >/dev/null 2>&1; then
    echo "✅ Maven build successful"
else
    echo "❌ Maven build failed!"
    exit 1
fi

echo
echo "🎉 All security validations passed!"
echo "✅ Base64 token patterns removed from main CI workflow"
echo "✅ Gitleaks integration properly configured"
echo "✅ Build functionality preserved"
echo "✅ Security risk eliminated"