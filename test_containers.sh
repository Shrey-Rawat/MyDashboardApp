#!/bin/bash

# Test Android Container Availability
# Checks which Android containers are accessible and working

set -e

echo "🧪 Testing Android Container Availability"
echo "=========================================="

# Test container images
IMAGES=(
    "docker.io/budtmo/docker-android:emulator_11.0"
    "docker.io/thyrlian/android-sdk:latest"
    "docker.io/cimg/android:2023.08"
    "docker.io/openjdk:11-jdk"
    "docker.io/appium/appium:latest"
)

echo "📦 Testing container image accessibility..."

for image in "${IMAGES[@]}"; do
    echo ""
    echo "🔍 Testing: $image"
    
    if timeout 30 podman pull "$image" >/dev/null 2>&1; then
        echo "✅ $image - AVAILABLE"
        
        # Try to run a simple container
        CONTAINER_NAME="test_$(echo "$image" | tr '/' '_' | tr ':' '_')"
        if podman run --rm -d --name "$CONTAINER_NAME" "$image" tail -f /dev/null >/dev/null 2>&1; then
            sleep 2
            if podman ps --format "{{.Names}}" | grep -q "$CONTAINER_NAME"; then
                echo "✅ $image - RUNS SUCCESSFULLY"
                podman stop "$CONTAINER_NAME" >/dev/null 2>&1 || true
            else
                echo "⚠️  $image - FAILED TO RUN"
            fi
        else
            echo "⚠️  $image - FAILED TO START"
        fi
    else
        echo "❌ $image - NOT ACCESSIBLE"
    fi
done

echo ""
echo "🎯 Recommended Testing Approach"
echo "================================"
echo ""
echo "Based on container testing results:"
echo ""
echo "1. 🥇 **Physical Device** (Most Reliable)"
echo "   ./test_on_device.sh"
echo ""
echo "2. 🥈 **Manual APK Install**"
echo "   ~/Desktop/MyDashboardApp-SentryDemo.apk"
echo ""
echo "3. 🥉 **Container Approach** (if any worked above)"
echo "   ./android_sdk_test.sh    # SDK tools only"
echo "   ./android_simple_test.sh # Full emulator attempt"
echo ""
echo "📊 Sentry Dashboard: https://shray.sentry.io/projects/mydashboardapp/"
