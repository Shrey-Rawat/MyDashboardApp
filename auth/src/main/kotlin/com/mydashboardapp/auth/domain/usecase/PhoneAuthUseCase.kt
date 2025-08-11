package com.mydashboardapp.auth.domain.usecase

import android.app.Activity
import com.mydashboardapp.auth.domain.model.*
import com.mydashboardapp.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for phone number authentication operations
 * 
 * Provides high-level operations for phone authentication that can be
 * used by ViewModels or other presentation layer components.
 */
@Singleton
class PhoneAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Current authentication state
     */
    val authState: Flow<AuthState> = authRepository.authState
    
    /**
     * Phone verification state
     */
    val phoneVerificationState: Flow<PhoneVerificationState> = authRepository.phoneVerificationState
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): AuthUser? {
        return authRepository.getCurrentUser()
    }
    
    /**
     * Check if user is currently authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return authRepository.isAuthenticated()
    }
    
    /**
     * Start phone number verification process
     * 
     * @param phoneNumber The phone number to verify
     * @param activity Required Android activity for SMS auto-retrieval
     * @param config Optional configuration for phone authentication
     * @return Result indicating success or failure
     */
    suspend fun startPhoneVerification(
        phoneNumber: PhoneNumber,
        activity: Activity,
        config: PhoneAuthConfig = PhoneAuthConfig()
    ): AuthResult<String> {
        return authRepository.sendOtp(phoneNumber, activity, config)
    }
    
    /**
     * Complete phone number verification with OTP code
     * 
     * @param verificationId ID received from startPhoneVerification
     * @param otpCode The 6-digit OTP code entered by user
     * @return Result with authenticated user or error
     */
    suspend fun completePhoneVerification(
        verificationId: String,
        otpCode: String
    ): AuthResult<AuthUser> {
        return try {
            val otp = OtpCode(otpCode)
            authRepository.verifyOtp(verificationId, otp)
        } catch (e: IllegalArgumentException) {
            AuthResult.Error("Invalid OTP code format. Must be 6 digits.", e)
        }
    }
    
    /**
     * Resend OTP code to phone number
     * 
     * @param phoneNumber The phone number to resend OTP to
     * @param resendToken Token received from previous verification attempt
     * @param activity Required Android activity for SMS auto-retrieval
     * @return Result indicating success or failure
     */
    suspend fun resendOtp(
        phoneNumber: PhoneNumber,
        resendToken: String,
        activity: Activity
    ): AuthResult<String> {
        return authRepository.resendOtp(phoneNumber, resendToken, activity)
    }
    
    /**
     * Sign in anonymously for guest access
     * 
     * @return Result with anonymous user or error
     */
    suspend fun signInAnonymously(): AuthResult<AuthUser> {
        return authRepository.signInAnonymously()
    }
    
    /**
     * Link current anonymous account with phone number
     * 
     * This allows upgrading a guest account to a permanent account
     * 
     * @param verificationId ID from phone verification process
     * @param otpCode The 6-digit OTP code
     * @return Result with linked user or error
     */
    suspend fun linkAnonymousAccountWithPhone(
        verificationId: String,
        otpCode: String
    ): AuthResult<AuthUser> {
        return try {
            val otp = OtpCode(otpCode)
            authRepository.linkAnonymousWithPhone(verificationId, otp)
        } catch (e: IllegalArgumentException) {
            AuthResult.Error("Invalid OTP code format. Must be 6 digits.", e)
        }
    }
    
    /**
     * Update phone number for current user
     * 
     * @param newPhoneNumber The new phone number to update to
     * @param verificationId ID from phone verification process
     * @param otpCode The 6-digit OTP code
     * @return Result with updated user or error
     */
    suspend fun updatePhoneNumber(
        newPhoneNumber: PhoneNumber,
        verificationId: String,
        otpCode: String
    ): AuthResult<AuthUser> {
        return try {
            val otp = OtpCode(otpCode)
            authRepository.updatePhoneNumber(newPhoneNumber, verificationId, otp)
        } catch (e: IllegalArgumentException) {
            AuthResult.Error("Invalid OTP code format. Must be 6 digits.", e)
        }
    }
    
    /**
     * Sign out current user
     * 
     * @return Result indicating success or failure
     */
    suspend fun signOut(): AuthResult<Unit> {
        return authRepository.signOut()
    }
    
    /**
     * Refresh authentication token
     * 
     * @return Result indicating success or failure
     */
    suspend fun refreshToken(): AuthResult<Unit> {
        return authRepository.refreshToken()
    }
    
    /**
     * Delete current user account permanently
     * 
     * @return Result indicating success or failure
     */
    suspend fun deleteAccount(): AuthResult<Unit> {
        return authRepository.deleteAccount()
    }
    
    /**
     * Parse phone number from international format string
     * 
     * @param phoneNumberString Phone number in international format (e.g., "+1234567890")
     * @return Parsed PhoneNumber object or null if invalid
     */
    fun parsePhoneNumber(phoneNumberString: String): PhoneNumber? {
        return PhoneNumber.fromInternationalFormat(phoneNumberString)
    }
    
    /**
     * Validate OTP code format
     * 
     * @param otpCode The OTP code to validate
     * @return true if valid, false otherwise
     */
    fun isValidOtpCode(otpCode: String): Boolean {
        return try {
            OtpCode(otpCode)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
