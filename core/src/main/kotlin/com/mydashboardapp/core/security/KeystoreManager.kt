package com.mydashboardapp.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages secure key storage using Android Keystore for database encryption
 * and other sensitive operations. Avoids reflection to keep size down.
 */
@Singleton
class KeystoreManager @Inject constructor(
    private val context: Context
) {
    
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    /**
     * Generates or retrieves the database encryption key
     */
    fun getDatabaseKey(): ByteArray {
        return if (keyStore.containsAlias(DATABASE_KEY_ALIAS)) {
            getExistingKey(DATABASE_KEY_ALIAS)
        } else {
            generateDatabaseKey()
        }
    }
    
    /**
     * Generates or retrieves an API key encryption key
     */
    fun getApiKeyEncryptionKey(): SecretKey {
        return if (keyStore.containsAlias(API_KEY_ALIAS)) {
            keyStore.getKey(API_KEY_ALIAS, null) as SecretKey
        } else {
            generateApiKeyEncryptionKey()
        }
    }
    
    /**
     * Encrypts sensitive data using the API key encryption key
     */
    fun encryptData(data: String): EncryptedData {
        val secretKey = getApiKeyEncryptionKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }
        
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv
        
        return EncryptedData(encryptedBytes, iv)
    }
    
    /**
     * Decrypts sensitive data using the API key encryption key
     */
    fun decryptData(encryptedData: EncryptedData): String {
        val secretKey = getApiKeyEncryptionKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, encryptedData.iv))
        }
        
        val decryptedBytes = cipher.doFinal(encryptedData.data)
        return String(decryptedBytes)
    }
    
    /**
     * Clears all keys from the keystore (for security reset)
     */
    fun clearAllKeys() {
        try {
            keyStore.deleteEntry(DATABASE_KEY_ALIAS)
            keyStore.deleteEntry(API_KEY_ALIAS)
        } catch (e: Exception) {
            // Keys may not exist, which is fine
        }
    }
    
    private fun generateDatabaseKey(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            DATABASE_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(false) // For database consistency
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        val secretKey = keyGenerator.generateKey()
        
        // For SQLCipher, we need a byte array key
        return generateKeyFromSecret(secretKey)
    }
    
    private fun generateApiKeyEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            API_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    private fun getExistingKey(alias: String): ByteArray {
        val secretKey = keyStore.getKey(alias, null) as SecretKey
        return generateKeyFromSecret(secretKey)
    }
    
    /**
     * Generates a deterministic key derivation for database encryption
     * This ensures the same key is generated each time for the same device
     */
    private fun generateKeyFromSecret(secretKey: SecretKey): ByteArray {
        // Use a fixed IV for database key derivation to ensure consistency
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val fixedIv = ByteArray(12) // All zeros for deterministic key generation
        val gcmSpec = GCMParameterSpec(128, fixedIv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        
        // Use app package name as deterministic input
        val input = context.packageName.toByteArray()
        return cipher.doFinal(input).take(32).toByteArray() // 256-bit key
    }
    
    data class EncryptedData(
        val data: ByteArray,
        val iv: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as EncryptedData
            
            if (!data.contentEquals(other.data)) return false
            if (!iv.contentEquals(other.iv)) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            return result
        }
    }
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DATABASE_KEY_ALIAS = "database_encryption_key"
        private const val API_KEY_ALIAS = "api_key_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
