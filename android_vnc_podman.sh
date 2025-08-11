#!/bin/bash

# Simple Android VNC Testing with Podman
# Uses VNC for GUI access - works great with Podman

set -e

APP_NAME="MyDashboardApp"
APK_PATH="app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk"
CONTAINER_NAME="android_vnc_test"

echo "🖥️ Android VNC Testing with Podman"
echo "===================================="

# Check if APK exists
if [[ ! -f "$APK_PATH" ]]; then
    echo "❌ APK not found at $APK_PATH"
    echo "Please build the app first with: ./gradlew assembleProFirebaseDebug"
    exit 1
fi

echo "✅ APK found: $APK_PATH"

# Clean up any existing containers
echo "🧹 Cleaning up existing containers..."
podman stop $CONTAINER_NAME 2>/dev/null || true
podman rm $CONTAINER_NAME 2>/dev/null || true

# Create directory for sharing APK
mkdir -p /tmp/android_vnc_test
cp "$APK_PATH" /tmp/android_vnc_test/app.apk

echo "📱 Starting Android VNC container..."
echo "This uses a lightweight Android-x86 image with VNC support"

# Run Android container with VNC - try multiple reliable images
echo "🔍 Trying reliable Android container images..."

if podman run -d \
    --name $CONTAINER_NAME \
    --privileged \
    -p 5900:5900 \
    -p 5555:5555 \
    -p 6080:6080 \
    -v /tmp/android_vnc_test:/shared:Z \
    -e DISPLAY_WIDTH=720 \
    -e DISPLAY_HEIGHT=1280 \
    -e WEB_VNC=true \
    docker.io/budtmo/docker-android:emulator_11.0; then
    
    echo "✅ Using budtmo/docker-android container"
    
elif podman run -d \
    --name $CONTAINER_NAME \
    --privileged \
    -p 5900:5900 \
    -p 5555:5555 \
    -v /tmp/android_vnc_test:/shared:Z \
    docker.io/thyrlian/android-sdk:latest \
    tail -f /dev/null; then
    
    echo "✅ Using thyrlian/android-sdk container (no emulator)"
    echo "⚠️  This provides SDK tools only, no GUI emulator"
    
else
    echo "❌ Failed to start Android container"
    echo "📱 Please use alternative testing methods:"
    echo "   ./test_on_device.sh - Physical device testing"
    echo "   Manual install from ~/Desktop/MyDashboardApp-SentryDemo.apk"
    exit 1
fi

echo "⏳ Waiting for Android emulator to start..."
sleep 30

# Wait for container to be ready
echo "🔍 Checking container status..."
for i in {1..20}; do
    if podman ps --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
        echo "✅ Container is running!"
        break
    fi
    echo "⏳ Still starting... ($i/20)"
    sleep 10
done

# Wait a bit more for Android to boot
echo "⏳ Waiting for Android to boot completely..."
sleep 60

# Install ADB if not present
if ! command -v adb >/dev/null; then
    echo "📦 Installing ADB (Android Debug Bridge)..."
    if command -v pacman >/dev/null; then
        sudo pacman -S --noconfirm android-tools
    else
        echo "❌ Please install android-tools for your distribution"
        exit 1
    fi
fi

# Connect to the Android device
echo "🔌 Connecting to Android emulator..."
adb connect localhost:5555

# Wait for device to be ready
echo "⏳ Waiting for ADB connection..."
timeout 120 adb -s localhost:5555 wait-for-device || {
    echo "❌ ADB connection timeout"
    echo "Container logs:"
    podman logs --tail 50 $CONTAINER_NAME
    exit 1
}

echo "✅ ADB connected successfully!"

# Copy APK to device and install
echo "📲 Installing APK..."
adb -s localhost:5555 push /tmp/android_vnc_test/app.apk /data/local/tmp/
if adb -s localhost:5555 shell pm install -r /data/local/tmp/app.apk; then
    echo "✅ APK installed successfully!"
else
    echo "❌ APK installation failed"
    adb -s localhost:5555 shell pm list packages | grep mydashboardapp || echo "App not found"
    exit 1
fi

# Launch the app
echo "🎯 Launching $APP_NAME..."
adb -s localhost:5555 shell am start -n com.mydashboardapp.pro.firebase.debug/com.mydashboardapp.MainActivity

# Start VNC viewer if available
if command -v vncviewer >/dev/null; then
    echo "🖥️ Starting VNC viewer..."
    vncviewer localhost:5900 &
elif command -v remmina >/dev/null; then
    echo "🖥️ You can connect with Remmina to localhost:5900"
fi

echo ""
echo "🎉 Android VNC Test Environment Ready!"
echo "======================================"
echo ""
echo "📱 Access the Android emulator:"
echo "   VNC: localhost:5900 (no password)"
echo "   ADB: adb -s localhost:5555 shell"
echo ""
echo "🖥️ VNC Clients you can use:"
echo "   - vncviewer localhost:5900"
echo "   - remmina (VNC to localhost:5900)"  
echo "   - Any VNC client connecting to localhost:5900"
echo ""
echo "🔧 Useful commands:"
echo "   Container logs: podman logs $CONTAINER_NAME"
echo "   App logs: adb -s localhost:5555 logcat | grep -i bestproductivity"
echo "   Sentry logs: adb -s localhost:5555 logcat | grep -i sentry"
echo "   Take screenshot: adb -s localhost:5555 shell screencap -p /sdcard/screenshot.png"
echo "   Stop container: podman stop $CONTAINER_NAME"
echo "   Remove: podman rm $CONTAINER_NAME"
echo ""
echo "🎯 Testing Instructions:"
echo "   1. Connect via VNC to see the Android desktop"
echo "   2. Find and open 'Best Productivity App Pro'"
echo "   3. Test the three Sentry buttons"
echo "   4. Check Sentry dashboard for events"
echo ""
echo "📊 Sentry Dashboard: https://shray.sentry.io/projects/mydashboardapp/"

# Keep script running to show logs
echo ""
echo "📋 Showing container logs (Ctrl+C to exit):"
echo "============================================"
podman logs -f $CONTAINER_NAME
