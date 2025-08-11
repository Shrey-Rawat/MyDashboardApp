# Sentry Integration for MyDashboardApp

## Overview
Sentry has been integrated into the MyDashboardApp to provide comprehensive error tracking, performance monitoring, and user experience insights.

## Setup Complete

### Project Details
- **Organization:** shray
- **Project Name:** MyDashboardApp
- **Project Slug:** mydashboardapp
- **DSN:** `https://a72855998b3e174755611c78ee92d6af@o4509820996419584.ingest.de.sentry.io/4509821671047248`
- **Project ID:** 4509821671047248

### What's Configured

1. **Android SDK Integration:**
   - Sentry Android SDK 7.14.0
   - Sentry Compose integration for UI tracking
   - Automatic crash reporting and ANR detection
   - Performance monitoring with 20% sample rate in production, 100% in debug

2. **Error Tracking:**
   - Automatic crash reporting
   - Custom exception capturing with context
   - ViewModel error handling integration
   - ProGuard mapping for better stack traces

3. **Performance Monitoring:**
   - Transaction tracking for key user flows
   - Navigation tracking in Compose
   - Custom performance metrics for Pomodoro sessions
   - Automatic UI interaction breadcrumbs

4. **User Context:**
   - User authentication tracking
   - Feature usage analytics
   - Custom breadcrumbs for app-specific actions

## Environment Variables

To enable full Sentry features (especially for release builds), set the following environment variable:

```bash
export SENTRY_AUTH_TOKEN="your-sentry-auth-token"
```

You can generate an auth token in your Sentry account at: https://shray.sentry.io/settings/account/api/auth-tokens/

## Usage Examples

### Tracking Custom Events
```kotlin
// In your ViewModels
trackUserAction("task_created", mapOf("priority" to task.priority))

// Using SentryHelper directly
@Inject lateinit var sentryHelper: SentryHelper

sentryHelper.addBreadcrumb(
    message = "User completed workout",
    category = SentryHelper.Breadcrumbs.WORKOUT_LOGGED,
    data = mapOf("exerciseCount" to exercises.size)
)
```

### Performance Monitoring
```kotlin
// Wrap expensive operations
withSentryTransaction("database_import", "data_operation") {
    importLargeDataset()
}
```

### Setting User Context
```kotlin
// After user authentication
sentryHelper.setUser(
    userId = user.id,
    email = user.email,
    username = user.username
)
```

### Screen Tracking
```kotlin
@Composable
fun MyScreen() {
    TrackedComposable(screenName = "nutrition_logging") {
        // Your screen content
    }
}
```

## Features Monitored

### Productivity Features
- Pomodoro session starts, pauses, completions
- Task creation, completion, deletion
- Time tracking accuracy
- Streak maintenance

### Health & Fitness Features
- Workout logging and completion
- Nutrition data entry
- Progress tracking

### AI Features
- Query processing time
- API response handling
- Token usage tracking

### Finance Features
- Transaction processing
- Budget calculations
- Investment tracking

### Inventory Features
- Barcode scanning success/failure
- Stock level updates
- Alert triggering

## Sentry Dashboard

Access your Sentry dashboard at: https://shray.sentry.io/projects/mydashboardapp/

### Key Metrics to Monitor
1. **Error Rate:** Percentage of sessions with errors
2. **Crash-Free Sessions:** Sessions without crashes
3. **Performance:** Transaction durations and throughput
4. **User Impact:** Number of users affected by issues
5. **Release Health:** Stability across app versions

### Alerts Configured
- Critical errors affecting >10% of users
- Performance degradation >50% slower than baseline
- Crash rate >1% of sessions

## Development vs Production

### Debug Builds
- All transactions sent to Sentry (100% sample rate)
- Debug logs included
- Source context enabled
- Additional debugging breadcrumbs

### Release Builds
- 20% transaction sample rate for performance
- Production environment tag
- Source maps uploaded for better stack traces
- ProGuard mapping uploaded
- Optimized for performance

## Build Configuration

The integration includes:
- Automatic source context upload
- ProGuard mapping upload for release builds
- Build-time source map generation
- Release creation and deployment tracking

## Privacy & Data Handling

The integration follows privacy best practices:
- No sensitive user data in error reports
- PII filtering enabled
- Configurable data scrubbing
- GDPR-compliant data handling

## Troubleshooting

### Common Issues

1. **Missing Source Context:**
   - Ensure `SENTRY_AUTH_TOKEN` is set for builds
   - Verify organization and project name in build.gradle

2. **ProGuard Issues:**
   - Check proguard-rules.pro is properly configured
   - Verify mapping upload in build logs

3. **Performance Impact:**
   - Monitor SDK overhead in production
   - Adjust sample rates if needed

### Debug Commands
```bash
# Check Sentry CLI installation
sentry-cli --version

# Test auth token
sentry-cli info

# Manually upload ProGuard mapping
sentry-cli upload-proguard --uuid <BUILD_UUID> app/build/outputs/mapping/release/
```

## Next Steps

1. **Set Up Alerts:** Configure email/Slack notifications for critical issues
2. **Create Releases:** Tag releases to track stability over time
3. **Custom Dashboards:** Build dashboards for key metrics
4. **Integration Testing:** Test error scenarios in staging environment

## Resources

- [Sentry Android Documentation](https://docs.sentry.io/platforms/android/)
- [Sentry Compose Integration](https://docs.sentry.io/platforms/android/guides/compose/)
- [Performance Monitoring Guide](https://docs.sentry.io/product/performance/)
- [Release Health Monitoring](https://docs.sentry.io/product/releases/health/)
