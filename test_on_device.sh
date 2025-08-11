#!/bin/bash

# Manual Android Device Testing
# Works with any Android device connected via USB or network

set -e

APP_NAME="MyDashboardApp"
APK_PATH="app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk"
PACKAGE_NAME="com.mydashboardapp.pro.firebase.debug"
ACTIVITY_NAME="com.mydashboardapp.MainActivity"

echo "📱 Manual Android Device Testing"
echo "================================="

# Check if APK exists
if [[ ! -f "$APK_PATH" ]]; then
    echo "❌ APK not found at $APK_PATH"
    echo "Please build the app first with: ./gradlew assembleProFirebaseDebug"
    exit 1
fi

echo "✅ APK found: $APK_PATH ($(du -h "$APK_PATH" | cut -f1))"

# Check ADB connectivity
echo ""
echo "🔌 Checking ADB connectivity..."
if ! command -v adb >/dev/null; then
    echo "❌ ADB not found. Installing..."
    sudo pacman -S --noconfirm android-tools
fi

# List connected devices
echo "📱 Connected Android devices:"
adb devices -l

DEVICE_COUNT=$(adb devices | grep -c "device$" 2>/dev/null || echo "0")

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo ""
    echo "❌ No Android devices found!"
    echo ""
    echo "📋 Options to connect a device:"
    echo "   1. 📱 USB Device:"
    echo "      - Enable 'Developer Options' on your Android device"
    echo "      - Enable 'USB Debugging'"
    echo "      - Connect via USB cable"
    echo "      - Allow USB debugging when prompted"
    echo ""
    echo "   2. 🌐 Wireless ADB (Android 11+):"
    echo "      - Enable 'Developer Options'"
    echo "      - Enable 'Wireless debugging'"
    echo "      - Connect to same WiFi network"
    echo "      - Run: adb connect [DEVICE_IP]:5555"
    echo ""
    echo "   3. 🖥️  Android Emulator:"
    echo "      - Install Android Studio"
    echo "      - Create and start an AVD"
    echo "      - Run this script again"
    echo ""
    echo "   4. 📦 Use containerized approach:"
    echo "      - Run: ./android_simple_test.sh"
    echo ""
    exit 1
fi

echo "✅ Found $DEVICE_COUNT Android device(s)"

# If multiple devices, let user choose
if [ "$DEVICE_COUNT" -gt 1 ]; then
    echo ""
    echo "📱 Multiple devices found. Please choose:"
    adb devices | grep "device$" | nl -n ln
    echo ""
    read -p "Enter device number (1-$DEVICE_COUNT): " DEVICE_NUM
    DEVICE_SERIAL=$(adb devices | grep "device$" | sed -n "${DEVICE_NUM}p" | awk '{print $1}')
    ADB_OPTS="-s $DEVICE_SERIAL"
    echo "Selected device: $DEVICE_SERIAL"
else
    ADB_OPTS=""
    DEVICE_SERIAL=$(adb devices | grep "device$" | awk '{print $1}' | head -1)
fi

echo ""
echo "🔍 Device Information:"
echo "   Serial: $DEVICE_SERIAL"
echo "   Model: $(adb $ADB_OPTS shell getprop ro.product.model 2>/dev/null || echo 'Unknown')"
echo "   Android Version: $(adb $ADB_OPTS shell getprop ro.build.version.release 2>/dev/null || echo 'Unknown')"
echo "   API Level: $(adb $ADB_OPTS shell getprop ro.build.version.sdk 2>/dev/null || echo 'Unknown')"

echo ""
echo "📲 Installing APK..."
if adb $ADB_OPTS install -r "$APK_PATH"; then
    echo "✅ APK installed successfully!"
else
    echo "❌ APK installation failed. Trying alternative method..."
    if adb $ADB_OPTS push "$APK_PATH" /data/local/tmp/app.apk && \
       adb $ADB_OPTS shell pm install -r /data/local/tmp/app.apk; then
        echo "✅ APK installed via push method!"
    else
        echo "❌ APK installation failed completely"
        exit 1
    fi
fi

echo ""
echo "🎯 Launching $APP_NAME..."
if adb $ADB_OPTS shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME"; then
    echo "✅ App launched successfully!"
else
    echo "❌ Failed to launch app. Checking if it's installed..."
    if adb $ADB_OPTS shell pm list packages | grep -q "$PACKAGE_NAME"; then
        echo "✅ App is installed but failed to launch"
        echo "Try launching manually: 'Best Productivity App Pro'"
    else
        echo "❌ App not found in installed packages"
        exit 1
    fi
fi

echo ""
echo "🎉 Android Testing Ready!"
echo "========================="
echo ""
echo "📱 App should now be running on your device"
echo "🎯 Test the Sentry integration by clicking:"
echo "   🔴 'Trigger Test Exception' - Will crash app"
echo "   💬 'Send Test Message to Sentry' - Sends info message"
echo "   ⚠️  'Send Non-Fatal Exception' - Sends error without crash"
echo ""
echo "📊 Monitor events at: https://shray.sentry.io/projects/mydashboardapp/"
echo ""
echo "🔧 Useful commands:"
echo "   App logs: adb $ADB_OPTS logcat | grep -i bestproductivity"
echo "   Sentry logs: adb $ADB_OPTS logcat | grep -i sentry"
echo "   Uninstall: adb $ADB_OPTS uninstall $PACKAGE_NAME"
echo "   Screenshot: adb $ADB_OPTS shell screencap -p /sdcard/screenshot.png"
echo ""
echo "💡 If app crashes, that's expected for the 'Trigger Test Exception' button!"
