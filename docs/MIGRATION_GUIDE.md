# Android Project Migration Guide

## Overview
This guide provides step-by-step instructions for migrating Android projects with different package names, app names, and GitHub repositories.

## Prerequisites
- Git installed and configured
- GitHub CLI (`gh`) installed and authenticated
- Android SDK platform-tools (for `adb`)
- FFmpeg (for video recording)

## Migration Process

### 1. Prepare Source Project
```bash
# Ensure all changes are committed
cd SOURCE_PROJECT
git add .
git commit -m "Save progress before migration"
git push
```

### 2. Run Migration Script
```bash
# Basic migration
./scripts/migrate_android_project.sh \
    ~/Code/SourceApp \
    ~/Code/TargetApp \
    com.sourcepackage \
    com.targetpackage \
    "SourceAppName" \
    "TargetAppName"
```

### 3. Create GitHub Repository
```bash
cd TARGET_PROJECT
gh repo create TargetAppName --public --description "Description here"
git remote add origin https://github.com/USERNAME/TargetAppName.git
git push -u origin main
```

### 4. Verify Migration
```bash
# Test build
./gradlew clean assembleDebug

# Run app on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

## What Gets Migrated

### Package Structure
- All `.kt`, `.java` files
- Package declarations
- Import statements
- Directory structure

### Build Configuration
- `build.gradle.kts` files
- `settings.gradle.kts`
- ProGuard rules
- Manifest files

### Resources
- String resources
- Theme names
- Drawable references
- Layout files

### Documentation
- README files
- Markdown documentation
- Script files

## Troubleshooting

### Large Files
If push fails due to large files:
```bash
# Remove large files and add to gitignore
rm large-file
echo "large-file" >> .gitignore
git add .
git commit -m "Remove large files"
```

### Build Errors
If build fails after migration:
```bash
# Clean build
./gradlew clean

# Check for remaining old package references
grep -r "old.package.name" . --exclude-dir=build --exclude-dir=.git
```

### Package Name Conflicts
If package names conflict:
```bash
# Update specific files manually
find . -name "*.kt" -exec sed -i 's/old.specific.reference/new.reference/g' {} \;
```

## Best Practices

1. **Always backup** your source project before migration
2. **Test thoroughly** after migration
3. **Update documentation** to reflect new names
4. **Create new Sentry/analytics projects** for separate tracking
5. **Update CI/CD configurations** if applicable

## Common Migration Scenarios

### Simple Rename
- Same functionality, different name
- Personal/company rebranding
- Open source vs commercial versions

### Feature Fork
- Split project into specialized versions
- Platform-specific variants
- Different target audiences

### Template Creation
- Create reusable project templates
- Remove specific business logic
- Generalize for multiple use cases

