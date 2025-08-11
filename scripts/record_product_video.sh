#!/bin/bash

# Android App Product Video Recording Script
# Usage: ./record_product_video.sh [DEVICE_ID] [OUTPUT_DIR]

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

# Default values
DEVICE_ID=${1:-$(adb devices | grep -v "List" | head -n1 | cut -f1)}
OUTPUT_DIR=${2:-"~/Pictures/Development/ProductVideo_$(date +%Y%m%d_%H%M%S)"}
PACKAGE_NAME="com.mydashboardapp.pro.debug"
APP_NAME="MyDashboardApp"

# Expand tilde in output directory
OUTPUT_DIR=$(eval echo "$OUTPUT_DIR")

print_status "=== Android App Product Video Recording ==="
print_status "Device ID: $DEVICE_ID"
print_status "Output Directory: $OUTPUT_DIR"
print_status "Package Name: $PACKAGE_NAME"
print_status "App Name: $APP_NAME"

# Check if device is connected
if [ -z "$DEVICE_ID" ]; then
    print_error "No Android device found. Please connect a device and enable USB debugging."
    exit 1
fi

# Check if adb is available
if ! command -v adb &> /dev/null; then
    print_error "adb not found. Please install Android SDK platform-tools."
    exit 1
fi

# Check if ffmpeg is available
if ! command -v ffmpeg &> /dev/null; then
    print_error "ffmpeg not found. Please install ffmpeg for video processing."
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"
cd "$OUTPUT_DIR"

print_status "Created output directory: $OUTPUT_DIR"

# Function to take screenshot
take_screenshot() {
    local filename="$1"
    local description="$2"
    
    print_status "📸 Taking screenshot: $description"
    adb -s "$DEVICE_ID" shell screencap -p > "${filename}.png"
    
    if [ -s "${filename}.png" ]; then
        print_success "Screenshot saved: ${filename}.png"
        echo "$(date '+%H:%M:%S') - $description" >> recording_log.txt
    else
        print_error "Failed to capture screenshot: $filename"
    fi
}

# Function to tap screen coordinates
tap_screen() {
    local x="$1"
    local y="$2"
    local description="$3"
    
    print_status "👆 Tapping: $description (${x}, ${y})"
    adb -s "$DEVICE_ID" shell input tap "$x" "$y"
    sleep 2  # Wait for UI response
}

# Function to clear app data
clear_app_data() {
    print_status "🧹 Clearing app data for fresh start..."
    adb -s "$DEVICE_ID" shell pm clear "$PACKAGE_NAME" 2>/dev/null || {
        print_warning "Could not clear app data via adb. Please clear manually in device settings."
        read -p "Press Enter after manually clearing app data..."
    }
}

# Function to launch app
launch_app() {
    print_status "🚀 Launching app..."
    adb -s "$DEVICE_ID" shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
    sleep 3  # Wait for app to load
}

# Function to go back
go_back() {
    print_status "⬅️ Going back..."
    adb -s "$DEVICE_ID" shell input keyevent KEYCODE_BACK
    sleep 1
}

# Main recording workflow
print_status "Starting product video recording workflow..."

# Step 1: Clear app data for fresh start
clear_app_data

# Step 2: Launch app and start recording
launch_app
sleep 2

# Step 3: Record onboarding flow
print_status "🎬 Recording onboarding flow..."

SCREEN_COUNT=1

# Capture initial splash/onboarding screen
take_screenshot "$(printf "%02d_splash" $SCREEN_COUNT)" "Splash Screen"
((SCREEN_COUNT++))

# Navigate through onboarding screens
for i in {1..6}; do
    sleep 2
    take_screenshot "$(printf "%02d_onboarding_%d" $SCREEN_COUNT $i)" "Onboarding Screen $i"
    
    # Try to tap "Next" button (approximate coordinates for most devices)
    tap_screen 900 1800 "Next button"
    ((SCREEN_COUNT++))
done

# Step 4: Capture main app screen
sleep 3
take_screenshot "$(printf "%02d_main_app" $SCREEN_COUNT)" "Main App Dashboard"
((SCREEN_COUNT++))

# Step 5: Navigate through bottom tabs
declare -a tabs=("Finance" "Nutrition" "Training" "AI" "Settings")
declare -a tab_coords=("200 1900" "400 1900" "600 1900" "800 1900" "1000 1900")

for i in "${!tabs[@]}"; do
    tap_screen ${tab_coords[$i]} "${tabs[$i]} tab"
    sleep 2
    take_screenshot "$(printf "%02d_%s_tab" $SCREEN_COUNT "${tabs[$i],,}")" "${tabs[$i]} Tab Screen"
    ((SCREEN_COUNT++))
done

# Step 6: Test some interactions (if applicable)
print_status "🎯 Testing app interactions..."

# Go back to main screen
tap_screen 200 1900 "Home tab"
sleep 2

# Test menu or settings access
tap_screen 1000 1900 "Settings tab"
sleep 2
take_screenshot "$(printf "%02d_settings_detail" $SCREEN_COUNT)" "Settings Detail Screen"
((SCREEN_COUNT++))

# Step 7: Create video from screenshots
print_status "🎬 Creating product video from screenshots..."

# Create a video from the screenshots
ffmpeg -y \
    -framerate 2 \
    -pattern_type glob \
    -i "*.png" \
    -c:v libx264 \
    -r 30 \
    -pix_fmt yuv420p \
    -vf "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2" \
    "${APP_NAME}_ProductDemo_$(date +%Y%m%d_%H%M%S).mp4"

print_success "Product video created successfully!"

# Step 8: Generate summary report
cat > "RECORDING_SUMMARY.md" << EOF
# $APP_NAME Product Video Recording Summary

## Recording Details
- **Date**: $(date '+%Y-%m-%d %H:%M:%S')
- **Device**: $DEVICE_ID
- **Package**: $PACKAGE_NAME
- **Screenshots**: $((SCREEN_COUNT-1)) captured
- **Output Directory**: $OUTPUT_DIR

## Workflow Completed
1. ✅ App data cleared for fresh start
2. ✅ App launched and splash screen captured
3. ✅ Onboarding flow recorded (6 screens)
4. ✅ Main dashboard screen captured
5. ✅ Bottom navigation tabs tested (${#tabs[@]} tabs)
6. ✅ Settings and detail screens captured
7. ✅ Product video generated

## Files Generated
- Screenshots: \`*.png\`
- Video: \`${APP_NAME}_ProductDemo_*.mp4\`
- Log: \`recording_log.txt\`
- Summary: \`RECORDING_SUMMARY.md\`

## Next Steps
1. Review the generated video
2. Edit or enhance as needed
3. Use for product demos, marketing, or documentation

---
*Generated by record_product_video.sh*
EOF

print_success "Recording summary generated: RECORDING_SUMMARY.md"

# Step 9: Show final results
print_status "📊 Final Results:"
echo "  📁 Directory: $OUTPUT_DIR"
echo "  📸 Screenshots: $((SCREEN_COUNT-1))"
echo "  🎬 Video: $(ls *.mp4 2>/dev/null | head -n1 || echo 'Generation failed')"
echo "  📝 Log: recording_log.txt"
echo "  📋 Summary: RECORDING_SUMMARY.md"

print_success "Product video recording workflow completed!"
print_status "Check your files in: $OUTPUT_DIR"
