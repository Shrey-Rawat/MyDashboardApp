package com.mydashboardapp.auth

import com.mydashboardapp.auth.domain.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthModelsTest {
    
    @Test
    fun `PhoneNumber toInternationalFormat returns correct format`() {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        assertEquals("+15551234567", phoneNumber.toInternationalFormat())
    }
    
    @Test
    fun `PhoneNumber toDisplayFormat returns correct format`() {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        assertEquals("+1 5551234567", phoneNumber.toDisplayFormat())
    }
    
    @Test
    fun `PhoneNumber fromInternationalFormat parses US number correctly`() {
        val phoneNumber = PhoneNumber.fromInternationalFormat("+15551234567")
        assertNotNull(phoneNumber)
        assertEquals("+1", phoneNumber.countryCode)
        assertEquals("5551234567", phoneNumber.number)
    }
    
    @Test
    fun `PhoneNumber fromInternationalFormat parses UK number correctly`() {
        val phoneNumber = PhoneNumber.fromInternationalFormat("+441234567890")
        assertNotNull(phoneNumber)
        assertEquals("+44", phoneNumber.countryCode)
        assertEquals("1234567890", phoneNumber.number)
    }
    
    @Test
    fun `PhoneNumber fromInternationalFormat returns null for invalid format`() {
        val phoneNumber = PhoneNumber.fromInternationalFormat("123")
        assertNull(phoneNumber)
    }
    
    @Test
    fun `OtpCode validates correct 6-digit code`() {
        val otpCode = OtpCode("123456")
        assertEquals("123456", otpCode.code)
    }
    
    @Test
    fun `OtpCode throws exception for invalid length`() {
        assertThrows<IllegalArgumentException> {
            OtpCode("12345")
        }
        
        assertThrows<IllegalArgumentException> {
            OtpCode("1234567")
        }
    }
    
    @Test
    fun `OtpCode throws exception for non-numeric code`() {
        assertThrows<IllegalArgumentException> {
            OtpCode("12345a")
        }
        
        assertThrows<IllegalArgumentException> {
            OtpCode("abcdef")
        }
    }
    
    @Test
    fun `AuthUser creation with valid data`() {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        val user = AuthUser(
            uid = "test-uid",
            phoneNumber = phoneNumber,
            isAnonymous = false,
            createdAt = 1000L,
            lastSignInAt = 2000L
        )
        
        assertEquals("test-uid", user.uid)
        assertEquals(phoneNumber, user.phoneNumber)
        assertEquals(false, user.isAnonymous)
        assertEquals(1000L, user.createdAt)
        assertEquals(2000L, user.lastSignInAt)
    }
    
    @Test
    fun `AuthUser defaults work correctly`() {
        val phoneNumber = PhoneNumber("+1", "5551234567")
        val currentTime = System.currentTimeMillis()
        
        val user = AuthUser(
            uid = "test-uid",
            phoneNumber = phoneNumber
        )
        
        assertEquals("test-uid", user.uid)
        assertEquals(phoneNumber, user.phoneNumber)
        assertEquals(false, user.isAnonymous) // Default value
        // Allow some time variance for creation/sign-in times
        assert(user.createdAt >= currentTime - 1000)
        assert(user.lastSignInAt >= currentTime - 1000)
    }
    
    @Test
    fun `PhoneAuthConfig defaults are correct`() {
        val config = PhoneAuthConfig()
        
        assertEquals(60L, config.timeoutDurationSeconds)
        assertEquals(true, config.enableAutoRetrieval)
        assertEquals(3, config.maxRetryAttempts)
    }
    
    @Test
    fun `PhoneAuthConfig custom values work`() {
        val config = PhoneAuthConfig(
            timeoutDurationSeconds = 120L,
            enableAutoRetrieval = false,
            maxRetryAttempts = 5
        )
        
        assertEquals(120L, config.timeoutDurationSeconds)
        assertEquals(false, config.enableAutoRetrieval)
        assertEquals(5, config.maxRetryAttempts)
    }
}
