#!/bin/bash

# Setup Migration Tools Script
# Creates all necessary scripts and documentation for future Android project migrations

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_status "Setting up migration tools in: $PROJECT_ROOT"

# Create scripts directory if it doesn't exist
mkdir -p "$SCRIPT_DIR"

# Make all scripts executable
chmod +x "$SCRIPT_DIR"/*.sh

# Create migration documentation
cat > "$PROJECT_ROOT/docs/MIGRATION_GUIDE.md" << 'EOF'
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

EOF

print_success "Created migration guide: docs/MIGRATION_GUIDE.md"

# Create video recording documentation
cat > "$PROJECT_ROOT/docs/VIDEO_RECORDING_GUIDE.md" << 'EOF'
# Android App Video Recording Guide

## Overview
Comprehensive guide for recording professional product demo videos of Android applications.

## Prerequisites
- Android device connected via USB with debugging enabled
- ADB (Android Debug Bridge) installed
- FFmpeg installed for video processing
- App built and installable APK available

## Quick Start

### 1. Prepare Device
```bash
# Check connected devices
adb devices

# Install your app
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Record Video
```bash
# Run automated recording script
./scripts/record_product_video.sh [DEVICE_ID] [OUTPUT_DIR]

# Or manually specify:
./scripts/record_product_video.sh 1234567890ABCDEF ~/Videos/MyApp
```

### 3. Review Output
The script generates:
- Individual screenshots (PNG files)
- Combined video (MP4 file)
- Recording log with timestamps
- Summary report

## Manual Recording Process

If you prefer manual control:

### 1. Clear App Data
```bash
adb shell pm clear com.yourpackage.debug
```

### 2. Launch and Screenshot
```bash
# Launch app
adb shell monkey -p com.yourpackage.debug -c android.intent.category.LAUNCHER 1

# Take screenshots
adb shell screencap -p > screenshot_01.png

# Tap screen
adb shell input tap 500 1000

# Navigate and repeat...
```

### 3. Create Video
```bash
ffmpeg -framerate 2 -pattern_type glob -i "*.png" \
    -c:v libx264 -r 30 -pix_fmt yuv420p \
    -vf "scale=1080:1920:force_original_aspect_ratio=decrease" \
    output_video.mp4
```

## Recording Best Practices

### Screen Interaction
- **Wait 2-3 seconds** between taps for smooth transitions
- **Use consistent timing** for professional look
- **Test coordinates** on your specific device first
- **Handle different screen sizes** with adaptive coordinates

### Video Quality
- **Use 1080x1920 resolution** for mobile-optimized videos
- **30 FPS frame rate** for smooth playback
- **H.264 codec** for broad compatibility
- **2-3 seconds per screen** for comfortable viewing

### Content Strategy
1. **Splash screen** (2 seconds)
2. **Onboarding flow** (3-4 seconds per screen)
3. **Main dashboard** (5 seconds)
4. **Key features** (3-5 seconds each)
5. **Settings/configuration** (2-3 seconds)

## Troubleshooting

### Device Connection Issues
```bash
# Check USB debugging is enabled
adb devices

# Restart ADB server
adb kill-server && adb start-server

# Check device authorization
adb shell echo "Device connected"
```

### Screenshot Issues
```bash
# Check permissions
adb shell screencap -p > test.png && echo "Screenshots working"

# Alternative screenshot method
adb exec-out screencap -p > screenshot.png
```

### Video Generation Issues
```bash
# Check FFmpeg installation
ffmpeg -version

# Test with single image
ffmpeg -loop 1 -i image.png -t 3 -c:v libx264 test.mp4

# Verify image format
file *.png
```

### App Launch Issues
```bash
# Check package name
adb shell pm list packages | grep mydashboard

# Check if app is installed
adb shell pm path com.mydashboardapp.pro.debug

# Manual launch
adb shell am start -n com.mydashboardapp.pro.debug/.SplashActivity
```

## Advanced Features

### Custom Interaction Scripts
Create app-specific interaction sequences:

```bash
# Example: E-commerce app demo
tap_screen 500 800 "Product category"
sleep 2
tap_screen 600 600 "Specific product"
sleep 3
tap_screen 900 1600 "Add to cart"
sleep 2
```

### Multiple Device Recording
Record on different screen sizes:

```bash
# Phone recording
./record_product_video.sh phone_device_id ~/Videos/phone_demo

# Tablet recording  
./record_product_video.sh tablet_device_id ~/Videos/tablet_demo
```

### Professional Editing
Post-process videos for marketing:

```bash
# Add fade transitions
ffmpeg -i input.mp4 -vf "fade=in:0:30,fade=out:st=57:d=3" output_with_fades.mp4

# Add text overlay
ffmpeg -i input.mp4 -vf "drawtext=text='My Dashboard App':fontsize=60:x=50:y=100" output_with_text.mp4

# Combine multiple videos
ffmpeg -f concat -i video_list.txt -c copy final_demo.mp4
```

EOF

print_success "Created video recording guide: docs/VIDEO_RECORDING_GUIDE.md"

print_status "Making scripts executable..."
find "$SCRIPT_DIR" -name "*.sh" -exec chmod +x {} \;

print_success "✅ Migration tools setup completed!"
print_status "Available scripts:"
echo "  📄 scripts/migrate_android_project.sh - Full project migration"
echo "  🎬 scripts/record_product_video.sh - Automated video recording"
echo "  📚 docs/MIGRATION_GUIDE.md - Detailed migration documentation"
echo "  🎥 docs/VIDEO_RECORDING_GUIDE.md - Video recording documentation"

print_status "Usage examples:"
echo "  ./scripts/migrate_android_project.sh ~/Code/Source ~/Code/Target com.old com.new \"OldName\" \"NewName\""
echo "  ./scripts/record_product_video.sh [device_id] [output_dir]"
