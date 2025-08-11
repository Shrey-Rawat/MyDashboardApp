package com.mydashboardapp.auth.domain.repository

import android.app.Activity
import com.mydashboardapp.auth.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 * 
 * Provides a clean interface that can be implemented with Firebase Auth
 * or stub implementations for open-source builds.
 */
interface AuthRepository {
    
    /**
     * Current authentication state as a Flow
     */
    val authState: Flow<AuthState>
    
    /**
     * Phone verification state as a Flow
     */
    val phoneVerificationState: Flow<PhoneVerificationState>
    
    /**
     * Get current authenticated user if available
     */
    suspend fun getCurrentUser(): AuthUser?
    
    /**
     * Send OTP to the specified phone number
     * 
     * @param phoneNumber The phone number to send OTP to
     * @param activity Required Android activity for SMS auto-retrieval
     * @param config Configuration for phone authentication
     * @return Result indicating success or failure
     */
    suspend fun sendOtp(
        phoneNumber: PhoneNumber,
        activity: Activity,
        config: PhoneAuthConfig = PhoneAuthConfig()
    ): AuthResult<String>
    
    /**
     * Verify OTP code with verification ID
     * 
     * @param verificationId ID received from sendOtp
     * @param otpCode The 6-digit OTP code entered by user
     * @return Result with authenticated user or error
     */
    suspend fun verifyOtp(
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser>
    
    /**
     * Resend OTP using resend token
     * 
     * @param phoneNumber The phone number to resend OTP to
     * @param resendToken Token received from previous sendOtp call
     * @param activity Required Android activity for SMS auto-retrieval
     * @return Result indicating success or failure
     */
    suspend fun resendOtp(
        phoneNumber: PhoneNumber,
        resendToken: String,
        activity: Activity
    ): AuthResult<String>
    
    /**
     * Sign in with phone credential directly (for instant verification)
     * 
     * @param phoneNumber The phone number to sign in with
     * @param credential Platform-specific credential object
     * @return Result with authenticated user or error
     */
    suspend fun signInWithPhoneCredential(
        phoneNumber: PhoneNumber,
        credential: Any
    ): AuthResult<AuthUser>
    
    /**
     * Sign out current user
     */
    suspend fun signOut(): AuthResult<Unit>
    
    /**
     * Sign in anonymously for guest users
     */
    suspend fun signInAnonymously(): AuthResult<AuthUser>
    
    /**
     * Check if current user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Refresh authentication token
     */
    suspend fun refreshToken(): AuthResult<Unit>
    
    /**
     * Delete current user account
     */
    suspend fun deleteAccount(): AuthResult<Unit>
    
    /**
     * Link anonymous account with phone number
     */
    suspend fun linkAnonymousWithPhone(
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser>
    
    /**
     * Update phone number for current user
     */
    suspend fun updatePhoneNumber(
        newPhoneNumber: PhoneNumber,
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser>
}
