#!/bin/bash

# Android App Testing with Podman - Optimized for CachyOS
# Uses a lightweight approach with Android SDK container

set -e

APP_NAME="MyDashboardApp"
APK_PATH="app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk"
CONTAINER_NAME="android_test_env"
NETWORK_NAME="android_test_net"

echo "🐧 Android App Testing with Podman"
echo "==================================="

# Check if APK exists
if [[ ! -f "$APK_PATH" ]]; then
    echo "❌ APK not found at $APK_PATH"
    echo "Please build the app first with: ./gradlew assembleProFirebaseDebug"
    exit 1
fi

echo "✅ APK found: $APK_PATH"

# Clean up any existing containers and networks
echo "🧹 Cleaning up existing containers..."
podman stop $CONTAINER_NAME 2>/dev/null || true
podman rm $CONTAINER_NAME 2>/dev/null || true
podman network rm $NETWORK_NAME 2>/dev/null || true

# Create a custom network
echo "🌐 Creating Podman network..."
podman network create $NETWORK_NAME

# Create directory for sharing files
mkdir -p /tmp/android_test_podman
cp "$APK_PATH" /tmp/android_test_podman/app.apk
chmod 644 /tmp/android_test_podman/app.apk

echo "📱 Starting Android test environment with Podman..."
echo "This will download the container image on first run..."

# Try different lightweight Android approaches
echo "🔍 Trying lightweight Android containers..."

if podman run -d \
    --name $CONTAINER_NAME \
    --network $NETWORK_NAME \
    --privileged \
    -p 5555:5555 \
    -p 6080:6080 \
    -v /tmp/android_test_podman:/data/local/tmp:Z \
    -e EMULATOR_DEVICE="Pixel 4" \
    -e WEB_VNC=true \
    docker.io/budtmo/docker-android:emulator_11.0; then
    
    echo "✅ Using budtmo/docker-android container"
    CONTAINER_TYPE="budtmo"
    
elif podman run -d \
    --name $CONTAINER_NAME \
    --network $NETWORK_NAME \
    --privileged \
    -p 5555:5555 \
    -v /tmp/android_test_podman:/workspace:Z \
    docker.io/thyrlian/android-sdk:latest \
    tail -f /dev/null; then
    
    echo "✅ Using thyrlian/android-sdk container (SDK only)"
    CONTAINER_TYPE="sdk"
    
else
    echo "❌ Failed to start any Android container"
    echo "📱 Alternative testing options:"
    echo "   ./test_on_device.sh - Use your physical Android device"
    echo "   Manual install from ~/Desktop/MyDashboardApp-SentryDemo.apk"
    exit 1
fi

echo "⏳ Waiting for Android container to initialize..."
sleep 20

# Check if container is running
if ! podman ps --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
    echo "❌ Container failed to start. Checking logs..."
    podman logs $CONTAINER_NAME
    exit 1
fi

echo "🔍 Waiting for Android system to boot..."
for i in {1..30}; do
    if podman exec $CONTAINER_NAME getprop sys.boot_completed 2>/dev/null | grep -q "1"; then
        echo "✅ Android system is ready!"
        break
    fi
    echo "⏳ Still booting... ($i/30)"
    sleep 10
done

# Connect via ADB
echo "🔌 Connecting to Android via ADB..."
adb connect localhost:5555 || {
    echo "❌ Failed to connect via ADB. Installing ADB..."
    if command -v pacman >/dev/null; then
        sudo pacman -S --noconfirm android-tools
    else
        echo "Please install android-tools package for your distribution"
        exit 1
    fi
    adb connect localhost:5555
}

# Wait for device
echo "⏳ Waiting for ADB connection..."
timeout 60 adb -s localhost:5555 wait-for-device

# Install APK
echo "📲 Installing $APP_NAME APK..."
if adb -s localhost:5555 install -r /data/local/tmp/app.apk; then
    echo "✅ APK installed successfully!"
else
    echo "❌ Failed to install APK. Trying to push and install manually..."
    adb -s localhost:5555 push /tmp/android_test_podman/app.apk /data/local/tmp/
    if adb -s localhost:5555 shell pm install -r /data/local/tmp/app.apk; then
        echo "✅ APK installed successfully via shell!"
    else
        echo "❌ APK installation failed"
        exit 1
    fi
fi

# Launch the app
echo "🎯 Launching $APP_NAME..."
adb -s localhost:5555 shell am start -n com.mydashboardapp.pro.firebase.debug/com.mydashboardapp.MainActivity

# Get container IP for VNC access
CONTAINER_IP=$(podman inspect $CONTAINER_NAME --format '{{.NetworkSettings.Networks.android_test_net.IPAddress}}')

echo ""
echo "🎉 Android Test Environment Ready!"
echo "=================================="
echo ""
echo "📱 Access Methods:"
echo "   ADB: adb -s localhost:5555 shell"
echo "   Container IP: $CONTAINER_IP"
echo ""
echo "🔧 Useful commands:"
echo "   View logs: podman logs $CONTAINER_NAME"
echo "   App logs: adb -s localhost:5555 logcat | grep -i sentry"
echo "   Screenshot: adb -s localhost:5555 shell screencap -p /sdcard/screen.png"
echo "   Stop: podman stop $CONTAINER_NAME"
echo "   Remove: podman rm $CONTAINER_NAME && podman network rm $NETWORK_NAME"
echo ""
echo "🎯 Test Sentry Integration:"
echo "   The app should now be running in the Android container"
echo "   Use ADB commands to interact with the device"
echo "   Check your Sentry dashboard for events"
echo ""
echo "📊 Sentry Dashboard: https://shray.sentry.io/projects/mydashboardapp/"
