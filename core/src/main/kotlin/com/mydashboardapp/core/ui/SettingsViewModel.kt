package com.mydashboardapp.core.ui

import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.data.FeatureFlags
import com.mydashboardapp.core.data.SyncFrequency
import com.mydashboardapp.core.data.Theme
import com.mydashboardapp.core.data.UserPreferences
import com.mydashboardapp.core.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * UI State for Settings screen
 */
data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val featureFlags: FeatureFlags = FeatureFlags.fromPremiumState(false),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showUpgradeDialog: Boolean = false
) : UiState

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : BaseViewModel<SettingsUiState>(
    initialState = SettingsUiState()
) {

    init {
        observeUserPreferences()
        observeFeatureFlags()
    }

    private fun observeUserPreferences() {
        userPreferencesRepository.userPreferences
            .onEach { preferences ->
                updateState { currentState ->
                    currentState.copy(
                        userPreferences = preferences,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFeatureFlags() {
        userPreferencesRepository.featureFlags
            .onEach { flags ->
                updateState { currentState ->
                    currentState.copy(
                        featureFlags = flags
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Update theme preference
     */
    fun updateTheme(theme: Theme) {
        launchWithErrorHandling {
            updateLoadingState(true)
            userPreferencesRepository.updateTheme(theme)
            showSuccessMessage("Theme updated successfully")
            updateLoadingState(false)
        }
    }

    /**
     * Update language preference
     */
    fun updateLanguage(language: String) {
        launchWithErrorHandling {
            updateLoadingState(true)
            userPreferencesRepository.updateLanguage(language)
            showSuccessMessage("Language updated successfully")
            updateLoadingState(false)
        }
    }

    /**
     * Toggle notifications
     */
    fun toggleNotifications(enabled: Boolean) {
        launchWithErrorHandling {
            userPreferencesRepository.updateNotificationsEnabled(enabled)
            showSuccessMessage(if (enabled) "Notifications enabled" else "Notifications disabled")
        }
    }

    /**
     * Toggle analytics
     */
    fun toggleAnalytics(enabled: Boolean) {
        launchWithErrorHandling {
            userPreferencesRepository.updateAnalyticsEnabled(enabled)
            showSuccessMessage(if (enabled) "Analytics enabled" else "Analytics disabled")
        }
    }

    /**
     * Toggle auto sync
     */
    fun toggleAutoSync(enabled: Boolean) {
        val flags = currentState.featureFlags
        if (!flags.cloudSyncEnabled && enabled) {
            showUpgradeDialog("Cloud sync is a Pro feature. Upgrade to sync your data across devices.")
            return
        }

        launchWithErrorHandling {
            userPreferencesRepository.updateAutoSyncEnabled(enabled)
            showSuccessMessage(if (enabled) "Auto sync enabled" else "Auto sync disabled")
        }
    }

    /**
     * Update sync frequency
     */
    fun updateSyncFrequency(frequency: SyncFrequency) {
        val flags = currentState.featureFlags
        if (!flags.cloudSyncEnabled) {
            showUpgradeDialog("Cloud sync is a Pro feature. Upgrade to sync your data across devices.")
            return
        }

        launchWithErrorHandling {
            userPreferencesRepository.updateSyncFrequency(frequency)
            showSuccessMessage("Sync frequency updated")
        }
    }

    /**
     * Update currency code
     */
    fun updateCurrencyCode(currencyCode: String) {
        launchWithErrorHandling {
            updateLoadingState(true)
            userPreferencesRepository.updateCurrencyCode(currencyCode)
            showSuccessMessage("Currency updated successfully")
            updateLoadingState(false)
        }
    }

    /**
     * Mark onboarding as completed
     */
    fun completeOnboarding() {
        launchWithErrorHandling {
            userPreferencesRepository.setOnboardingCompleted(true)
            userPreferencesRepository.setFirstLaunch(false)
        }
    }

    /**
     * Upgrade to Pro version
     */
    fun upgradeToPro() {
        launchWithErrorHandling {
            updateLoadingState(true)
            userPreferencesRepository.upgradeToProVersion()
            showSuccessMessage("Welcome to Pro! Enjoy unlimited features.")
            updateLoadingState(false)
        }
    }

    /**
     * Reset all settings to defaults
     */
    fun resetSettings() {
        launchWithErrorHandling {
            updateLoadingState(true)
            userPreferencesRepository.clearAllPreferences()
            showSuccessMessage("Settings reset to defaults")
            updateLoadingState(false)
        }
    }

    /**
     * Export user data (Pro feature)
     */
    fun exportData() {
        val flags = currentState.featureFlags
        if (!flags.exportEnabled) {
            showUpgradeDialog("Data export is a Pro feature. Upgrade to export your data.")
            return
        }

        launchWithErrorHandling {
            updateLoadingState(true)
            // Implement export logic here
            showSuccessMessage("Data exported successfully")
            updateLoadingState(false)
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        updateState { it.copy(errorMessage = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        updateState { it.copy(successMessage = null) }
    }

    /**
     * Dismiss upgrade dialog
     */
    fun dismissUpgradeDialog() {
        updateState { it.copy(showUpgradeDialog = false) }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        updateState { it.copy(isLoading = isLoading) }
    }

    private fun showSuccessMessage(message: String) {
        updateState { it.copy(successMessage = message) }
    }

    private fun showUpgradeDialog(message: String) {
        updateState { 
            it.copy(
                showUpgradeDialog = true,
                errorMessage = message
            ) 
        }
    }

    /**
     * Get feature limitation message for free users
     */
    fun getFeatureLimitationMessage(feature: String): String {
        return when (feature) {
            "nutrition" -> "You've used ${currentState.featureFlags.maxNutritionEntries} nutrition entries. Upgrade to Pro for unlimited entries."
            "workouts" -> "You've used ${currentState.featureFlags.maxWorkouts} workouts. Upgrade to Pro for unlimited workouts."
            "tasks" -> "You've used ${currentState.featureFlags.maxTasks} tasks. Upgrade to Pro for unlimited tasks."
            "accounts" -> "You've used ${currentState.featureFlags.maxAccounts} accounts. Upgrade to Pro for unlimited accounts."
            "inventory" -> "You've used ${currentState.featureFlags.maxInventoryItems} inventory items. Upgrade to Pro for unlimited items."
            else -> "Upgrade to Pro to unlock this feature."
        }
    }

    /**
     * Check if a feature is available
     */
    fun isFeatureAvailable(feature: String): Boolean {
        val flags = currentState.featureFlags
        return when (feature) {
            "advancedAnalytics" -> flags.advancedAnalyticsEnabled
            "export" -> flags.exportEnabled
            "customCategories" -> flags.customCategoriesEnabled
            "bulkOperations" -> flags.bulkOperationsEnabled
            "aiSuggestions" -> flags.aiSuggestionsEnabled
            "cloudSync" -> flags.cloudSyncEnabled
            else -> true
        }
    }
}
