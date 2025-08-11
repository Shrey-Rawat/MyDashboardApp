# Best Productivity App

🚀 A comprehensive productivity application built with modern Android technologies including Jetpack Compose, Room, Hilt, and Sentry error monitoring.

## 🎯 Current Status

✅ **Working Features:**
- Beautiful Material Design 3 UI with custom app icon
- Sentry integration for error monitoring and crash reporting
- Modern architecture with multi-module setup
- Clean build system with proper dependency management
- Comprehensive testing infrastructure

🚧 **In Development:**
- Core productivity features (task management, nutrition, finance)
- Database integration with Room
- Authentication system
- AI-powered insights

## Project Structure

### Top-level Modules

- **`app`** - Thin launcher module containing the main Activity and Application class
- **`core`** - Common utilities, theme, navigation helpers, and shared UI components
- **`data`** - Room database, repository implementations, data models, and DTOs
- **`sync`** - Optional cloud-sync implementation (premium feature)
- **`auth`** - Authentication module with Firebase Phone Auth isolated behind interfaces
- **`export`** - CSV/PDF export functionality
- **`billing`** - Google Play Billing integration for premium features

### Feature Modules

- **`feature-nutrition`** - Nutrition tracking and meal planning functionality
- **`feature-training`** - Workout tracking and fitness planning
- **`feature-productivity`** - Task management and productivity tools
- **`feature-finance`** - Financial tracking and budgeting features
- **`feature-inventory`** - Inventory and stock management
- **`feature-ai`** - AI-powered features and recommendations

### Build Logic

- **`build-logic`** - Contains Gradle convention plugins that standardize build configuration across modules

## Gradle Convention Plugins

The project uses custom Gradle convention plugins located in `build-logic/convention/` to share:

- **Compiler configurations** - Consistent Kotlin compiler settings
- **Android configurations** - Common Android SDK, compile SDK, and build options
- **Detekt setup** - Code quality and static analysis
- **Test configurations** - Standardized testing dependencies and configurations
- **Dependency management** - Centralized dependency versions via version catalogs

### Available Convention Plugins

- `mydashboardapp.android.application` - For the main app module
- `mydashboardapp.android.library` - For Android library modules
- `mydashboardapp.android.feature` - For feature modules with common dependencies
- `mydashboardapp.android.hilt` - For Hilt dependency injection setup
- `mydashboardapp.android.room` - For Room database configuration
- `mydashboardapp.jvm.library` - For pure Kotlin/JVM modules

## 🛠️ Technology Stack

### Development
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Dagger Hilt
- **Database**: Room
- **Navigation**: Navigation Compose
- **Network**: Retrofit with Kotlin Serialization
- **Async**: Kotlin Coroutines and Flow
- **Error Monitoring**: Sentry
- **Build System**: Gradle with Kotlin DSL and Convention Plugins

### Testing
- **JUnit 5** - Unit testing framework
- **MockK** - Mocking library
- **Espresso** - UI testing
- **Robolectric** - Android unit tests
- **Compose Testing** - UI component testing

### Build & Tools
- **Gradle Version Catalogs** - Dependency management
- **Convention Plugins** - Build configuration
- **Detekt** - Static code analysis
- **ktlint** - Code formatting
- **Sentry CLI** - Release and source map management

## Module Dependencies

```
app
├── core
├── feature-nutrition → data, core
├── feature-training → data, core
├── feature-productivity → data, core
├── feature-finance → data, core
├── feature-inventory → data, core
└── feature-ai → data, core

data → core
sync → data, core
auth → core
export → data, core
billing → core
```

## Build Configuration

The project uses:
- **Version Catalogs** (`gradle/libs.versions.toml`) for centralized dependency management
- **Convention Plugins** for shared build logic
- **Detekt** for static code analysis with shared configuration
- **ProGuard/R8** for code obfuscation and optimization in release builds

## Premium Features

Some modules like `sync` and features behind `billing` are designed to be premium/paid features:
- Cloud synchronization
- Advanced AI features
- Premium export options
- Extended analytics

## 🚀 Quick Start

### Prerequisites

- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 17+
- Android SDK with API level 34+
- Git
- ADB (Android Debug Bridge)

### Setup & Run

1. **Clone and build**
   ```bash
   git clone https://github.com/yourusername/MyDashboardApp.git
   cd MyDashboardApp
   ./gradlew assembleFreeFirebaseDebug
   ```

2. **Test on your device** (Recommended)
   ```bash
   # Connect your Android device with USB debugging enabled
   ./test_app.sh
   ```

3. **Alternative: Test with Android emulator containers**
   ```bash
   # Option 1: Simple container test
   ./android_simple_test.sh
   
   # Option 2: VNC-enabled container
   ./android_vnc_podman.sh
   
   # Check which containers work on your system
   ./test_containers.sh
   ```

### App Features Demo

Once installed, the app includes:
- **Welcome screen** with modern Material Design 3 UI
- **Beautiful custom icon** with productivity theme
- **Sentry integration** - click "Test Sentry" to send a test message
- **Crash testing** - click "Test Crash" to trigger a sample crash report

## 📊 Monitoring & Analytics

### Sentry Integration
The app is integrated with Sentry for:
- **Crash reporting** - Automatic crash detection and reporting
- **Error monitoring** - Real-time error tracking
- **Performance monitoring** - App performance insights
- **User context** - Enhanced debugging information

See `SENTRY_SETUP.md` for detailed Sentry configuration.

### View Live Data
- Sentry Dashboard: https://shray.sentry.io/projects/mydashboardapp/
- Real-time error tracking and performance monitoring
- Source code integration for better debugging

## 🧪 Testing Options

### Physical Device (Recommended)
```bash
./test_app.sh
```

### Container Testing
Multiple container options available:
```bash
./test_containers.sh        # Check available containers
./android_simple_test.sh    # Basic Android container
./android_vnc_podman.sh     # VNC-enabled Android container
./android_sdk_test.sh       # SDK-based container
```

See `TESTING_OPTIONS.md` for detailed testing instructions.

## 🎨 App Icon

The app features a beautiful custom icon with:
- **Material Design 3 colors** - Primary purple theme
- **Productivity symbols** - Checkmark, target, tasks, clock, star
- **Adaptive icon support** - Works on all Android versions
- **Multiple densities** - Crisp on all screen resolutions

## Development

### Code Style

This project uses ktlint for Kotlin code formatting. Git hooks are configured to automatically check formatting before commits.

#### Manual Linting

To manually check formatting:
```bash
./gradlew ktlintCheck
```

To automatically format your code:
```bash
./gradlew ktlintFormat
```

#### Git Hooks Setup

Git hooks are automatically installed for code formatting and commit message validation. If you need to reinstall them:

```bash
./scripts/install-hooks.sh
```

The hooks will:
- Run ktlint formatting checks before each commit
- Validate commit messages follow conventional commit format

### Git Workflow

- `main` branch: Production-ready code
- `develop` branch: Integration branch for features
- Feature branches: Create from `develop` for new features

### Commit Guidelines

- Use conventional commit messages
- Keep commits atomic and focused
- Write clear, descriptive commit messages

## Contributing

1. Fork the repository
2. Create a feature branch from `develop`
3. Make your changes
4. Run tests and linting
5. Submit a pull request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contact

- Project Link: [https://github.com/yourusername/MyDashboardApp](https://github.com/yourusername/MyDashboardApp)
