package com.mydashboardapp.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    val featureFlags: Flow<FeatureFlags>
    
    suspend fun updateTheme(theme: Theme)
    suspend fun updateLanguage(language: String)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateAnalyticsEnabled(enabled: Boolean)
    suspend fun updateAutoSyncEnabled(enabled: Boolean)
    suspend fun updateSyncFrequency(frequency: SyncFrequency)
    suspend fun updateCurrencyCode(currencyCode: String)
    suspend fun setFirstLaunch(firstLaunch: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun updateLastSyncTimestamp(timestamp: Long)
    suspend fun upgradeToProVersion()
    suspend fun clearAllPreferences()
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val IS_PRO = booleanPreferencesKey("is_pro")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        val SYNC_FREQUENCY = stringPreferencesKey("sync_frequency")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }

    override val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            isPro = preferences[PreferencesKeys.IS_PRO] ?: false,
            theme = Theme.valueOf(preferences[PreferencesKeys.THEME] ?: Theme.SYSTEM.name),
            language = preferences[PreferencesKeys.LANGUAGE] ?: "en",
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            analyticsEnabled = preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true,
            autoSyncEnabled = preferences[PreferencesKeys.AUTO_SYNC_ENABLED] ?: true,
            syncFrequency = SyncFrequency.valueOf(
                preferences[PreferencesKeys.SYNC_FREQUENCY] ?: SyncFrequency.HOURLY.name
            ),
            currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "USD",
            firstLaunch = preferences[PreferencesKeys.FIRST_LAUNCH] ?: true,
            onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            lastSyncTimestamp = preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] ?: 0L
        )
    }

    override val featureFlags: Flow<FeatureFlags> = userPreferences.map { preferences ->
        FeatureFlags.fromPremiumState(preferences.isPro)
    }

    override suspend fun updateTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    override suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED] = enabled
        }
    }

    override suspend fun updateAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SYNC_ENABLED] = enabled
        }
    }

    override suspend fun updateSyncFrequency(frequency: SyncFrequency) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_FREQUENCY] = frequency.name
        }
    }

    override suspend fun updateCurrencyCode(currencyCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] = currencyCode
        }
    }

    override suspend fun setFirstLaunch(firstLaunch: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = firstLaunch
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun updateLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    override suspend fun upgradeToProVersion() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PRO] = true
        }
    }

    override suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
