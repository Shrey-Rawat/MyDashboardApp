# Build Instructions

This document provides comprehensive instructions for building the Best Productivity App from source.

## Prerequisites

### Required Software
- **Android Studio**: Arctic Fox (2021.3.1) or later
- **Java Development Kit**: OpenJDK 17 or later
- **Android SDK**: API level 24 (Android 7.0) minimum, API level 34 (Android 14) target
- **Git**: For version control

### Recommended System Requirements
- **RAM**: 16GB minimum (8GB may work but builds will be slower)
- **Storage**: 10GB free space for Android SDK, project, and build artifacts
- **OS**: Windows 10+, macOS 10.15+, or Ubuntu 18.04+

## Initial Setup

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/MyDashboardApp.git
cd MyDashboardApp
```

### 2. Install Git Hooks (Optional but Recommended)
```bash
./scripts/install-hooks.sh
```

### 3. Android Studio Setup
1. Open Android Studio
2. Choose "Open an existing Android Studio project"
3. Navigate to the cloned repository and select the root folder
4. Wait for Gradle sync to complete

### 4. SDK Configuration
The project uses API level 34 as the target SDK. Ensure you have:
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android SDK Platform-Tools (latest)

## Build Variants

The project supports multiple build variants:

### Product Flavors
- **`free`**: Free version with limited features
- **`pro`**: Pro version with all features enabled

### Build Types
- **`debug`**: Development builds with debugging enabled
- **`release`**: Production builds with code optimization

### Available Variants
1. `freeDebug` - Free development build
2. `freeRelease` - Free production build  
3. `proDebug` - Pro development build
4. `proRelease` - Pro production build

## Building from Command Line

### Prerequisites
Ensure you have the Android SDK path set:
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### Build Commands

#### Build All Variants
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

#### Build Specific Variants
```bash
./gradlew assembleFreeDebug
./gradlew assembleFreeRelease
./gradlew assembleProDebug
./gradlew assembleProRelease
```

#### Clean Build
```bash
./gradlew clean
./gradlew assembleDebug
```

## Testing

### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run tests for specific variant
./gradlew testFreeDebugUnitTest
./gradlew testProReleaseUnitTest
```

### Instrumentation Tests
```bash
# Run all instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run for specific variant
./gradlew connectedFreeDebugAndroidTest
```

### Code Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Code Quality

### Linting
```bash
# Run ktlint check
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

### Static Analysis
```bash
./gradlew detekt
```

## Signing Configuration

### Debug Builds
Debug builds are automatically signed with the debug keystore.

### Release Builds
For release builds, create a `keystore.properties` file in the root directory:
```properties
storeFile=/path/to/your/keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

## Troubleshooting

### Common Issues

#### Gradle Sync Failed
1. Check internet connection
2. Invalidate caches and restart Android Studio
3. Delete `.gradle` folder and retry

#### Out of Memory Errors
Add to `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
```

#### SDK Not Found
1. Verify `ANDROID_HOME` environment variable
2. Check SDK installation in Android Studio Settings

#### Build Failures
1. Clean project: `./gradlew clean`
2. Check for dependency conflicts
3. Verify all required SDK components are installed

### Performance Optimization

#### Faster Builds
Enable parallel builds in `gradle.properties`:
```properties
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
```

#### Incremental Compilation
The project is configured for incremental compilation by default.

## CI/CD Integration

The project includes GitHub Actions workflows for:
- **Build**: Automated building of all variants
- **Test**: Running unit and instrumentation tests
- **Lint**: Code quality checks
- **Release**: Publishing releases

## Module-Specific Build Notes

### Feature Modules
Each feature module can be built independently:
```bash
./gradlew :feature-nutrition:assembleDebug
./gradlew :feature-training:assembleDebug
```

### Data Module
Contains Room database migrations - ensure database version increments:
```bash
./gradlew :data:test
```

### Auth Module
Firebase configuration required for full functionality:
1. Add `google-services.json` to `app/` directory
2. Enable Phone Authentication in Firebase Console

## Build Artifacts

### APK Location
Built APKs are located in:
```
app/build/outputs/apk/{flavor}/{buildType}/
```

### AAB (App Bundle) Location
```
app/build/outputs/bundle/{flavor}{buildType}/
```

## Development Workflow

1. Create feature branch from `develop`
2. Make changes
3. Run tests: `./gradlew test`
4. Run linting: `./gradlew ktlintCheck detekt`
5. Build: `./gradlew assembleDebug`
6. Submit pull request

## Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Gradle Build Tool](https://gradle.org/guides/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
