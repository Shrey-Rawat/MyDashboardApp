package com.mydashboardapp.billing.domain.model

import kotlinx.serialization.Serializable

/**
 * Premium state that represents the current subscription/purchase status
 */
@Serializable
data class PremiumState(
    val isPro: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.NONE,
    val purchaseDate: Long? = null,
    val expiryDate: Long? = null,
    val isTrialActive: Boolean = false,
    val trialEndDate: Long? = null,
    val autoRenewing: Boolean = false
) {
    val isActive: Boolean
        get() = isPro && (expiryDate?.let { it > System.currentTimeMillis() } ?: true)
    
    val isExpired: Boolean
        get() = !isActive && expiryDate != null && expiryDate < System.currentTimeMillis()
    
    val daysUntilExpiry: Int?
        get() = expiryDate?.let { 
            ((it - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
        }
}

/**
 * Types of subscription/purchase options
 */
@Serializable
enum class SubscriptionType(val productId: String) {
    NONE(""),
    MONTHLY("premium_monthly"),
    YEARLY("premium_yearly"),
    LIFETIME("premium_lifetime")
}

/**
 * Product details from Google Play Billing
 */
data class ProductDetails(
    val productId: String,
    val productType: ProductType,
    val title: String,
    val description: String,
    val price: String,
    val priceAmount: Long,
    val currencyCode: String,
    val subscriptionOfferDetails: List<SubscriptionOfferDetail>? = null
)

/**
 * Subscription offer details
 */
data class SubscriptionOfferDetail(
    val offerId: String?,
    val basePlanId: String,
    val offerTags: List<String>,
    val pricingPhases: List<PricingPhase>
)

/**
 * Pricing phase for subscription offers
 */
data class PricingPhase(
    val price: String,
    val priceAmount: Long,
    val currencyCode: String,
    val billingPeriod: String,
    val billingCycleCount: Int,
    val recurrenceMode: Int
)

/**
 * Product types supported by the billing system
 */
enum class ProductType {
    INAPP,
    SUBS
}

/**
 * Purchase information
 */
data class PurchaseInfo(
    val purchaseToken: String,
    val productId: String,
    val purchaseTime: Long,
    val purchaseState: Int,
    val isAcknowledged: Boolean,
    val isAutoRenewing: Boolean,
    val orderId: String?
)

/**
 * Billing result states
 */
sealed class BillingResult<out T> {
    data class Success<T>(val data: T) : BillingResult<T>()
    data class Error(val code: Int, val message: String) : BillingResult<Nothing>()
    data object Loading : BillingResult<Nothing>()
    data object NotInitialized : BillingResult<Nothing>()
}

/**
 * Purchase validation result
 */
sealed class PurchaseValidationResult {
    data object Valid : PurchaseValidationResult()
    data class Invalid(val reason: String) : PurchaseValidationResult()
    data object Pending : PurchaseValidationResult()
}
