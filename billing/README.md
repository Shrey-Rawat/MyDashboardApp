# Billing & Monetization Module

This module implements Google Play Billing v6 integration with feature flags and graceful degradation for free vs premium users.

## Overview

The billing module provides:
- Google Play Billing v6 integration
- Feature flags injected via dependency injection
- UI components for premium upgrade flows
- Graceful degradation with blur effects and CTAs
- Trial management
- Purchase validation

## Architecture

### Core Components

1. **Domain Layer**
   - `PremiumState`: Represents current subscription status
   - `BillingRepository`: Interface for billing operations
   - `PremiumFeature`: Enum of gated features

2. **Data Layer**
   - `BillingRepositoryImpl`: Google Play Billing v6 implementation
   - Integration with UserPreferences for state persistence

3. **UI Layer**
   - `PremiumUpgradeScreen`: Main upgrade/subscription screen
   - `PremiumComponents`: Reusable UI components for feature gating
   - `PremiumUpgradeViewModel`: Handles billing UI logic

## Usage

### 1. Dependency Injection

The billing repository is automatically injected via Hilt:

```kotlin
@HiltViewModel
class MyFeatureViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {
    
    val premiumState = billingRepository.premiumState
    val isFeatureAvailable = billingRepository.isFeatureAvailable(PremiumFeature.ADVANCED_ANALYTICS)
}
```

### 2. Feature Gating

Use `PremiumFeatureGate` to wrap premium features:

```kotlin
@Composable
fun MyFeatureScreen() {
    val isPremium by remember { billingRepository.premiumState.map { it.isPro } }
        .collectAsState(initial = false)
    
    PremiumFeatureGate(
        feature = PremiumFeature.ADVANCED_ANALYTICS,
        isPremium = isPremium,
        onUpgradeClick = { /* Navigate to upgrade screen */ }
    ) {
        // Premium content here
        AdvancedAnalyticsContent()
    }
}
```

### 3. Usage Limits

Display usage indicators for limited features:

```kotlin
@Composable
fun FeatureUsageCard() {
    val premiumState by billingRepository.premiumState.collectAsState()
    val limit by billingRepository.getFeatureLimit(PremiumFeature.NUTRITION_ENTRIES)
        .collectAsState(initial = 10)
    
    FeatureUsageIndicator(
        feature = PremiumFeature.NUTRITION_ENTRIES,
        currentUsage = currentEntries,
        limit = limit,
        isPremium = premiumState.isPro,
        onUpgradeClick = { /* Navigate to upgrade */ }
    )
}
```

### 4. Conditional UI

Show different UI based on premium status:

```kotlin
@Composable
fun ConditionalFeatureUI() {
    val premiumState by billingRepository.premiumState.collectAsState()
    
    if (premiumState.isPro) {
        PremiumFeatureUI()
        PremiumBadge(premiumState)
    } else {
        FreeFeatureUI()
        if (currentUsage >= limit) {
            UpgradeButton(onClick = onUpgradeClick)
        }
    }
}
```

## Premium Features

The system supports the following premium features:

- **Limited Features** (have free tier limits):
  - Nutrition Entries (10 free)
  - Workouts (5 free)
  - Tasks (20 free)
  - Financial Accounts (3 free)
  - Inventory Items (50 free)

- **Premium-Only Features**:
  - Advanced Analytics
  - Data Export
  - Custom Categories
  - Bulk Operations
  - AI Suggestions
  - Cloud Sync

## Subscription Options

Three subscription tiers are configured:

1. **Monthly** (`premium_monthly`): Monthly subscription
2. **Yearly** (`premium_yearly`): Annual subscription with savings
3. **Lifetime** (`premium_lifetime`): One-time purchase

## Trial System

7-day free trial is available for new users:

```kotlin
// Check trial availability
val isTrialAvailable = billingRepository.isTrialAvailable()

// Start trial
billingRepository.startTrial()
```

## Integration Steps

### 1. Add Dependencies

The billing module is already configured with Google Play Billing v6.1.0.

### 2. Configure Products

Update product IDs in `SubscriptionType` enum if needed:

```kotlin
enum class SubscriptionType(val productId: String) {
    MONTHLY("your_monthly_product_id"),
    YEARLY("your_yearly_product_id"),
    LIFETIME("your_lifetime_product_id")
}
```

### 3. Initialize Billing

Billing is automatically initialized when the repository is first accessed.

### 4. Handle Activity Context

For purchase flows, ensure your Activity context is available:

```kotlin
// In your activity or fragment
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var billingRepository: BillingRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Billing will use this activity context for purchase flows
    }
}
```

## UI Components

### PremiumFeatureGate
Wraps content and shows upgrade prompt when locked.

### FeatureUsageIndicator  
Shows current usage vs limits with progress indicator.

### PremiumBadge
Displays "PRO" badge for premium users.

### TrialBanner
Shows trial status and remaining days.

### PremiumUpgradeScreen
Full-screen upgrade flow with subscription options.

## Graceful Degradation Strategies

1. **Blur Effect**: Content is shown but blurred with overlay
2. **Placeholder**: Show static placeholder with upgrade CTA
3. **Limited Functionality**: Basic version with upgrade prompts
4. **Usage Limits**: Allow limited usage with progress indicators

## Testing

The module includes example implementations showing different degradation strategies. See `FeatureGatingExample.kt` for comprehensive usage examples.

## Error Handling

All billing operations return `BillingResult<T>` sealed class:

```kotlin
when (val result = billingRepository.startPurchaseFlow(product)) {
    is BillingResult.Success -> {
        // Handle success
    }
    is BillingResult.Error -> {
        // Handle error with result.code and result.message
    }
    is BillingResult.Loading -> {
        // Show loading state
    }
    is BillingResult.NotInitialized -> {
        // Billing not initialized
    }
}
```

## Security Considerations

- Purchase validation should be implemented server-side
- The current implementation includes basic client-side validation
- Consider implementing receipt verification with your backend
- Store sensitive purchase data securely

## Customization

### Custom Premium Features

Add new features to `PremiumFeature` enum:

```kotlin
enum class PremiumFeature(val freeLimit: Int, val displayName: String) {
    // Existing features...
    MY_CUSTOM_FEATURE(5, "My Custom Feature")
}
```

### Custom UI Themes

Customize the premium UI colors and styling by modifying the color extensions in `PremiumComponents.kt`.

### Product Configuration

Update subscription details, trial periods, and feature limits as needed in the respective model classes.
