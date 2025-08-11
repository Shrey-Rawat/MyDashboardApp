package com.mydashboardapp.auth.data.stub

import android.app.Activity
import android.util.Log
import com.mydashboardapp.auth.domain.model.*
import com.mydashboardapp.auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Stub implementation of AuthRepository for open-source builds
 * 
 * This implementation simulates authentication behavior without requiring
 * Firebase or other proprietary dependencies. Perfect for:
 * - Open source distributions
 * - Development and testing
 * - Builds that don't want external service dependencies
 */
@Singleton
class StubAuthRepository @Inject constructor() : AuthRepository {
    
    companion object {
        private const val TAG = "StubAuthRepository"
        private const val VERIFICATION_DELAY = 2000L
        private const val STUB_VERIFICATION_CODE = "123456" // For testing
    }
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private val _phoneVerificationState = MutableStateFlow<PhoneVerificationState>(PhoneVerificationState.Idle)
    
    private var currentUser: AuthUser? = null
    private val activeVerifications = mutableMapOf<String, PhoneNumber>()
    
    override val authState: Flow<AuthState> = _authState.asStateFlow()
    override val phoneVerificationState: Flow<PhoneVerificationState> = _phoneVerificationState.asStateFlow()
    
    override suspend fun getCurrentUser(): AuthUser? {
        return currentUser
    }
    
