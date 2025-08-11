package com.mydashboardapp.ai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydashboardapp.ai.data.models.AIProvider
import com.mydashboardapp.ai.data.models.ProviderConfig
import com.mydashboardapp.ai.data.security.SecureStorage
import com.mydashboardapp.ai.data.service.AIService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AISettingsUiState(
    val providerConfigs: Map<AIProvider, ProviderConfig> = emptyMap(),
    val configuredProviders: List<AIProvider> = emptyList(),
    val defaultProvider: AIProvider? = null,
    val streamingEnabled: Boolean = true,
    val saveChatHistory: Boolean = true,
    val defaultTemperature: Float = 0.7f,
    val defaultMaxTokens: Int = 1024,
    val showProviderDropdown: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val testingProvider: AIProvider? = null,
    val connectionTestResult: ConnectionTestResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ConnectionTestResult(
    val provider: AIProvider,
    val success: Boolean,
    val message: String
)

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val aiService: AIService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val configs = mutableMapOf<AIProvider, ProviderConfig>()
                val configuredProviders = mutableListOf<AIProvider>()
                
                for (provider in AIProvider.values()) {
                    secureStorage.getProviderConfig(provider)?.let { config ->
                        configs[provider] = config
                        if (config.apiKey.isNotBlank() && config.isEnabled) {
                            configuredProviders.add(provider)
                        }
                    }
                }
                
                val defaultProviderName = secureStorage.getAIPreference(
                    SecureStorage.PREF_DEFAULT_PROVIDER
                )
                val defaultProvider = if (defaultProviderName.isNotBlank()) {
                    AIProvider.values().find { it.name == defaultProviderName }
                } else {
                    configuredProviders.firstOrNull()
                }
                
                val streamingEnabled = secureStorage.getAIPreference(
                    SecureStorage.PREF_STREAMING_ENABLED, true
                )
                
                val saveChatHistory = secureStorage.getAIPreference(
                    SecureStorage.PREF_SAVE_CHAT_HISTORY, true
                )
                
                val defaultTemperature = secureStorage.getAIPreference(
                    SecureStorage.PREF_DEFAULT_TEMPERATURE, 0.7f
                )
                
                val defaultMaxTokens = secureStorage.getAIPreference(
                    SecureStorage.PREF_DEFAULT_MAX_TOKENS, 1024.0f
                ).toInt()
                
                _uiState.value = _uiState.value.copy(
                    providerConfigs = configs,
                    configuredProviders = configuredProviders,
                    defaultProvider = defaultProvider,
                    streamingEnabled = streamingEnabled,
                    saveChatHistory = saveChatHistory,
                    defaultTemperature = defaultTemperature,
                    defaultMaxTokens = defaultMaxTokens,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load settings: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun updateProviderConfig(config: ProviderConfig) {
        viewModelScope.launch {
            try {
                secureStorage.saveProviderConfig(config)
                
                val updatedConfigs = _uiState.value.providerConfigs.toMutableMap()
                updatedConfigs[config.provider] = config
                
                val configuredProviders = updatedConfigs.values
                    .filter { it.apiKey.isNotBlank() && it.isEnabled }
                    .map { it.provider }
                
                _uiState.value = _uiState.value.copy(
                    providerConfigs = updatedConfigs,
                    configuredProviders = configuredProviders
                )
                
                // If this is the first configured provider, set it as default
                if (_uiState.value.defaultProvider == null && configuredProviders.isNotEmpty()) {
                    setDefaultProvider(configuredProviders.first())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save provider config: ${e.message}"
                )
            }
        }
    }
    
    fun setDefaultProvider(provider: AIProvider) {
        viewModelScope.launch {
            try {
                secureStorage.saveAIPreference(SecureStorage.PREF_DEFAULT_PROVIDER, provider.name)
                _uiState.value = _uiState.value.copy(defaultProvider = provider)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to set default provider: ${e.message}"
                )
            }
        }
    }
    
    fun setStreamingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                secureStorage.saveAIPreference(SecureStorage.PREF_STREAMING_ENABLED, enabled)
                _uiState.value = _uiState.value.copy(streamingEnabled = enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save streaming setting: ${e.message}"
                )
            }
        }
    }
    
    fun setSaveChatHistory(enabled: Boolean) {
        viewModelScope.launch {
            try {
                secureStorage.saveAIPreference(SecureStorage.PREF_SAVE_CHAT_HISTORY, enabled)
                _uiState.value = _uiState.value.copy(saveChatHistory = enabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save chat history setting: ${e.message}"
                )
            }
        }
    }
    
    fun setDefaultTemperature(temperature: Float) {
        viewModelScope.launch {
            try {
                secureStorage.saveAIPreference(SecureStorage.PREF_DEFAULT_TEMPERATURE, temperature)
                _uiState.value = _uiState.value.copy(defaultTemperature = temperature)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save temperature setting: ${e.message}"
                )
            }
        }
    }
    
    fun testConnection(provider: AIProvider) {
        viewModelScope.launch {
            val config = _uiState.value.providerConfigs[provider]
            if (config == null) {
                _uiState.value = _uiState.value.copy(
                    connectionTestResult = ConnectionTestResult(
                        provider = provider,
                        success = false,
                        message = "Provider not configured"
                    )
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(testingProvider = provider)
            
            try {
                val success = aiService.validateConnection(config)
                _uiState.value = _uiState.value.copy(
                    testingProvider = null,
                    connectionTestResult = ConnectionTestResult(
                        provider = provider,
                        success = success,
                        message = if (success) "Connection successful!" else "Connection failed"
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    testingProvider = null,
                    connectionTestResult = ConnectionTestResult(
                        provider = provider,
                        success = false,
                        message = "Connection failed: ${e.message}"
                    )
                )
            }
        }
    }
    
    fun toggleProviderDropdown() {
        _uiState.value = _uiState.value.copy(
            showProviderDropdown = !_uiState.value.showProviderDropdown
        )
    }
    
    fun showClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = true)
    }
    
    fun hideClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = false)
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            try {
                secureStorage.clearAllConfigs()
                _uiState.value = AISettingsUiState()
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to clear data: ${e.message}",
                    showClearDataDialog = false
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearConnectionTestResult() {
        _uiState.value = _uiState.value.copy(connectionTestResult = null)
    }
}
