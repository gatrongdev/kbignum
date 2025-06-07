#!/bin/bash

# KBigNum Workflow Validation Script
# This script validates that all workflows and builds are working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }

echo "🔍 KBigNum Workflow Validation"
echo "=============================="
echo ""

# Test 1: Basic build
print_info "Testing basic build..."
if ./gradlew build --quiet; then
    print_success "Build completed successfully"
else
    print_error "Build failed"
    exit 1
fi

# Test 2: Run tests
print_info "Testing unit tests..."
if ./gradlew test --quiet; then
    print_success "All tests passed"
else
    print_error "Tests failed"
    exit 1
fi

# Test 3: Code quality checks
print_info "Testing code quality checks..."
if ./gradlew runAllChecks --quiet; then
    print_success "Code quality checks passed"
else
    print_error "Code quality checks failed"
    exit 1
fi

# Test 4: Documentation generation
print_info "Testing documentation generation..."
if ./gradlew dokkaHtml --quiet; then
    print_success "Documentation generated successfully"
else
    print_error "Documentation generation failed"
    exit 1
fi

# Test 5: Specific multiplatform compile tasks
print_info "Testing multiplatform compile tasks..."
if ./gradlew shared:compileCommonMainKotlinMetadata shared:compileDebugKotlinAndroid --quiet; then
    print_success "Multiplatform compile tasks completed"
else
    print_error "Multiplatform compile tasks failed"
    exit 1
fi

# Test 6: Check workflow files syntax
print_info "Validating workflow files..."
WORKFLOW_ERRORS=0

for workflow in .github/workflows/*.yml; do
    if [ -f "$workflow" ]; then
        # Basic YAML syntax check
        if ! python3 -c "import yaml; yaml.safe_load(open('$workflow'))" 2>/dev/null; then
            print_error "Invalid YAML syntax in $workflow"
            WORKFLOW_ERRORS=$((WORKFLOW_ERRORS + 1))
        fi
    fi
done

if [ $WORKFLOW_ERRORS -eq 0 ]; then
    print_success "All workflow files have valid syntax"
else
    print_error "$WORKFLOW_ERRORS workflow files have syntax errors"
    exit 1
fi

# Test 7: Check for required files
print_info "Checking required files..."
REQUIRED_FILES=(
    ".github/workflows/ci.yml"
    ".github/workflows/release.yml"
    ".github/workflows/version-check.yml"
    ".github/dependabot.yml"
    ".github/CONTRIBUTING.md"
    ".github/SECURITY.md"
    ".github/PULL_REQUEST_TEMPLATE.md"
    "scripts/update-version.sh"
    "README.md"
    "LICENSE"
)

MISSING_FILES=0
for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        print_error "Missing required file: $file"
        MISSING_FILES=$((MISSING_FILES + 1))
    fi
done

if [ $MISSING_FILES -eq 0 ]; then
    print_success "All required files are present"
else
    print_error "$MISSING_FILES required files are missing"
    exit 1
fi

# Test 8: Version detection
print_info "Testing version detection..."
CURRENT_VERSION=$(grep 'version = ' shared/build.gradle.kts | head -1 | sed 's/.*version = "\(.*\)".*/\1/')
if [[ $CURRENT_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.-]+)?(\+[a-zA-Z0-9.-]+)?$ ]]; then
    print_success "Version format is valid: $CURRENT_VERSION"
else
    print_error "Invalid version format: $CURRENT_VERSION"
    exit 1
fi

echo ""
print_success "🎉 All workflow validations passed!"
echo ""
print_info "Summary:"
echo "  ✅ Build and tests work correctly"
echo "  ✅ Code quality checks pass"
echo "  ✅ Documentation generation works"
echo "  ✅ Multiplatform tasks function properly"
echo "  ✅ Workflow files are valid"
echo "  ✅ All required files are present"
echo "  ✅ Version format is correct"
echo ""
print_success "The project is ready for CI/CD automation!"