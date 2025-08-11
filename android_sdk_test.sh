#!/bin/bash

# Android SDK Testing with Podman
# Uses Android SDK container for ADB testing without full emulator

set -e

APP_NAME="MyDashboardApp"
APK_PATH="app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk"
CONTAINER_NAME="android_sdk_test"

echo "📦 Android SDK Testing with Podman"
echo "==================================="

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
mkdir -p /tmp/android_sdk_test
cp "$APK_PATH" /tmp/android_sdk_test/app.apk

echo "📱 Starting Android SDK container..."
echo "This provides Android SDK tools for APK management"

# Use a simple Ubuntu container with Android SDK
podman run -d \
    --name $CONTAINER_NAME \
    --privileged \
    -v /tmp/android_sdk_test:/workspace:Z \
    -w /workspace \
    docker.io/cimg/android:2023.08 \
    tail -f /dev/null

if ! podman ps --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
    echo "❌ Container failed to start. Trying alternative..."
    podman rm $CONTAINER_NAME 2>/dev/null || true
    
    # Try with a different base image
    podman run -d \
        --name $CONTAINER_NAME \
        --privileged \
        -v /tmp/android_sdk_test:/workspace:Z \
        -w /workspace \
        docker.io/openjdk:11-jdk \
        tail -f /dev/null
        
    if ! podman ps --format "{{.Names}}" | grep -q "^$CONTAINER_NAME$"; then
        echo "❌ Failed to start container. Container approach not available."
        echo ""
        echo "📱 Alternative Testing Methods:"
        echo "  1. Physical device: ./test_on_device.sh"
        echo "  2. Manual install: ~/Desktop/MyDashboardApp-SentryDemo.apk"
        echo "  3. Wireless download: http://localhost:8080"
        exit 1
    fi
    
    # Install Android SDK in the OpenJDK container
    echo "📦 Installing Android SDK..."
    podman exec $CONTAINER_NAME bash -c "
        apt-get update && apt-get install -y wget unzip
        mkdir -p /opt/android-sdk
        cd /opt/android-sdk
        wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
        unzip -q commandlinetools-linux-9477386_latest.zip
        mkdir -p cmdline-tools
        mv cmdline-tools latest-temp
        mv latest-temp cmdline-tools/latest
        export ANDROID_HOME=/opt/android-sdk
        export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools
        yes | cmdline-tools/latest/bin/sdkmanager --licenses || true
        cmdline-tools/latest/bin/sdkmanager platform-tools
    "
fi

echo "✅ Container started successfully!"

# Check if we have Android tools available
echo "🔍 Checking Android SDK tools..."
if podman exec $CONTAINER_NAME which adb >/dev/null 2>&1; then
    echo "✅ ADB is available in container"
    
    # Test APK validation
    echo "🔍 Validating APK..."
    if podman exec $CONTAINER_NAME aapt dump badging /workspace/app.apk | head -5; then
        echo "✅ APK is valid and readable"
        
        # Show APK information
        echo ""
        echo "📱 APK Information:"
        podman exec $CONTAINER_NAME aapt dump badging /workspace/app.apk | grep -E "(package:|application-label:|uses-permission)" | head -10
        
    else
        echo "⚠️  Could not validate APK, but file exists"
    fi
    
else
    echo "⚠️  ADB not found in container, but container is running"
fi

echo ""
echo "🎉 Android SDK Container Ready!"
echo "================================"
echo ""
echo "📱 The APK is available in the container at /workspace/app.apk"
echo "🔧 Container tools available for APK analysis and testing"
echo ""
echo "💡 For actual device testing, use:"
echo "   ./test_on_device.sh - Connect your Android device via USB"
echo ""
echo "🔧 Container Management:"
echo "   Enter container: podman exec -it $CONTAINER_NAME bash"
echo "   View APK info: podman exec $CONTAINER_NAME aapt dump badging /workspace/app.apk"
echo "   Stop: podman stop $CONTAINER_NAME"
echo "   Remove: podman rm $CONTAINER_NAME"
echo ""
echo "📊 Sentry Dashboard: https://shray.sentry.io/projects/mydashboardapp/"
