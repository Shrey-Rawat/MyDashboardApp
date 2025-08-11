package com.mydashboardapp.ai.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mydashboardapp.ai.data.models.AIProvider
import com.mydashboardapp.ai.data.models.ProviderConfig
import com.mydashboardapp.core.security.KeystoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage service for API keys using Android Keystore encryption
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "ai_provider_configs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    suspend fun saveProviderConfig(config: ProviderConfig) = withContext(Dispatchers.IO) {
        try {
            val configJson = json.encodeToString(config)
            encryptedPrefs.edit()
                .putString(getProviderKey(config.provider), configJson)
                .apply()
        } catch (e: Exception) {
            throw SecurityException("Failed to save provider config: ${e.message}", e)
        }
    }
    
    suspend fun getProviderConfig(provider: AIProvider): ProviderConfig? = withContext(Dispatchers.IO) {
        try {
            val configJson = encryptedPrefs.getString(getProviderKey(provider), null)
            configJson?.let { json.decodeFromString<ProviderConfig>(it) }
        } catch (e: Exception) {
            null // Return null if decryption fails or config doesn't exist
        }
    }
    
    suspend fun getAllProviderConfigs(): List<ProviderConfig> = withContext(Dispatchers.IO) {
        val configs = mutableListOf<ProviderConfig>()
        for (provider in AIProvider.values()) {
            getProviderConfig(provider)?.let { configs.add(it) }
        }
        configs
    }
    
    suspend fun deleteProviderConfig(provider: AIProvider) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .remove(getProviderKey(provider))
            .apply()
    }
    
    suspend fun hasProviderConfig(provider: AIProvider): Boolean = withContext(Dispatchers.IO) {
        encryptedPrefs.contains(getProviderKey(provider))
    }
    
    suspend fun isProviderConfigured(provider: AIProvider): Boolean = withContext(Dispatchers.IO) {
        val config = getProviderConfig(provider)
        config != null && config.apiKey.isNotBlank() && config.isEnabled
    }
    
    suspend fun getConfiguredProviders(): List<AIProvider> = withContext(Dispatchers.IO) {
        AIProvider.values().filter { isProviderConfigured(it) }
    }
    
    suspend fun saveApiKey(provider: AIProvider, apiKey: String) = withContext(Dispatchers.IO) {
        // Use KeystoreManager for additional API key encryption
        val encryptedApiKey = try {
            val encryptedData = keystoreManager.encryptData(apiKey)
            "${encryptedData.data.joinToString(",")}|${encryptedData.iv.joinToString(",")}"
        } catch (e: Exception) {
            // Fallback to the original key if keystore encryption fails
            apiKey
        }
        
        val existingConfig = getProviderConfig(provider) ?: ProviderConfig(
            provider = provider,
            apiKey = ""
        )
        saveProviderConfig(existingConfig.copy(apiKey = encryptedApiKey))
    }
    
    suspend fun getApiKey(provider: AIProvider): String? = withContext(Dispatchers.IO) {
        val storedApiKey = getProviderConfig(provider)?.apiKey?.takeIf { it.isNotBlank() }
        
        // Try to decrypt the API key if it was encrypted with KeystoreManager
        storedApiKey?.let { key ->
            try {
                if (key.contains("|")) {
                    val parts = key.split("|")
                    val data = parts[0].split(",").map { it.toByte() }.toByteArray()
                    val iv = parts[1].split(",").map { it.toByte() }.toByteArray()
                    val encryptedData = KeystoreManager.EncryptedData(data, iv)
                    keystoreManager.decryptData(encryptedData)
                } else {
                    // Return the key as-is if not in encrypted format
                    key
                }
            } catch (e: Exception) {
                // If decryption fails, return the original key
                key
            }
        }
    }
    
    suspend fun validateApiKey(provider: AIProvider, apiKey: String): Boolean = withContext(Dispatchers.IO) {
        when (provider) {
            AIProvider.OPENAI -> apiKey.startsWith("sk-") && apiKey.length > 20
            AIProvider.ANTHROPIC -> apiKey.startsWith("sk-ant-") && apiKey.length > 30
            AIProvider.GOOGLE -> apiKey.length >= 20 // Google API keys vary in format
            AIProvider.OLLAMA -> true // Local instance doesn't need API key
            AIProvider.CUSTOM -> apiKey.isNotBlank() // Basic validation for custom providers
        }
    }
    
    suspend fun clearAllConfigs() = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().clear().apply()
    }
    
    // Preferences for AI settings
    suspend fun saveAIPreference(key: String, value: String) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .putString("pref_$key", value)
            .apply()
    }
    
    suspend fun getAIPreference(key: String, defaultValue: String = ""): String = withContext(Dispatchers.IO) {
        encryptedPrefs.getString("pref_$key", defaultValue) ?: defaultValue
    }
    
    suspend fun saveAIPreference(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .putBoolean("pref_$key", value)
            .apply()
    }
    
    suspend fun getAIPreference(key: String, defaultValue: Boolean): Boolean = withContext(Dispatchers.IO) {
        encryptedPrefs.getBoolean("pref_$key", defaultValue)
    }
    
    suspend fun saveAIPreference(key: String, value: Float) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .putFloat("pref_$key", value)
            .apply()
    }
    
    suspend fun getAIPreference(key: String, defaultValue: Float): Float = withContext(Dispatchers.IO) {
        encryptedPrefs.getFloat("pref_$key", defaultValue)
    }
    
    private fun getProviderKey(provider: AIProvider): String = "provider_${provider.name.lowercase()}"
    
    companion object {
        const val PREF_DEFAULT_PROVIDER = "default_provider"
        const val PREF_DEFAULT_MODEL = "default_model"
        const val PREF_DEFAULT_TEMPERATURE = "default_temperature"
        const val PREF_DEFAULT_MAX_TOKENS = "default_max_tokens"
        const val PREF_STREAMING_ENABLED = "streaming_enabled"
        const val PREF_SAVE_CHAT_HISTORY = "save_chat_history"
        const val PREF_AUTO_TITLE_CHATS = "auto_title_chats"
    }
}
