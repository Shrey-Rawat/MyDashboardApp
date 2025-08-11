#!/bin/bash

# Android App Testing Script with Podman
# This script sets up an Android emulator in a container and installs the APK

set -e

APP_NAME="MyDashboardApp"
APK_PATH="app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk"
CONTAINER_NAME="android_emulator_test"

echo "🚀 Starting Android App Test Environment"
echo "========================================"

# Check if APK exists
if [[ ! -f "$APK_PATH" ]]; then
    echo "❌ APK not found at $APK_PATH"
    echo "Please build the app first with: ./gradlew assembleProFirebaseDebug"
    exit 1
fi

echo "✅ APK found: $APK_PATH"

# Clean up any existing container
echo "🧹 Cleaning up existing containers..."
podman stop $CONTAINER_NAME 2>/dev/null || true
podman rm $CONTAINER_NAME 2>/dev/null || true

# Create directory for sharing files
mkdir -p /tmp/android_test
cp "$APK_PATH" /tmp/android_test/app.apk

echo "📱 Starting Android emulator container..."
echo "This will take a few minutes on first run..."

# Use budtmo's docker-android which works well with Podman
podman run -d \
    --name $CONTAINER_NAME \
    --privileged \
    -p 5554:5554 \
    -p 5555:5555 \
    -p 6080:6080 \
    -v /tmp/android_test:/data/app:Z \
    -e EMULATOR_DEVICE="Samsung Galaxy S10" \
    -e WEB_VNC=true \
    docker.io/budtmo/docker-android:emulator_11.0

echo "⏳ Waiting for emulator to boot..."
sleep 30

# Wait for emulator to be ready
echo "🔍 Checking emulator status..."
for i in {1..20}; do
    if podman exec $CONTAINER_NAME adb devices | grep -q "emulator"; then
        echo "✅ Emulator is ready!"
        break
    fi
    echo "⏳ Still booting... ($i/20)"
    sleep 15
done

# Install the APK
echo "📲 Installing $APP_NAME APK..."
if podman exec $CONTAINER_NAME adb install -r /data/app/app.apk; then
    echo "✅ APK installed successfully!"
else
    echo "❌ Failed to install APK"
    exit 1
fi

# Launch the app
echo "🎯 Launching $APP_NAME..."
podman exec $CONTAINER_NAME adb shell am start -n com.mydashboardapp.pro.firebase.debug/com.mydashboardapp.MainActivity

echo ""
echo "🎉 Android Test Environment Ready!"
echo "=================================="
echo ""
echo "📱 Access the Android emulator through:"
echo "   Web VNC: http://localhost:6080"
echo "   (No password required)"
echo ""
echo "🔧 Useful commands:"
echo "   View logs: podman exec $CONTAINER_NAME adb logcat"
echo "   Stop container: podman stop $CONTAINER_NAME"
echo "   Remove container: podman rm $CONTAINER_NAME"
echo ""
echo "🎯 Test Sentry Integration:"
echo "   1. Open the app in the emulator"
echo "   2. Click the test buttons to trigger Sentry events"
echo "   3. Check your Sentry dashboard for the events"
echo ""
echo "📊 Sentry Dashboard: https://shray.sentry.io/projects/mydashboardapp/"
