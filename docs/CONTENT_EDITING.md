# Content Editing Guide

This guide explains where to find and edit various text content in the Best Productivity App.

## Main Screen Content

### Welcome Screen Text
**File:** `app/src/main/kotlin/com/mydashboardapp/MainScreen.kt`

In the `WelcomeScreen` composable function, you can edit:

- **Main Title:** Line 79
  ```kotlin
  Text(
      text = "Best Productivity App",
      style = MaterialTheme.typography.headlineMedium
  )
  ```

- **Welcome Message:** Line 87
  ```kotlin
  Text(
      text = "Welcome to the Best Productivity App!",
      style = MaterialTheme.typography.titleLarge
  )
  ```

- **Description:** Line 93
  ```kotlin
  Text(
      text = "This app helps you manage your daily tasks, track your nutrition, monitor your finances, and improve your productivity with AI-powered insights.",
      style = MaterialTheme.typography.bodyLarge
  )
  ```

- **Feature List:** Lines 109-114
  ```kotlin
  FeatureItem("• Smart Task Management", "Organize and prioritize your daily tasks")
  FeatureItem("• Nutrition Tracking", "Monitor your dietary intake and health goals")
  FeatureItem("• Training Programs", "Custom workouts and fitness tracking")
  FeatureItem("• Financial Management", "Budget tracking and expense monitoring")
  FeatureItem("• Inventory Management", "Track your belongings and assets")
  FeatureItem("• AI-Powered Insights", "Get intelligent recommendations and analysis")
  ```

## Onboarding Screens

### Onboarding Content
**File:** `app/src/main/kotlin/com/mydashboardapp/onboarding/OnboardingScreen.kt`

The onboarding pages are defined at the bottom of the file (lines 228-286). Each page has:

- **Title:** The main heading for each feature
- **Description:** Detailed explanation of the feature
- **Icon:** Material Design icon
- **Colors:** Background and icon colors

#### Example - Task Management Page:
```kotlin
OnboardingPage(
    title = "Smart Task Management",
    description = "Organize your daily tasks with intelligent prioritization, deadlines, and progress tracking. Get things done efficiently with our AI-powered task recommendations.",
    icon = Icons.Default.Checklist,
    backgroundColor = Color(0xFF6366F1),
    iconColor = Color(0xFF6366F1)
),
```

### Current Onboarding Pages:
1. **Smart Task Management** - Task organization and AI recommendations
2. **Nutrition Tracking** - Dietary monitoring and meal recommendations
3. **Training Programs** - Workout routines and fitness tracking
4. **Financial Management** - Budget tracking and saving recommendations
5. **Reporting & Analysis** - Progress insights and analytics
6. **AI Integration** - Personalized recommendations and automation

## String Resources

### App Name
**File:** `app/src/main/res/values/strings.xml`

```xml
<string name="app_name">MyDashboardApp</string>
```

### Splash Screen Strings
```xml
<string name="splash_title">Best Productivity App</string>
<string name="splash_subtitle">Get productive with style</string>
<string name="splash_loading">Loading...</string>
```

## Build Variants

The app name changes based on the build variant:
- **Free version:** "Best Productivity App Free"
- **Pro version:** "Best Productivity App Pro"

This is configured in `app/build.gradle.kts`:
```kotlin
resValue("string", "app_name", "Best Productivity App Free")  // Free variant
resValue("string", "app_name", "Best Productivity App Pro")   // Pro variant
```

## Navigation Labels

### Bottom Navigation
**File:** `core/src/main/kotlin/com/mydashboardapp/core/navigation/BottomNavigation.kt`

The bottom navigation labels are defined in the `bottomNavItems` list (lines 27-64):

```kotlin
BottomNavItem(
    destination = MainDestinations.Nutrition,
    icon = Icons.Default.Fastfood,
    label = "Nutrition",
    contentDescription = "Navigate to Nutrition"
),
// ... more items
```

## Editing Guidelines

1. **Consistency:** Keep messaging consistent across all screens
2. **Localization:** Consider adding string resources for future localization
3. **Length:** Keep text concise for mobile screens
4. **Tone:** Maintain a professional yet friendly tone
5. **Features:** Update descriptions when features change

## Testing Changes

After editing content:

1. Clean and rebuild the app:
   ```bash
   ./gradlew clean
   ./gradlew :app:assembleProFirebaseDebug
   ```

2. Install on device:
   ```bash
   adb install -r app/build/outputs/apk/proFirebase/debug/app-pro-firebase-debug.apk
   ```

3. Clear app data to see onboarding again:
   ```bash
   adb shell pm clear com.mydashboardapp.pro
   ```
