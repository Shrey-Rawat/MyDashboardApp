#!/bin/bash

# Script to migrate package names from mydashboardapp to mydashboardapp

echo "🔄 Starting package migration from com.mydashboardapp to com.mydashboardapp..."

# Find all relevant files and update package references
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.gradle" -o -name "*.gradle.kts" -o -name "*.pro" -o -name "*.proto" -o -name "*.md" -o -name "*.sh" -o -name "*.yml" -o -name "*.yaml" \) \
  -not -path "./.git/*" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec sed -i 's/com\.mydashboardapp/com.mydashboardapp/g' {} \;

echo "✅ Updated package references in source files"

# Update any remaining MyDashboardApp references to MyDashboardApp
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.gradle" -o -name "*.gradle.kts" -o -name "*.pro" -o -name "*.proto" -o -name "*.md" -o -name "*.sh" -o -name "*.yml" -o -name "*.yaml" \) \
  -not -path "./.git/*" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec sed -i 's/MyDashboardApp/MyDashboardApp/g' {} \;

echo "✅ Updated app name references"

# Update mydashboardapp to mydashboardapp (lowercase)
find . -type f \( -name "*.kt" -o -name "*.java" -o -name "*.gradle" -o -name "*.gradle.kts" -o -name "*.pro" -o -name "*.proto" -o -name "*.md" -o -name "*.sh" -o -name "*.yml" -o -name "*.yaml" \) \
  -not -path "./.git/*" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec sed -i 's/mydashboardapp/mydashboardapp/g' {} \;

echo "✅ Updated lowercase package references"

echo "🎉 Package migration completed!"
echo ""
echo "Next steps:"
echo "1. Move source files to new package directory structure"
echo "2. Update AndroidManifest.xml"
echo "3. Clean and rebuild project"