    override suspend fun sendOtp(
        phoneNumber: PhoneNumber,
        activity: Activity,
        config: PhoneAuthConfig
    ): AuthResult<String> {
        Log.d(TAG, "Sending OTP to ${phoneNumber.toDisplayFormat()} (stub implementation)")
        
        return try {
            _phoneVerificationState.value = PhoneVerificationState.SendingCode
            
            // Simulate network delay
            delay(VERIFICATION_DELAY)
            
            // Generate a fake verification ID
            val verificationId = "stub_verification_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
            
            // Store the phone number for verification
            activeVerifications[verificationId] = phoneNumber
            
            // Generate a fake resend token
            val resendToken = "stub_resend_token_${System.currentTimeMillis()}"
            
            _phoneVerificationState.value = PhoneVerificationState.CodeSent(
                verificationId = verificationId,
                phoneNumber = phoneNumber,
                resendToken = resendToken
            )
            
            Log.d(TAG, "OTP sent successfully (stub). Use code: $STUB_VERIFICATION_CODE")
            
            AuthResult.Success("OTP sent successfully (stub implementation)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send OTP (stub)", e)
            val errorMessage = "Failed to send OTP: ${e.message}"
            _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage, e)
            AuthResult.Error(errorMessage, e)
        }
    }
    
    override suspend fun verifyOtp(
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser> {
        Log.d(TAG, "Verifying OTP code: ${otpCode.code} (stub implementation)")
        
        return try {
            _phoneVerificationState.value = PhoneVerificationState.VerifyingCode
            
            // Simulate verification delay
            delay(1000L)
            
            // Check if verification ID is valid
            val phoneNumber = activeVerifications[verificationId]
                ?: return AuthResult.Error("Invalid or expired verification ID").also {
                    _phoneVerificationState.value = PhoneVerificationState.Error("Invalid verification ID")
                }
            
            // In stub mode, accept the predefined code or any 6-digit code for testing
            val isValidCode = otpCode.code == STUB_VERIFICATION_CODE || 
                             (otpCode.code.length == 6 && otpCode.code.all { it.isDigit() })
            
            if (!isValidCode) {
                val errorMessage = "Invalid verification code. Use $STUB_VERIFICATION_CODE for testing."
                _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage)
                return AuthResult.Error(errorMessage)
            }
            
            // Create authenticated user
            val user = AuthUser(
                uid = "stub_user_${phoneNumber.toInternationalFormat().hashCode()}",
                phoneNumber = phoneNumber,
                isAnonymous = false,
                createdAt = System.currentTimeMillis(),
                lastSignInAt = System.currentTimeMillis()
            )
            
            currentUser = user
            _authState.value = AuthState.Authenticated(user)
            _phoneVerificationState.value = PhoneVerificationState.VerificationComplete(user)
            
            // Clean up verification
            activeVerifications.remove(verificationId)
            
            Log.d(TAG, "OTP verification successful (stub)")
            
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify OTP (stub)", e)
            val errorMessage = "Verification failed: ${e.message}"
            _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage, e)
            AuthResult.Error(errorMessage, e)
        }
    }
    
    override suspend fun resendOtp(
        phoneNumber: PhoneNumber,
        resendToken: String,
        activity: Activity
    ): AuthResult<String> {
        Log.d(TAG, "Resending OTP to ${phoneNumber.toDisplayFormat()} (stub implementation)")
        
        // Reuse the sendOtp logic for simplicity
        return sendOtp(phoneNumber, activity)
    }
    
    override suspend fun signInWithPhoneCredential(
        phoneNumber: PhoneNumber,
        credential: Any
    ): AuthResult<AuthUser> {
        Log.d(TAG, "Signing in with phone credential (stub implementation)")
        
        return try {
            // In stub mode, accept any credential
            val user = AuthUser(
                uid = "stub_user_${phoneNumber.toInternationalFormat().hashCode()}",
                phoneNumber = phoneNumber,
                isAnonymous = false,
                createdAt = System.currentTimeMillis(),
                lastSignInAt = System.currentTimeMillis()
            )
            
            currentUser = user
            _authState.value = AuthState.Authenticated(user)
            
            Log.d(TAG, "Sign in with credential successful (stub)")
            
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign in with credential (stub)", e)
            AuthResult.Error("Sign in failed: ${e.message}", e)
        }
    }
    
    override suspend fun signOut(): AuthResult<Unit> {
        Log.d(TAG, "Signing out (stub implementation)")
        
        return try {
            currentUser = null
            _authState.value = AuthState.Unauthenticated
            _phoneVerificationState.value = PhoneVerificationState.Idle
            activeVerifications.clear()
            
            Log.d(TAG, "Sign out successful (stub)")
            
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign out (stub)", e)
            AuthResult.Error("Sign out failed: ${e.message}", e)
        }
    }
    
    override suspend fun signInAnonymously(): AuthResult<AuthUser> {
        Log.d(TAG, "Signing in anonymously (stub implementation)")
        
        return try {
            val user = AuthUser(
                uid = "stub_anonymous_${System.currentTimeMillis()}",
                phoneNumber = PhoneNumber("+1", "0000000000"), // Default for anonymous
                isAnonymous = true,
                createdAt = System.currentTimeMillis(),
                lastSignInAt = System.currentTimeMillis()
            )
            
            currentUser = user
            _authState.value = AuthState.Authenticated(user)
            
            Log.d(TAG, "Anonymous sign in successful (stub)")
            
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign in anonymously (stub)", e)
            AuthResult.Error("Anonymous sign in failed: ${e.message}", e)
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return currentUser != null
    }
    
    override suspend fun refreshToken(): AuthResult<Unit> {
        Log.d(TAG, "Refreshing token (stub implementation)")
        
        return if (currentUser != null) {
            Log.d(TAG, "Token refresh successful (stub)")
            AuthResult.Success(Unit)
        } else {
            Log.w(TAG, "No user to refresh token for (stub)")
            AuthResult.Error("No authenticated user")
        }
    }
    
    override suspend fun deleteAccount(): AuthResult<Unit> {
        Log.d(TAG, "Deleting account (stub implementation)")
        
        return try {
            currentUser = null
            _authState.value = AuthState.Unauthenticated
            _phoneVerificationState.value = PhoneVerificationState.Idle
            activeVerifications.clear()
            
            Log.d(TAG, "Account deletion successful (stub)")
            
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete account (stub)", e)
            AuthResult.Error("Account deletion failed: ${e.message}", e)
        }
    }
    
    override suspend fun linkAnonymousWithPhone(
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser> {
        Log.d(TAG, "Linking anonymous account with phone (stub implementation)")
        
        val phoneNumber = activeVerifications[verificationId]
            ?: return AuthResult.Error("Invalid verification ID")
        
        val existingUser = currentUser
        if (existingUser == null || !existingUser.isAnonymous) {
            return AuthResult.Error("No anonymous user to link")
        }
        
        // Verify OTP first
        val verifyResult = verifyOtp(verificationId, otpCode)
        if (verifyResult is AuthResult.Error) {
            return verifyResult
        }
        
        // Update user to be non-anonymous with phone number
        val linkedUser = existingUser.copy(
            phoneNumber = phoneNumber,
            isAnonymous = false,
            lastSignInAt = System.currentTimeMillis()
        )
        
        currentUser = linkedUser
        _authState.value = AuthState.Authenticated(linkedUser)
        
        Log.d(TAG, "Anonymous account linking successful (stub)")
        
        return AuthResult.Success(linkedUser)
    }
    
    override suspend fun updatePhoneNumber(
        newPhoneNumber: PhoneNumber,
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser> {
        Log.d(TAG, "Updating phone number (stub implementation)")
        
        val existingUser = currentUser
            ?: return AuthResult.Error("No authenticated user")
        
        // Verify OTP first
        val verifyResult = verifyOtp(verificationId, otpCode)
        if (verifyResult is AuthResult.Error) {
            return verifyResult
        }
        
        // Update user's phone number
        val updatedUser = existingUser.copy(
            phoneNumber = newPhoneNumber,
            lastSignInAt = System.currentTimeMillis()
        )
        
        currentUser = updatedUser
        _authState.value = AuthState.Authenticated(updatedUser)
        
        Log.d(TAG, "Phone number update successful (stub)")
        
        return AuthResult.Success(updatedUser)
    }
}
