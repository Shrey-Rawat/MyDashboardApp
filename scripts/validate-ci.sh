#!/bin/bash

# CI/CD Validation Script
# This script helps validate the CI/CD setup locally before pushing to GitHub

set -e  # Exit on error

echo "🔍 Validating CI/CD Setup for Best Productivity App"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}➤${NC} $1"
}

print_success() {
    echo -e "${GREEN}✅${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC}  $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

# Check if we're in the project root
if [[ ! -f "app/build.gradle.kts" ]]; then
    print_error "Not in project root directory. Please run from the root of MyDashboardApp."
    exit 1
fi

print_success "Project structure validated"

# 1. Validate GitHub Actions workflows
print_status "Validating GitHub Actions workflows..."

WORKFLOWS_DIR=".github/workflows"
REQUIRED_WORKFLOWS=("build.yml" "test.yml" "lint.yml" "release.yml")

for workflow in "${REQUIRED_WORKFLOWS[@]}"; do
    if [[ -f "$WORKFLOWS_DIR/$workflow" ]]; then
        print_success "Found $workflow"
        
        # Basic YAML syntax validation (if yq is available)
        if command -v yq >/dev/null 2>&1; then
            if yq eval '.' "$WORKFLOWS_DIR/$workflow" >/dev/null 2>&1; then
                print_success "  YAML syntax valid"
            else
                print_error "  YAML syntax invalid in $workflow"
                exit 1
            fi
        fi
    else
        print_error "Missing required workflow: $workflow"
        exit 1
    fi
done

# 2. Check if gradlew is executable
print_status "Checking Gradle wrapper..."
if [[ -x "./gradlew" ]]; then
    print_success "gradlew is executable"
else
    print_warning "gradlew is not executable, fixing..."
    chmod +x ./gradlew
    print_success "Fixed gradlew permissions"
fi

# 3. Validate Gradle build files
print_status "Validating Gradle build files..."
if ./gradlew help >/dev/null 2>&1; then
    print_success "Gradle configuration is valid"
else
    print_error "Gradle configuration has issues"
    exit 1
fi

# 4. Check for required configuration files
print_status "Checking configuration files..."

CONFIG_FILES=(
    "config/detekt/detekt.yml"
    "config/dependency-check-suppressions.xml"
    "fastlane/metadata/android/en-US/changelogs/default.txt"
)

for config_file in "${CONFIG_FILES[@]}"; do
    if [[ -f "$config_file" ]]; then
        print_success "Found $config_file"
    else
        print_warning "Missing optional config file: $config_file"
    fi
done

# 5. Validate build variants
print_status "Checking build variants..."
VARIANTS=("freeDebug" "freeRelease" "proDebug" "proRelease")

echo "Available build variants:"
for variant in "${VARIANTS[@]}"; do
    if ./gradlew tasks --all | grep -q "assemble$variant"; then
        print_success "  $variant"
    else
        print_error "  Missing variant: $variant"
        exit 1
    fi
done

# 6. Run quick build test
print_status "Running quick build test..."
if ./gradlew assembleDebug -q; then
    print_success "Debug build successful"
else
    print_error "Debug build failed"
    exit 1
fi

# 7. Run quick test
print_status "Running quick unit tests..."
if ./gradlew testFreeDebugUnitTest -q; then
    print_success "Unit tests passed"
else
    print_warning "Some unit tests failed (check details with './gradlew test')"
fi

# 8. Run lint checks
print_status "Running lint checks..."
if ./gradlew detekt -q; then
    print_success "Detekt passed"
else
    print_warning "Detekt found issues (check reports/detekt/)"
fi

# 9. Check for secrets in code (basic check)
print_status "Checking for potential secrets..."
SECRET_PATTERNS=("password" "api[_-]key" "secret" "token" "private[_-]key")
SECRETS_FOUND=0

for pattern in "${SECRET_PATTERNS[@]}"; do
    if find . -name "*.kt" -o -name "*.java" -o -name "*.xml" | \
       xargs grep -l -i "$pattern" | \
       grep -v build | grep -v ".git" >/dev/null 2>&1; then
        SECRETS_FOUND=1
        print_warning "Found potential secret pattern: $pattern"
    fi
done

if [[ $SECRETS_FOUND -eq 0 ]]; then
    print_success "No obvious secrets found in source code"
fi

# 10. Validate release signing setup (just structure, not actual secrets)
print_status "Validating release signing setup..."
if grep -q "signingConfigs" app/build.gradle.kts; then
    print_success "Signing configuration found in build.gradle.kts"
else
    print_error "No signing configuration found"
    exit 1
fi

# 11. Check GitHub Actions runners compatibility
print_status "Checking GitHub Actions compatibility..."
if [[ -f ".github/workflows/build.yml" ]]; then
    # Check if we're using supported Java version
    if grep -q "java-version: '17'" .github/workflows/build.yml; then
        print_success "Using supported Java 17"
    else
        print_warning "Java version might not be optimal"
    fi
    
    # Check if we're using supported Gradle version
    if grep -q "gradle-version: '8.2'" .github/workflows/build.yml; then
        print_success "Using supported Gradle 8.2"
    else
        print_warning "Gradle version might not be optimal"
    fi
fi

# 12. Documentation check
print_status "Checking documentation..."
if [[ -f ".github/README.md" ]]; then
    print_success "CI/CD documentation found"
else
    print_warning "Missing CI/CD documentation"
fi

# Summary
echo ""
echo "🎉 CI/CD Validation Complete!"
echo "=============================="

print_success "All critical validations passed"
echo ""
echo "Next steps:"
echo "1. Set up required GitHub Secrets (see .github/README.md)"
echo "2. Create a test release tag to validate the full pipeline"
echo "3. Monitor first workflow runs and adjust as needed"
echo ""

print_status "To test a specific workflow locally:"
echo "  • Build: ./gradlew assembleRelease"
echo "  • Test: ./gradlew test"
echo "  • Lint: ./gradlew detekt ktlintCheck"
echo ""

print_status "To create a test release:"
echo "  git tag v1.0.0-test"
echo "  git push origin v1.0.0-test"
echo ""

exit 0
