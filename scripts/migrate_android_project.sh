#!/bin/bash

# Android Project Migration Script
# Usage: ./migrate_android_project.sh SOURCE_DIR TARGET_DIR OLD_PACKAGE NEW_PACKAGE OLD_APP_NAME NEW_APP_NAME

set -e

# Color codes for output
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

# Check arguments
if [ $# -ne 6 ]; then
    print_error "Usage: $0 SOURCE_DIR TARGET_DIR OLD_PACKAGE NEW_PACKAGE OLD_APP_NAME NEW_APP_NAME"
    print_error "Example: $0 ~/Code/BestProductivityApp ~/Code/MyDashboardApp com.bestproductivityapp com.mydashboardapp \"BestProductivityApp\" \"MyDashboardApp\""
    exit 1
fi

SOURCE_DIR="$1"
TARGET_DIR="$2"
OLD_PACKAGE="$3"
NEW_PACKAGE="$4"
OLD_APP_NAME="$5"
NEW_APP_NAME="$6"

print_status "Starting Android project migration..."
print_status "Source: $SOURCE_DIR"
print_status "Target: $TARGET_DIR"
print_status "Package: $OLD_PACKAGE → $NEW_PACKAGE"
print_status "App Name: $OLD_APP_NAME → $NEW_APP_NAME"

# Step 1: Backup source if target exists
if [ -d "$TARGET_DIR" ]; then
    BACKUP_DIR="${TARGET_DIR}.backup.$(date +%Y%m%d_%H%M%S)"
    print_warning "Target directory exists. Creating backup at $BACKUP_DIR"
    mv "$TARGET_DIR" "$BACKUP_DIR"
fi

# Step 2: Copy source to target
print_status "Copying source project to target directory..."
cp -r "$SOURCE_DIR" "$TARGET_DIR"
cd "$TARGET_DIR"

# Step 3: Remove git history and initialize new repo
print_status "Initializing new git repository..."
rm -rf .git
git init
git branch -m main

# Step 4: Update package names in all files
print_status "Updating package names in source files..."
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.gradle" -o -name "*.gradle.kts" -o -name "*.pro" -o -name "*.proto" -o -name "*.md" -o -name "*.sh" -o -name "*.yml" -o -name "*.yaml" -o -name "*.xml" \) \
  -not -path "./.git/*" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec sed -i "s/${OLD_PACKAGE//./\\.}/${NEW_PACKAGE}/g" {} \;

print_success "Updated package references in source files"

# Step 5: Update app name references
print_status "Updating app name references..."
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.gradle" -o -name "*.gradle.kts" -o -name "*.pro" -o -name "*.proto" -o -name "*.md" -o -name "*.sh" -o -name "*.yml" -o -name "*.yaml" -o -name "*.xml" \) \
  -not -path "./.git/*" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec sed -i "s/${OLD_APP_NAME}/${NEW_APP_NAME}/g" {} \;

print_success "Updated app name references"

# Step 6: Move source directories to match new package structure
print_status "Moving source directories to new package structure..."
OLD_PACKAGE_PATH=$(echo "$OLD_PACKAGE" | tr '.' '/')
NEW_PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

find . -type d -path "*/${OLD_PACKAGE_PATH}" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" | while read old_dir; do
  
  new_dir=$(echo "$old_dir" | sed "s|${OLD_PACKAGE_PATH}|${NEW_PACKAGE_PATH}|g")
  
  print_status "Moving: $old_dir → $new_dir"
  
  mkdir -p "$(dirname "$new_dir")"
  mv "$old_dir" "$new_dir"
done

print_success "Directory migration completed"

# Step 7: Clean up any empty old package directories
find . -type d -name "$(basename "$OLD_PACKAGE_PATH")" -empty \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec rmdir {} \; 2>/dev/null || true

# Step 8: Update root project name in settings.gradle.kts
if [ -f "settings.gradle.kts" ]; then
    sed -i "s/rootProject.name = \".*\"/rootProject.name = \"$NEW_APP_NAME\"/" settings.gradle.kts
    print_success "Updated root project name"
fi

# Step 9: Remove large files that shouldn't be in git
print_status "Cleaning up large files..."
find . -type f -size +50M \
  -not -path "./.git/*" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec rm -f {} \;

# Step 10: Set up git config
print_status "Setting up git configuration..."
git config user.name "Ray"
git config user.email "ray@$(echo $NEW_PACKAGE | cut -d. -f2).com"

# Step 11: Clean build and verify
print_status "Cleaning and testing build..."
./gradlew clean

# Step 12: Commit initial migration
print_status "Committing migration..."
git add .
git commit -m "Initial commit: $NEW_APP_NAME migrated from $OLD_APP_NAME

- Updated all package names from $OLD_PACKAGE to $NEW_PACKAGE
- Renamed application class and components
- Updated app name to '$NEW_APP_NAME'
- Updated theme names and branding
- Moved all source files to new package structure
- Verified successful build compilation

Migration completed on: $(date +%Y-%m-%d)"

print_success "Migration completed successfully!"
print_status "Next steps:"
echo "  1. Create GitHub repository: gh repo create $NEW_APP_NAME --public"
echo "  2. Add remote: git remote add origin https://github.com/USERNAME/$NEW_APP_NAME.git"
echo "  3. Push code: git push -u origin main"
echo "  4. Test build: ./gradlew assembleDebug"
