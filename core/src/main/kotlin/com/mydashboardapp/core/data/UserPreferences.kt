package com.mydashboardapp.core.data

import kotlinx.serialization.Serializable

/**
 * Data class representing user preferences stored in DataStore
 */
@Serializable
data class UserPreferences(
    val isPro: Boolean = false,
    val theme: Theme = Theme.SYSTEM,
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val autoSyncEnabled: Boolean = true,
    val syncFrequency: SyncFrequency = SyncFrequency.HOURLY,
    val currencyCode: String = "USD",
    val firstLaunch: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

/**
 * Theme preferences
 */
@Serializable
enum class Theme {
    LIGHT, DARK, SYSTEM
}

/**
 * Sync frequency options
 */
@Serializable
enum class SyncFrequency(val minutes: Int) {
    NEVER(0),
    HOURLY(60),
    DAILY(1440),
    WEEKLY(10080)
}

/**
 * Feature flags for free vs pro functionality
 * Now integrated with the billing system's PremiumState
 */
@Serializable
data class FeatureFlags(
    val maxNutritionEntries: Int = 10,
    val maxWorkouts: Int = 5,
    val maxTasks: Int = 20,
    val maxAccounts: Int = 3,
    val maxInventoryItems: Int = 50,
    val advancedAnalyticsEnabled: Boolean = false,
    val exportEnabled: Boolean = false,
    val customCategoriesEnabled: Boolean = false,
    val bulkOperationsEnabled: Boolean = false,
    val aiSuggestionsEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = false
) {
    companion object {
        fun fromPremiumState(isPro: Boolean): FeatureFlags {
            return if (isPro) {
                FeatureFlags(
                    maxNutritionEntries = -1,
                    maxWorkouts = -1,
                    maxTasks = -1,
                    maxAccounts = -1,
                    maxInventoryItems = -1,
                    advancedAnalyticsEnabled = true,
                    exportEnabled = true,
                    customCategoriesEnabled = true,
                    bulkOperationsEnabled = true,
                    aiSuggestionsEnabled = true,
                    cloudSyncEnabled = true
                )
            } else {
                FeatureFlags() // Use default free tier limits
            }
        }
    }
}

/**
 * Note: In actual implementation, you would use BuildConfig from the app module
 * For now, we'll determine pro status from user preferences
 */
private object BuildConfigCompat {
    // This will be replaced with actual BuildConfig access when used in app module
    const val IS_PRO_VERSION = false
}
