# Authentication Module

This module provides phone number OTP authentication with support for both Firebase Auth and stub implementations for open-source builds.

## Features

- 📱 **Phone Number OTP Authentication**: Send and verify SMS OTP codes
- 🔥 **Firebase Auth Integration**: Production-ready authentication with Firebase
- 🧪 **Stub Implementation**: Firebase-free implementation for open-source builds
- 👤 **Anonymous Authentication**: Guest user support with account linking
- 🏗️ **Clean Architecture**: Domain-driven design with repository pattern
- 🔄 **Reactive**: Flow-based state management
- 🧰 **Dependency Injection**: Hilt/Dagger2 integration

## Architecture

The module follows clean architecture principles:

```
auth/
├── domain/                 # Business logic layer
│   ├── model/             # Domain models (PhoneNumber, AuthUser, etc.)
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Use cases for auth operations
├── data/                  # Data layer implementations
│   ├── firebase/          # Firebase Auth implementation
│   └── stub/              # Stub implementation for open-source builds
└── di/                    # Dependency injection configuration
```

## Build Configuration

The module supports two build flavors:

### Firebase Flavor
- Includes Firebase Auth dependencies
- Uses real SMS OTP functionality
- Production-ready authentication
- Build with: `./gradlew assembleFirebaseDebug`

### Stub Flavor  
- No Firebase dependencies
- Simulated authentication for development/testing
- Perfect for open-source builds
- Build with: `./gradlew assembleStubDebug`

## Usage

### Basic Setup

1. **Inject the use case** in your ViewModel or component:

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val phoneAuthUseCase: PhoneAuthUseCase
) : ViewModel() {
    // Implementation
}
```

2. **Observe authentication state**:

```kotlin
phoneAuthUseCase.authState.collect { state ->
    when (state) {
        is AuthState.Unauthenticated -> // Handle unauthenticated state
        is AuthState.Authenticated -> // Handle authenticated state
        is AuthState.Loading -> // Handle loading state
        is AuthState.Error -> // Handle error state
    }
}
```

### Phone Number Authentication

1. **Start phone verification**:

```kotlin
val phoneNumber = PhoneNumber("+1", "5551234567")
val result = phoneAuthUseCase.startPhoneVerification(
    phoneNumber = phoneNumber,
    activity = this@YourActivity
)

when (result) {
    is AuthResult.Success -> {
        // OTP sent successfully
        // Listen to phoneVerificationState for status updates
    }
    is AuthResult.Error -> {
        // Handle error
        showError(result.message)
    }
}
```

2. **Listen for verification state changes**:

```kotlin
phoneAuthUseCase.phoneVerificationState.collect { state ->
    when (state) {
        is PhoneVerificationState.Idle -> // Initial state
        is PhoneVerificationState.SendingCode -> // Sending OTP
        is PhoneVerificationState.CodeSent -> {
            // OTP sent, show verification screen
            val verificationId = state.verificationId
            val resendToken = state.resendToken
        }
        is PhoneVerificationState.VerifyingCode -> // Verifying OTP
        is PhoneVerificationState.VerificationComplete -> {
            // Authentication successful
            val user = state.user
        }
        is PhoneVerificationState.Error -> {
            // Handle verification error
            showError(state.message)
        }
    }
}
```

3. **Complete verification with OTP**:

```kotlin
val result = phoneAuthUseCase.completePhoneVerification(
    verificationId = verificationId,
    otpCode = "123456"
)

when (result) {
    is AuthResult.Success -> {
        // User authenticated successfully
        val user = result.data
        navigateToMainScreen()
    }
    is AuthResult.Error -> {
        // Handle verification error
        showError(result.message)
    }
}
```

### Anonymous Authentication

For guest users:

```kotlin
val result = phoneAuthUseCase.signInAnonymously()
when (result) {
    is AuthResult.Success -> {
        val anonymousUser = result.data
        // User can use app as guest
    }
    is AuthResult.Error -> {
        showError(result.message)
    }
}
```

### Link Anonymous Account

Convert guest account to permanent account:

```kotlin
// First, start phone verification
val phoneNumber = PhoneNumber("+1", "5551234567")
phoneAuthUseCase.startPhoneVerification(phoneNumber, activity)

// When OTP is received, link the account
val result = phoneAuthUseCase.linkAnonymousAccountWithPhone(
    verificationId = verificationId,
    otpCode = "123456"
)
```

## Domain Models

### PhoneNumber
```kotlin
data class PhoneNumber(
    val countryCode: String,  // e.g., "+1"
    val number: String        // e.g., "5551234567"
)
```

### AuthUser
```kotlin
data class AuthUser(
    val uid: String,
    val phoneNumber: PhoneNumber,
    val isAnonymous: Boolean = false,
    val createdAt: Long,
    val lastSignInAt: Long
)
```

### OtpCode
```kotlin
data class OtpCode(
    val code: String  // Must be exactly 6 digits
)
```

## Configuration

### PhoneAuthConfig
```kotlin
data class PhoneAuthConfig(
    val timeoutDurationSeconds: Long = 60L,
    val enableAutoRetrieval: Boolean = true,
    val maxRetryAttempts: Int = 3
)
```

## Testing

### Stub Implementation
When using the stub flavor:
- OTP code `123456` is always accepted
- Any 6-digit code is accepted for testing
- All operations are simulated with realistic delays
- No network calls or external dependencies

### Firebase Implementation
For Firebase testing:
- Configure Firebase project with phone auth enabled
- Add SHA fingerprints to Firebase console
- Test phone numbers can be configured in Firebase console

## Error Handling

The module provides comprehensive error handling:

```kotlin
when (result) {
    is AuthResult.Success -> // Success case
    is AuthResult.Error -> {
        when (result.message) {
            "Too many requests. Please try again later." -> // Rate limiting
            "Invalid verification code." -> // Wrong OTP
            "Verification code has expired." -> // Expired OTP
            else -> // Other errors
        }
    }
}
```

## Dependencies

### Firebase Flavor
- Firebase Auth
- Firebase BOM
- Google Play Services

### Stub Flavor
- No external dependencies
- Self-contained implementation

## License

This module is part of the MyDashboardApp project and follows the same license terms.
