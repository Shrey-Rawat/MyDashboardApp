#!/bin/bash

set -e

echo "🚀 Best Productivity App - Test Script"
echo "======================================"

# Build the app
echo "📦 Building the app..."
./gradlew assembleFreeFirebaseDebug

APK_PATH="app/build/outputs/apk/freeFirebase/debug/app-free-firebase-debug.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK not found at $APK_PATH"
    exit 1
fi

echo "✅ APK built successfully: $APK_PATH"

# Check for connected devices
echo ""
echo "🔍 Checking for connected devices..."
adb devices -l

DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)

if [ $DEVICES -eq 0 ]; then
    echo ""
    echo "⚠️  No devices connected. Please:"
    echo "   1. Connect your physical device with USB debugging enabled"
    echo "   2. Or start an Android emulator"
    echo "   3. Or use one of our container testing scripts:"
    echo "      - ./android_simple_test.sh"
    echo "      - ./android_vnc_podman.sh"
    echo ""
    echo "APK location: $APK_PATH"
    exit 0
fi

# Install the app
echo ""
echo "📱 Installing the app..."
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ App installed successfully!"
    
    # Launch the app
    echo ""
    echo "🚀 Launching the app..."
    adb shell am start -n com.mydashboardapp.debug.free/.MainActivity
    
    if [ $? -eq 0 ]; then
        echo "✅ App launched successfully!"
        echo ""
        echo "📱 Your app should now be running on your device/emulator."
        echo "🐛 To view logs, run: adb logcat | grep MyDashboardApp"
        echo "🔄 To reinstall, run: $0"
    else
        echo "❌ Failed to launch the app"
        exit 1
    fi
else
    echo "❌ Failed to install the app"
    exit 1
fi
