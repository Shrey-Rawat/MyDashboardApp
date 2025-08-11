#!/bin/bash

# Simple and Reliable Android Testing with Podman
# Uses a more straightforward Android-in-container approach

set -e

APP_NAME="MyDashboardApp"
APK_PATH="app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk"
CONTAINER_NAME="android_simple_test"

echo "🤖 Simple Android Testing with Podman"
echo "======================================"

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

# Create directory for sharing files
mkdir -p /tmp/android_simple_test
cp "$APK_PATH" /tmp/android_simple_test/app.apk

echo "📱 Starting Android container with Android-x86..."
echo "This provides a full Android system running in the container"

# Try different publicly available Android container images
echo "🔍 Trying publicly available Android container images..."

# First try: budtmo/docker-android (most reliable)
if podman run -d \
    --name $CONTAINER_NAME \
    --privileged \
    -p 5900:5900 \
    -p 5555:5555 \
    -p 6080:6080 \
    -v /tmp/android_simple_test:/data:Z \
    -e EMULATOR_DEVICE="Samsung Galaxy S10" \
    -e WEB_VNC=true \
    docker.io/budtmo/docker-android:emulator_11.0; then
    
    echo "✅ Using budtmo/docker-android container"
    CONTAINER_TYPE="budtmo"
    
elif podman run -d \
    --name $CONTAINER_NAME \
    --privileged \
    -p 5900:5900 \
    -p 5555:5555 \
    -p 4723:4723 \
    -v /tmp/android_simple_test:/data:Z \
    docker.io/appium/appium:latest; then
    
    echo "✅ Using appium container"
    CONTAINER_TYPE="appium"
    
else
    echo "❌ Failed to start any Android container"
    echo "Available alternatives:"
    echo "  1. Use physical device: ./test_on_device.sh"
    echo "  2. Manual APK install from ~/Desktop/"
    echo "  3. Try wireless download from HTTP server"
    exit 1
fi

echo "⏳ Waiting for Android to start..."
sleep 30

# Check if container is running
if ! podman ps --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
    echo "❌ Container failed to start. Trying alternative approach..."
    podman rm $CONTAINER_NAME 2>/dev/null || true
    
    # Try with a simpler Android emulator container
    echo "📱 Trying with Android emulator container..."
    podman run -d \
        --name $CONTAINER_NAME \
        --privileged \
        -p 5900:5900 \
        -p 5555:5555 \
        -p 6080:6080 \
        -v /tmp/android_simple_test:/data:Z \
        -e EMULATOR_DEVICE="Nexus 5" \
        -e WEB_VNC=true \
        docker.io/appium/appium:v2.1.3-p0
        
    sleep 20
fi

# Wait for Android to boot
echo "⏳ Waiting for Android system to be ready..."
for i in {1..30}; do
    if podman ps --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
        echo "✅ Container is running (attempt $i/30)"
        break
    fi
    echo "⏳ Still starting container... ($i/30)"
    sleep 10
done

echo "🔌 Attempting to connect with ADB..."

# Try different ADB connection methods
for port in 5555 5554; do
    echo "Trying ADB connection on port $port..."
    adb connect localhost:$port && break
    sleep 5
done

# Give ADB some time to establish connection
sleep 10

# Check ADB connection
if adb devices | grep -q "emulator\|localhost"; then
    echo "✅ ADB connected successfully!"
    DEVICE=$(adb devices | grep -E "(emulator|localhost)" | awk '{print $1}' | head -1)
    
    # Install APK
    echo "📲 Installing APK on device: $DEVICE"
    if adb -s "$DEVICE" install -r /shared/app.apk 2>/dev/null || \
       adb -s "$DEVICE" push /shared/app.apk /data/local/tmp/ && \
       adb -s "$DEVICE" shell pm install -r /data/local/tmp/app.apk; then
        echo "✅ APK installed successfully!"
        
        # Launch the app
        echo "🎯 Launching $APP_NAME..."
        adb -s "$DEVICE" shell am start -n com.mydashboardapp.pro.firebase.debug/com.mydashboardapp.MainActivity
        
        echo ""
        echo "🎉 Android Test Environment Ready!"
        echo "=================================="
        echo "📱 Device: $DEVICE"
        echo "🔧 VNC: localhost:5900 (if available)"
        echo "🌐 Web VNC: http://localhost:6080 (if available)"
        
    else
        echo "❌ Failed to install APK"
    fi
else
    echo "❌ ADB connection failed"
    echo "Container logs:"
    podman logs --tail 20 $CONTAINER_NAME
fi

echo ""
echo "🔧 Container Management:"
echo "   View logs: podman logs $CONTAINER_NAME"
echo "   Stop: podman stop $CONTAINER_NAME"
echo "   Remove: podman rm $CONTAINER_NAME"
echo ""
echo "📊 Sentry Dashboard: https://shray.sentry.io/projects/mydashboardapp/"
