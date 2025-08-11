package com.mydashboardapp.auth

import android.app.Activity
import com.mydashboardapp.auth.data.stub.StubAuthRepository
import com.mydashboardapp.auth.domain.model.*
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StubAuthRepositoryTest {
    
    private lateinit var repository: StubAuthRepository
    private lateinit var mockActivity: Activity
    
    @BeforeEach
    fun setup() {
        repository = StubAuthRepository()
        mockActivity = mockk(relaxed = true)
    }
    
    @Test
    fun `initial state is unauthenticated`() = runTest {
        val authState = repository.authState.first()
        assertEquals(AuthState.Unauthenticated, authState)
        
        val phoneState = repository.phoneVerificationState.first()
        assertEquals(PhoneVerificationState.Idle, phoneState)
        
        assertNull(repository.getCurrentUser())
        assertFalse(repository.isAuthenticated())
    }
    
    @Test
    fun `sendOtp completes successfully and transitions states`() = runTest {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        
        // Start OTP sending
        val result = repository.sendOtp(phoneNumber, mockActivity)
        
        // Should return success
        assertTrue(result is AuthResult.Success)
        assertEquals("OTP sent successfully (stub implementation)", result.data)
        
        // Phone verification state should be CodeSent
        val phoneState = repository.phoneVerificationState.first()
        assertTrue(phoneState is PhoneVerificationState.CodeSent)
        assertEquals(phoneNumber, phoneState.phoneNumber)
        assertTrue(phoneState.verificationId.startsWith("stub_verification_"))
        assertTrue(phoneState.resendToken?.startsWith("stub_resend_token_") ?: false)
    }
    
    @Test
    fun `verifyOtp with correct code succeeds`() = runTest {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        
        // First send OTP
        repository.sendOtp(phoneNumber, mockActivity)
        val phoneState = repository.phoneVerificationState.first() as PhoneVerificationState.CodeSent
        val verificationId = phoneState.verificationId
        
        // Verify with correct code
        val otpCode = OtpCode("123456")
        val result = repository.verifyOtp(verificationId, otpCode)
        
        // Should return success with user
        assertTrue(result is AuthResult.Success)
        val user = result.data
        assertEquals(phoneNumber, user.phoneNumber)
        assertFalse(user.isAnonymous)
        assertTrue(user.uid.startsWith("stub_user_"))
        
        // Auth state should be authenticated
        val authState = repository.authState.first()
        assertTrue(authState is AuthState.Authenticated)
        assertEquals(user, authState.user)
        
        // Repository should report authenticated
        assertTrue(repository.isAuthenticated())
        assertEquals(user, repository.getCurrentUser())
    }
    
    @Test
    fun `verifyOtp with invalid verification ID fails`() = runTest {
        val otpCode = OtpCode("123456")
        val result = repository.verifyOtp("invalid_id", otpCode)
        
        assertTrue(result is AuthResult.Error)
        assertEquals("Invalid or expired verification ID", result.message)
        
        // Phone verification state should show error
        val phoneState = repository.phoneVerificationState.first()
        assertTrue(phoneState is PhoneVerificationState.Error)
        assertEquals("Invalid verification ID", phoneState.message)
    }
    
    @Test
    fun `verifyOtp with invalid code fails`() = runTest {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        
        // First send OTP
        repository.sendOtp(phoneNumber, mockActivity)
        val phoneState = repository.phoneVerificationState.first() as PhoneVerificationState.CodeSent
        val verificationId = phoneState.verificationId
        
        // Verify with wrong code
        val otpCode = OtpCode("000000")
        val result = repository.verifyOtp(verificationId, otpCode)
        
        assertTrue(result is AuthResult.Error)
        assertTrue(result.message.contains("Invalid verification code"))
    }
    
    @Test
    fun `verifyOtp accepts any 6-digit code for testing`() = runTest {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        
        // First send OTP
        repository.sendOtp(phoneNumber, mockActivity)
        val phoneState = repository.phoneVerificationState.first() as PhoneVerificationState.CodeSent
        val verificationId = phoneState.verificationId
        
        // Verify with any 6-digit code
        val otpCode = OtpCode("987654")
        val result = repository.verifyOtp(verificationId, otpCode)
        
        assertTrue(result is AuthResult.Success)
    }
    
    @Test
    fun `signInAnonymously creates anonymous user`() = runTest {
        val result = repository.signInAnonymously()
        
        assertTrue(result is AuthResult.Success)
        val user = result.data
        assertTrue(user.isAnonymous)
        assertTrue(user.uid.startsWith("stub_anonymous_"))
        assertEquals(PhoneNumber("+1", "0000000000"), user.phoneNumber)
        
        assertTrue(repository.isAuthenticated())
        
        val authState = repository.authState.first()
        assertTrue(authState is AuthState.Authenticated)
        assertEquals(user, authState.user)
    }
    
    @Test
    fun `signOut clears authentication state`() = runTest {
        // First sign in
        repository.signInAnonymously()
        assertTrue(repository.isAuthenticated())
        
        // Sign out
        val result = repository.signOut()
        
        assertTrue(result is AuthResult.Success)
        assertFalse(repository.isAuthenticated())
        assertNull(repository.getCurrentUser())
        
        val authState = repository.authState.first()
        assertEquals(AuthState.Unauthenticated, authState)
        
        val phoneState = repository.phoneVerificationState.first()
        assertEquals(PhoneVerificationState.Idle, phoneState)
    }
    
    @Test
    fun `linkAnonymousWithPhone converts anonymous to permanent account`() = runTest {
        // First sign in anonymously
        repository.signInAnonymously()
        val anonymousUser = repository.getCurrentUser()!!
        assertTrue(anonymousUser.isAnonymous)
        
        // Start phone verification
        val phoneNumber = PhoneNumber("+1", "5551234567")
        repository.sendOtp(phoneNumber, mockActivity)
        val phoneState = repository.phoneVerificationState.first() as PhoneVerificationState.CodeSent
        val verificationId = phoneState.verificationId
        
        // Link with phone
        val otpCode = OtpCode("123456")
        val result = repository.linkAnonymousWithPhone(verificationId, otpCode)
        
        assertTrue(result is AuthResult.Success)
        val linkedUser = result.data
        assertFalse(linkedUser.isAnonymous)
        assertEquals(phoneNumber, linkedUser.phoneNumber)
        assertEquals(anonymousUser.uid, linkedUser.uid) // Should preserve the UID
    }
    
    @Test
    fun `refreshToken succeeds when user is authenticated`() = runTest {
        // Sign in first
        repository.signInAnonymously()
        
        val result = repository.refreshToken()
        assertTrue(result is AuthResult.Success)
    }
    
    @Test
    fun `refreshToken fails when no user is authenticated`() = runTest {
        val result = repository.refreshToken()
        assertTrue(result is AuthResult.Error)
        assertEquals("No authenticated user", result.message)
    }
    
    @Test
    fun `deleteAccount removes user and resets state`() = runTest {
        // Sign in first
        repository.signInAnonymously()
        assertTrue(repository.isAuthenticated())
        
        // Delete account
        val result = repository.deleteAccount()
        
        assertTrue(result is AuthResult.Success)
        assertFalse(repository.isAuthenticated())
        assertNull(repository.getCurrentUser())
        
        val authState = repository.authState.first()
        assertEquals(AuthState.Unauthenticated, authState)
    }
}
