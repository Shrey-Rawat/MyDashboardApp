package com.mydashboardapp.billing.domain.repository

import com.mydashboardapp.billing.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for billing operations
 */
interface BillingRepository {
    
    /**
     * Current premium state as a Flow
     */
    val premiumState: Flow<PremiumState>
    
    /**
     * Available products as a Flow
     */
    val products: Flow<BillingResult<List<ProductDetails>>>
    
    /**
     * Initialize the billing client
     */
    suspend fun initialize(): BillingResult<Unit>
    
    /**
     * Start purchase flow for a product
     */
    suspend fun startPurchaseFlow(
        productDetails: ProductDetails,
        offerToken: String? = null
    ): BillingResult<Unit>
    
    /**
     * Query purchases from Google Play
     */
    suspend fun queryPurchases(): BillingResult<List<PurchaseInfo>>
    
    /**
     * Acknowledge a purchase
     */
    suspend fun acknowledgePurchase(purchaseToken: String): BillingResult<Unit>
    
    /**
     * Consume a purchase (for consumable products)
     */
    suspend fun consumePurchase(purchaseToken: String): BillingResult<Unit>
    
    /**
     * Validate a purchase with backend
     */
    suspend fun validatePurchase(purchaseInfo: PurchaseInfo): PurchaseValidationResult
    
    /**
     * Check if a specific feature is available (based on current premium state)
     */
    fun isFeatureAvailable(feature: PremiumFeature): Flow<Boolean>
    
    /**
     * Get feature limit for a specific feature
     */
    fun getFeatureLimit(feature: PremiumFeature): Flow<Int>
    
    /**
     * Update premium state (typically called after successful purchase validation)
     */
    suspend fun updatePremiumState(premiumState: PremiumState)
    
    /**
     * Clear premium state (for logout or data reset)
     */
    suspend fun clearPremiumState()
    
    /**
     * Check if trial is available
     */
    suspend fun isTrialAvailable(): Boolean
    
    /**
     * Start trial period
     */
    suspend fun startTrial(): BillingResult<Unit>
}

/**
 * Premium features that can be gated
 */
enum class PremiumFeature(
    val freeLimit: Int,
    val displayName: String
) {
    NUTRITION_ENTRIES(10, "Nutrition Entries"),
    WORKOUTS(5, "Workout Plans"),
    TASKS(20, "Active Tasks"),
    ACCOUNTS(3, "Financial Accounts"),
    INVENTORY_ITEMS(50, "Inventory Items"),
    ADVANCED_ANALYTICS(-1, "Advanced Analytics"),
    EXPORT_DATA(-1, "Data Export"),
    CUSTOM_CATEGORIES(-1, "Custom Categories"),
    BULK_OPERATIONS(-1, "Bulk Operations"),
    AI_SUGGESTIONS(-1, "AI Suggestions"),
    CLOUD_SYNC(-1, "Cloud Sync")
}
