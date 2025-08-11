package com.mydashboardapp.auth.data.firebase

import android.app.Activity
import android.util.Log
import com.mydashboardapp.auth.domain.model.*
import com.mydashboardapp.auth.domain.model.AuthResult
import com.mydashboardapp.auth.domain.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Auth implementation of AuthRepository
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    
    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }
    
    private val _phoneVerificationState = MutableStateFlow<PhoneVerificationState>(PhoneVerificationState.Idle)
    
    override val authState: Flow<AuthState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            val state = when {
                user == null -> AuthState.Unauthenticated
                else -> {
                    try {
                        AuthState.Authenticated(user.toAuthUser())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting Firebase user to AuthUser", e)
                        AuthState.Error("Failed to process user data", e)
                    }
                }
            }
            trySend(state)
        }
        
        firebaseAuth.addAuthStateListener(authStateListener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
    
    override val phoneVerificationState: Flow<PhoneVerificationState> = _phoneVerificationState.asStateFlow()
    
    override suspend fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toAuthUser()
    }
    
    override suspend fun sendOtp(
        phoneNumber: PhoneNumber,
        activity: Activity,
        config: PhoneAuthConfig
    ): AuthResult<String> {
        return try {
            _phoneVerificationState.value = PhoneVerificationState.SendingCode
            
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber.toInternationalFormat())
                .setTimeout(config.timeoutDurationSeconds, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(createPhoneAuthCallbacks(phoneNumber))
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
            
            // Wait for callback to set the state
            AuthResult.Success("OTP send initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send OTP", e)
            val errorMessage = when (e) {
                is FirebaseTooManyRequestsException -> "Too many requests. Please try again later."
                is FirebaseAuthInvalidUserException -> "Invalid phone number format."
                else -> "Failed to send OTP: ${e.message}"
            }
            _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage, e)
            AuthResult.Error(errorMessage, e)
        }
    }
    
    override suspend fun verifyOtp(
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser> {
        return try {
            _phoneVerificationState.value = PhoneVerificationState.VerifyingCode
            
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.code)
            val result = firebaseAuth.signInWithCredential(credential).await()
            
            val user = result.user?.toAuthUser()
            if (user != null) {
                _phoneVerificationState.value = PhoneVerificationState.VerificationComplete(user)
                AuthResult.Success(user)
            } else {
                val errorMessage = "Authentication succeeded but user data is null"
                _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage)
                AuthResult.Error(errorMessage)
            }
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Failed to verify OTP", e)
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid verification code."
                "ERROR_CODE_EXPIRED" -> "Verification code has expired."
                "ERROR_SESSION_EXPIRED" -> "Verification session has expired."
                else -> "Verification failed: ${e.message}"
            }
            _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage, e)
            AuthResult.Error(errorMessage, e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during OTP verification", e)
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
        return try {
            _phoneVerificationState.value = PhoneVerificationState.SendingCode
            
            val resendTokenObj = resendTokens[resendToken]
            if (resendTokenObj == null) {
                return AuthResult.Error("Invalid or expired resend token")
            }
            
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber.toInternationalFormat())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(createPhoneAuthCallbacks(phoneNumber))
                .setForceResendingToken(resendTokenObj)
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
            AuthResult.Success("OTP resend initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resend OTP", e)
            val errorMessage = "Failed to resend OTP: ${e.message}"
            _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage, e)
            AuthResult.Error(errorMessage, e)
        }
    }
    
    override suspend fun signInWithPhoneCredential(
        phoneNumber: PhoneNumber,
        credential: Any
    ): AuthResult<AuthUser> {
        return try {
            if (credential !is PhoneAuthCredential) {
                return AuthResult.Error("Invalid credential type")
            }
            
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.toAuthUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Authentication succeeded but user data is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign in with phone credential", e)
            AuthResult.Error("Sign in failed: ${e.message}", e)
        }
    }
    
    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            firebaseAuth.signOut()
            _phoneVerificationState.value = PhoneVerificationState.Idle
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign out", e)
            AuthResult.Error("Sign out failed: ${e.message}", e)
        }
    }
    
    override suspend fun signInAnonymously(): AuthResult<AuthUser> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            val user = result.user?.toAuthUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Anonymous sign in succeeded but user data is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign in anonymously", e)
            AuthResult.Error("Anonymous sign in failed: ${e.message}", e)
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    override suspend fun refreshToken(): AuthResult<Unit> {
        return try {
            firebaseAuth.currentUser?.getIdToken(true)?.await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh token", e)
            AuthResult.Error("Token refresh failed: ${e.message}", e)
        }
    }
    
    override suspend fun deleteAccount(): AuthResult<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()?.await()
            _phoneVerificationState.value = PhoneVerificationState.Idle
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete account", e)
            AuthResult.Error("Account deletion failed: ${e.message}", e)
        }
    }
    
    override suspend fun linkAnonymousWithPhone(
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.code)
            val result = firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
            
            val user = result?.user?.toAuthUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Account linking succeeded but user data is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to link anonymous account with phone", e)
            AuthResult.Error("Account linking failed: ${e.message}", e)
        }
    }
    
    override suspend fun updatePhoneNumber(
        newPhoneNumber: PhoneNumber,
        verificationId: String,
        otpCode: OtpCode
    ): AuthResult<AuthUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode.code)
            firebaseAuth.currentUser?.updatePhoneNumber(credential)?.await()
            
            val user = firebaseAuth.currentUser?.toAuthUser()
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Phone number update succeeded but user data is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update phone number", e)
            AuthResult.Error("Phone number update failed: ${e.message}", e)
        }
    }
    
    // In-memory storage for resend tokens (in production, consider using a more robust solution)
    private val resendTokens = mutableMapOf<String, PhoneAuthProvider.ForceResendingToken>()
    
    private fun createPhoneAuthCallbacks(phoneNumber: PhoneNumber) = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "Phone verification completed automatically")
            // Auto-verification or instant verification - could auto-sign in here
        }
        
        override fun onVerificationFailed(e: FirebaseException) {
            Log.e(TAG, "Phone verification failed", e)
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format."
                is FirebaseTooManyRequestsException -> "Too many requests. Please try again later."
                else -> "Verification failed: ${e.message}"
            }
            _phoneVerificationState.value = PhoneVerificationState.Error(errorMessage, e)
        }
        
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d(TAG, "OTP code sent successfully")
            
            // Store token in memory for resend functionality
            val tokenId = "token_${System.currentTimeMillis()}"
            resendTokens[tokenId] = token
            
            _phoneVerificationState.value = PhoneVerificationState.CodeSent(
                verificationId = verificationId,
                phoneNumber = phoneNumber,
                resendToken = tokenId
            )
        }
    }
    
    private fun FirebaseUser.toAuthUser(): AuthUser {
        val phoneNumberStr = phoneNumber
        val parsedPhoneNumber = if (phoneNumberStr != null) {
            PhoneNumber.fromInternationalFormat(phoneNumberStr) 
                ?: PhoneNumber("+1", phoneNumberStr.removePrefix("+1")) // Fallback
        } else {
            PhoneNumber("+1", "0000000000") // Default for anonymous users
        }
        
        return AuthUser(
            uid = uid,
            phoneNumber = parsedPhoneNumber,
            isAnonymous = isAnonymous,
            createdAt = metadata?.creationTimestamp ?: System.currentTimeMillis(),
            lastSignInAt = metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
        )
    }
}
