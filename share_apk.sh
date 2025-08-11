#!/bin/bash

# APK Sharing and Testing Helper Script
# Provides multiple ways to access and test the APK

set -e

APK_PATH="app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk"
APK_NAME="MyDashboardApp-SentryDemo.apk"

echo "📱 Best Productivity App - Sentry Demo"
echo "======================================"

# Check if APK exists
if [[ ! -f "$APK_PATH" ]]; then
    echo "❌ APK not found at $APK_PATH"
    echo "Building APK now..."
    ./gradlew assembleProFirebaseDebug
    if [[ ! -f "$APK_PATH" ]]; then
        echo "❌ Build failed. Please check the build output."
        exit 1
    fi
fi

APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
echo "✅ APK ready: $APK_SIZE"

echo ""
echo "🔗 APK Access Options:"
echo "======================"

echo ""
echo "1️⃣  Direct File Access:"
echo "   📍 Location: $APK_PATH"
echo "   💾 Size: $APK_SIZE"

echo ""
echo "2️⃣  Copy to Desktop:"
if cp "$APK_PATH" ~/Desktop/"$APK_NAME" 2>/dev/null; then
    echo "   ✅ Copied to ~/Desktop/$APK_NAME"
else
    echo "   ℹ️  Desktop not found, copying to home directory"
    cp "$APK_PATH" ~/"$APK_NAME"
    echo "   ✅ Copied to ~/$APK_NAME"
fi

echo ""
echo "3️⃣  Start HTTP Server (for wireless install):"
echo "   Run this command to serve the APK over HTTP:"
echo "   cd $(dirname "$APK_PATH") && python3 -m http.server 8080"
echo "   Then access: http://$(hostname -I | awk '{print $1}'):8080/$(basename "$APK_PATH")"

echo ""
echo "4️⃣  QR Code for Download:"
if command -v qrencode &> /dev/null; then
    LOCAL_IP=$(hostname -I | awk '{print $1}')
    QR_URL="http://$LOCAL_IP:8080/$(basename "$APK_PATH")"
    echo "   📱 Scan this QR code to download on your device:"
    qrencode -t ANSIUTF8 "$QR_URL"
    echo "   🔗 URL: $QR_URL"
else
    echo "   ℹ️  Install qrencode to generate QR code: sudo pacman -S qrencode"
fi

echo ""
echo "5️⃣  Container Emulator:"
echo "   🖥️  Run: ./run_android_test.sh"
echo "   🌐 Access via: http://localhost:6080"

echo ""
echo "🎯 Testing Instructions:"
echo "========================"
echo "1. Install the APK on your Android device"
echo "2. Open 'Best Productivity App Pro'"
echo "3. Test the three buttons:"
echo "   • 'Trigger Test Exception' - Will crash the app (expected)"
echo "   • 'Send Test Message to Sentry' - Sends info message" 
echo "   • 'Send Non-Fatal Exception' - Sends error without crashing"
echo ""
echo "📊 Check results at: https://shray.sentry.io/projects/mydashboardapp/"
echo ""
echo "💡 Tip: Enable 'Install from unknown sources' in Android settings"
