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

