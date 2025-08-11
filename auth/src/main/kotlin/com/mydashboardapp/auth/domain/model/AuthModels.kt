package com.mydashboardapp.auth.domain.model

/**
 * Represents a phone number for authentication
 */
data class PhoneNumber(
    val countryCode: String,
    val number: String
) {
    /**
     * Returns the full international phone number format
     */
    fun toInternationalFormat(): String = "$countryCode$number"
    
    /**
     * Returns formatted display string
     */
    fun toDisplayFormat(): String = "$countryCode $number"
    
    companion object {
        /**
         * Parse phone number from international format
         */
        fun fromInternationalFormat(phoneNumber: String): PhoneNumber? {
            if (phoneNumber.length < 4) return null
            
            // Simple parsing for common country codes
            return when {
                phoneNumber.startsWith("+1") -> PhoneNumber("+1", phoneNumber.substring(2))
                phoneNumber.startsWith("+44") -> PhoneNumber("+44", phoneNumber.substring(3))
                phoneNumber.startsWith("+91") -> PhoneNumber("+91", phoneNumber.substring(3))
                phoneNumber.startsWith("+") -> {
                    // Generic parsing for other country codes (assume 1-4 digit codes)
                    val codeEnd = phoneNumber.indexOfFirst { !it.isDigit() && it != '+' }
                        .takeIf { it > 0 } ?: minOf(phoneNumber.length, 5)
                    PhoneNumber(phoneNumber.substring(0, codeEnd), phoneNumber.substring(codeEnd))
                }
                else -> null
            }
        }
    }
}

/**
 * Represents OTP verification code
 */
data class OtpCode(
    val code: String
) {
    init {
        require(code.length == 6 && code.all { it.isDigit() }) {
            "OTP code must be exactly 6 digits"
        }
    }
}

/**
 * Represents an authenticated user
 */
data class AuthUser(
    val uid: String,
    val phoneNumber: PhoneNumber,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInAt: Long = System.currentTimeMillis()
)

/**
 * Authentication states
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String, val throwable: Throwable? = null) : AuthState()
}

/**
 * Phone verification states
 */
sealed class PhoneVerificationState {
    object Idle : PhoneVerificationState()
    object SendingCode : PhoneVerificationState()
    data class CodeSent(
        val verificationId: String,
        val phoneNumber: PhoneNumber,
        val resendToken: String? = null
    ) : PhoneVerificationState()
    object VerifyingCode : PhoneVerificationState()
    data class VerificationComplete(val user: AuthUser) : PhoneVerificationState()
    data class Error(val message: String, val throwable: Throwable? = null) : PhoneVerificationState()
}

/**
 * Result wrapper for authentication operations
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

/**
 * Configuration for phone authentication
 */
data class PhoneAuthConfig(
    val timeoutDurationSeconds: Long = 60L,
    val enableAutoRetrieval: Boolean = true,
    val maxRetryAttempts: Int = 3
)
