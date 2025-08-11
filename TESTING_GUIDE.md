# 🧪 Android App Testing Guide - Sentry Integration

This guide provides multiple ways to test the **Best Productivity App** with Sentry integration using **Podman** containers.

## 📱 APK Information

- **File**: `~/Desktop/MyDashboardApp-SentryDemo.apk`
- **Size**: 23MB
- **Package**: `com.mydashboardapp.pro.firebase.debug`
- **Sentry Project**: https://shray.sentry.io/projects/mydashboardapp/

## 🚀 Testing Options

### Option 1: Physical Android Device (Recommended)

**Best for**: Real device testing, authentic user experience

```bash
# Copy APK to your device and install
# Enable "Install from unknown sources" in Android settings
# Install: ~/Desktop/MyDashboardApp-SentryDemo.apk
```

### Option 2: HTTP Download to Device

**Best for**: Wireless installation without USB

```bash
# Start HTTP server (already running on port 8080)
cd app/build/outputs/apk/proFirebase/debug && python3 -m http.server 8080

# On your device, navigate to:
# http://[YOUR_LOCAL_IP]:8080/app-pro-firebase-debug.apk
```

### Option 3: VNC Android Emulator (Podman)

**Best for**: GUI testing with visual feedback

```bash
# Install VNC viewer first
sudo pacman -S tigervnc  # or remmina

# Run the VNC-based emulator
./android_vnc_podman.sh

# Connect via VNC to localhost:5900
vncviewer localhost:5900
```

### Option 4: Lightweight Android Container

**Best for**: Headless testing, CI/CD integration

```bash
# Run lightweight Android container
./test_android_podman.sh

# Access via ADB
adb -s localhost:5555 shell
```

### Option 5: Web VNC Emulator

**Best for**: Browser-based testing, no additional software

```bash
# Run web-based emulator
./run_android_test.sh

# Access via browser
firefox http://localhost:6080
```

## 🎯 Testing Instructions

Once you have the app running:

1. **Open "Best Productivity App Pro"**
2. **Test Sentry Integration:**
   - 🔴 **"Trigger Test Exception"** - Crashes app (expected!)
   - 💬 **"Send Test Message to Sentry"** - Sends info message
   - ⚠️ **"Send Non-Fatal Exception"** - Sends error without crash

3. **Check Sentry Dashboard:**
   - Visit: https://shray.sentry.io/projects/mydashboardapp/
   - Look for the test events with user context

## 🔧 Troubleshooting

### Common Issues

**Container won't start:**
```bash
# Check Podman status
podman ps -a
podman logs [container_name]

# Clean up and retry
podman stop [container_name]
podman rm [container_name]
```

**ADB connection issues:**
```bash
# Install ADB if missing
sudo pacman -S android-tools

# Reset ADB
adb kill-server
adb start-server
adb connect localhost:5555
```

**VNC connection issues:**
```bash
# Install VNC client
sudo pacman -S tigervnc

# Connect manually
vncviewer localhost:5900
```

### Performance Tips

**For better container performance:**
- Close other containers: `podman stop $(podman ps -q)`
- Free up disk space: `podman system prune`
- Use SSD storage for containers

**For faster emulator:**
- Use the lightweight option (`test_android_podman.sh`)
- Enable hardware acceleration if available
- Allocate more RAM to containers

## 📊 Expected Sentry Events

You should see these events in your dashboard:

| Event Type | Message | Level |
|------------|---------|-------|
| Exception | "Test exception for Sentry monitoring..." | Error |
| Message | "User triggered a custom message" | Info |
| Exception | "Test non-fatal exception" | Error |

Each event includes:
- **User Context**: demo-user, demo@mydashboardapp.com
- **Environment**: development/production
- **Release**: com.mydashboardapp.pro.firebase.debug@1.0.0
- **Breadcrumbs**: User interaction history
- **Device Info**: Android version, device model

## 🧹 Cleanup

Stop and remove test containers:

```bash
# Stop all test containers
podman stop android_emulator_test android_vnc_test android_test_env

# Remove containers
podman rm android_emulator_test android_vnc_test android_test_env

# Remove networks
podman network rm android_test_net

# Clean up shared directories
rm -rf /tmp/android_test /tmp/android_vnc_test /tmp/android_test_podman
```

## 🎉 Success Indicators

✅ **APK builds successfully** (23MB)  
✅ **Container starts without errors**  
✅ **ADB connects to emulator**  
✅ **APK installs on Android device**  
✅ **App launches and shows Sentry demo screen**  
✅ **Sentry events appear in dashboard**  
✅ **User context and breadcrumbs are captured**  

## 🔍 Debug Commands

```bash
# View container logs
podman logs [container_name]

# Check app logs
adb -s localhost:5555 logcat | grep -i sentry

# Take screenshot
adb -s localhost:5555 shell screencap -p /sdcard/screenshot.png

# List installed packages
adb -s localhost:5555 shell pm list packages | grep bestproductivity

# Check device properties
adb -s localhost:5555 shell getprop
```

Choose the testing method that works best for your setup! The VNC option provides the most authentic Android experience, while the lightweight option is great for automated testing.
