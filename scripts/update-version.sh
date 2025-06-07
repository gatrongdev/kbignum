#!/bin/bash

# KBigNum Version Update Script
# Usage: ./scripts/update-version.sh <new-version>
# Example: ./scripts/update-version.sh 1.0.1

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

# Function to validate semantic versioning
validate_semver() {
    local version=$1
    if [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.-]+)?(\+[a-zA-Z0-9.-]+)?$ ]]; then
        return 0
    else
        return 1
    fi
}

# Function to compare versions
version_gt() {
    test "$(printf '%s\n' "$@" | sort -V | head -n 1)" != "$1"
}

# Main script
main() {
    # Check if version argument is provided
    if [ $# -eq 0 ]; then
        print_error "Version number is required"
        echo "Usage: $0 <new-version>"
        echo "Example: $0 1.0.1"
        exit 1
    fi

    NEW_VERSION=$1
    print_info "Updating KBigNum version to $NEW_VERSION"

    # Validate semantic versioning
    if ! validate_semver "$NEW_VERSION"; then
        print_error "Invalid version format. Please use semantic versioning (e.g., 1.0.0, 1.0.0-alpha.1)"
        exit 1
    fi

    # Get current version
    CURRENT_VERSION=$(grep 'version = ' shared/build.gradle.kts | head -1 | sed 's/.*version = "\(.*\)".*/\1/')
    print_info "Current version: $CURRENT_VERSION"

    # Check if new version is greater than current
    if ! version_gt "$NEW_VERSION" "$CURRENT_VERSION"; then
        print_warning "New version ($NEW_VERSION) should be greater than current version ($CURRENT_VERSION)"
        read -p "Continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Version update cancelled"
            exit 0
        fi
    fi

    # Update version in shared/build.gradle.kts
    print_info "Updating shared/build.gradle.kts..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/version = \".*\"/version = \"$NEW_VERSION\"/" shared/build.gradle.kts
    else
        # Linux
        sed -i "s/version = \".*\"/version = \"$NEW_VERSION\"/" shared/build.gradle.kts
    fi

    # Update version in README.md
    print_info "Updating README.md..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/io\.github\.gatrongdev:kbignum:[0-9]\+\.[0-9]\+\.[0-9]\+/io.github.gatrongdev:kbignum:$NEW_VERSION/g" README.md
        sed -i '' "s/'io\.github\.gatrongdev:kbignum:[0-9]\+\.[0-9]\+\.[0-9]\+'/'io.github.gatrongdev:kbignum:$NEW_VERSION'/g" README.md
    else
        # Linux
        sed -i "s/io\.github\.gatrongdev:kbignum:[0-9]\+\.[0-9]\+\.[0-9]\+/io.github.gatrongdev:kbignum:$NEW_VERSION/g" README.md
        sed -i "s/'io\.github\.gatrongdev:kbignum:[0-9]\+\.[0-9]\+\.[0-9]\+'/'io.github.gatrongdev:kbignum:$NEW_VERSION'/g" README.md
    fi

    # Verify changes
    NEW_VERSION_CHECK=$(grep 'version = ' shared/build.gradle.kts | head -1 | sed 's/.*version = "\(.*\)".*/\1/')
    if [ "$NEW_VERSION_CHECK" = "$NEW_VERSION" ]; then
        print_success "Version successfully updated to $NEW_VERSION"
    else
        print_error "Failed to update version"
        exit 1
    fi

    # Show git diff
    print_info "Changes made:"
    git diff --no-index /dev/null <(echo "Files changed:") 2>/dev/null || true
    git diff --name-only

    echo
    print_success "Version update completed!"
    echo
    print_info "Next steps:"
    echo "  1. Review the changes: git diff"
    echo "  2. Test the build: ./gradlew build test"
    echo "  3. Commit changes: git add . && git commit -m \"bump: version $NEW_VERSION\""
    echo "  4. Push to main: git push origin main"
    echo "  5. Release will be created automatically by GitHub Actions"
    echo
    print_warning "Note: Make sure to follow conventional commit format for automatic changelog generation"
    echo "Examples:"
    echo "  - feat: add new feature"
    echo "  - fix: resolve bug"
    echo "  - docs: update documentation"
    echo "  - bump: version $NEW_VERSION"
}

# Run main function
main "$@"