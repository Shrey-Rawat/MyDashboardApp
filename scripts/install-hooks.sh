#!/bin/bash
#
# Script to install Git hooks for the MyDashboardApp project
#

echo "Installing Git hooks..."

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
#
# Pre-commit Git hook for ktlint formatting
#

echo "Running ktlint..."

# Check if gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "Warning: gradlew not found. Skipping ktlint check."
    echo "Make sure to run './gradlew ktlintCheck' manually before committing."
    exit 0
fi

# Make gradle wrapper executable
chmod +x ./gradlew

# Run ktlint check
./gradlew ktlintCheck

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ ktlint check failed!"
    echo ""
    echo "Please fix the formatting issues above or run:"
    echo "  ./gradlew ktlintFormat"
    echo ""
    echo "Then stage your changes and commit again."
    exit 1
fi

echo "✅ ktlint check passed!"
exit 0
EOF

# Create commit-msg hook
cat > .git/hooks/commit-msg << 'EOF'
#!/bin/sh
#
# Git hook for conventional commit message linting
#

# Read the commit message
commit_regex='^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo ""
    echo "❌ Invalid commit message format!"
    echo ""
    echo "Commit message should follow Conventional Commits format:"
    echo "  <type>(<scope>): <description>"
    echo ""
    echo "Types:"
    echo "  feat:     A new feature"
    echo "  fix:      A bug fix"
    echo "  docs:     Documentation only changes"
    echo "  style:    Changes that do not affect the meaning of the code"
    echo "  refactor: A code change that neither fixes a bug nor adds a feature"
    echo "  test:     Adding missing tests or correcting existing tests"
    echo "  chore:    Changes to the build process or auxiliary tools"
    echo "  perf:     A code change that improves performance"
    echo "  ci:       Changes to CI configuration files and scripts"
    echo "  build:    Changes that affect the build system or external dependencies"
    echo "  revert:   Reverts a previous commit"
    echo ""
    echo "Examples:"
    echo "  feat: add user authentication"
    echo "  feat(auth): add login functionality"
    echo "  fix: resolve crash on startup"
    echo "  docs: update README installation steps"
    echo ""
    exit 1
fi

echo "✅ Commit message format is valid!"
exit 0
EOF

# Make hooks executable
chmod +x .git/hooks/pre-commit .git/hooks/commit-msg

echo "✅ Git hooks installed successfully!"
echo ""
echo "Hooks installed:"
echo "  - pre-commit: Runs ktlint formatting check"
echo "  - commit-msg: Validates conventional commit message format"
echo ""
echo "To bypass hooks temporarily, use: git commit --no-verify"
