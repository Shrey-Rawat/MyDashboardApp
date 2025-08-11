package com.mydashboardapp.inventory.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.mydashboardapp.data.entities.AffiliateLink
import com.mydashboardapp.data.dao.InventoryDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling affiliate deeplinks and URI schemes
 */
@Singleton
class DeeplinkService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inventoryDao: InventoryDao
) {
    
    companion object {
        // Custom URI schemes for different merchants
        private const val SCHEME_AMAZON = "amazon"
        private const val SCHEME_BESTBUY = "bestbuy"
        private const val SCHEME_TARGET = "target"
        private const val SCHEME_WALMART = "walmart"
        private const val SCHEME_GENERIC = "https"
        
        // Deeplink tracking parameters
        private const val UTM_SOURCE = "MyDashboardApp"
        private const val UTM_MEDIUM = "affiliate"
    }
    
    data class DeeplinkResult(
        val success: Boolean,
        val url: String?,
        val errorMessage: String? = null,
        val merchant: String,
        val trackingId: String? = null
    )
    
    data class DeeplinkConfig(
        val itemId: Long,
        val merchant: String,
        val campaignId: String? = null,
        val customParams: Map<String, String> = emptyMap(),
        val forceWebFallback: Boolean = false
    )
    
    /**
     * Generate affiliate deeplink for an item
     */
    suspend fun generateDeeplink(config: DeeplinkConfig): DeeplinkResult = withContext(Dispatchers.IO) {
        try {
            val affiliateLinks = inventoryDao.getAffiliateLinksByItemId(config.itemId)
            
            // Find the best matching affiliate link
            val selectedLink = findBestAffiliateLink(affiliateLinks, config.merchant)
                ?: return@withContext DeeplinkResult(
                    success = false,
                    url = null,
                    errorMessage = "No affiliate link found for merchant: ${config.merchant}",
                    merchant = config.merchant
                )
            
            // Generate the deeplink URL
            val deeplinkUrl = buildDeeplink(selectedLink, config)
            
            // Track the click
            inventoryDao.incrementAffiliateLinkClick(selectedLink.id)
            
            DeeplinkResult(
                success = true,
                url = deeplinkUrl,
                merchant = selectedLink.merchant,
                trackingId = selectedLink.trackingId
            )
            
        } catch (e: Exception) {
            DeeplinkResult(
                success = false,
                url = null,
                errorMessage = e.message,
                merchant = config.merchant
            )
        }
    }
    
    /**
     * Find the best affiliate link for a given merchant
     */
    private fun findBestAffiliateLink(
        links: List<AffiliateLink>, 
        preferredMerchant: String
    ): AffiliateLink? {
        // First, try to find exact merchant match
        val exactMatch = links.find { 
            it.merchant.equals(preferredMerchant, ignoreCase = true) && it.isActive 
        }
        if (exactMatch != null) return exactMatch
        
        // Then, try primary link
        val primaryLink = links.find { it.isPrimary && it.isActive }
        if (primaryLink != null) return primaryLink
        
        // Finally, return any active link
        return links.find { it.isActive }
    }
    
    /**
     * Build deeplink URL with proper tracking parameters
     */
    private fun buildDeeplink(affiliateLink: AffiliateLink, config: DeeplinkConfig): String {
        val baseUrl = affiliateLink.affiliateUrl
        val params = mutableMapOf<String, String>()
        
        // Add standard tracking parameters
        params["utm_source"] = UTM_SOURCE
        params["utm_medium"] = UTM_MEDIUM
        params["utm_campaign"] = config.campaignId ?: affiliateLink.campaignId ?: "inventory_deeplink"
        
        // Add affiliate tracking ID if available
        affiliateLink.trackingId?.let { trackingId ->
            params["tracking_id"] = trackingId
        }
        
        // Add custom parameters
        params.putAll(config.customParams)
        
        // Add coupon code if available
        affiliateLink.couponCode?.let { couponCode ->
            params["coupon"] = couponCode
        }
        
        return buildUrlWithParams(baseUrl, params)
    }
    
    /**
     * Build URL with query parameters
     */
    private fun buildUrlWithParams(baseUrl: String, params: Map<String, String>): String {
        if (params.isEmpty()) return baseUrl
        
        val uri = Uri.parse(baseUrl)
        val builder = uri.buildUpon()
        
        params.forEach { (key, value) ->
            builder.appendQueryParameter(key, value)
        }
        
        return builder.build().toString()
    }
    
    /**
     * Launch deeplink with appropriate fallback handling
     */
    suspend fun launchDeeplink(config: DeeplinkConfig): Boolean {
        val deeplinkResult = generateDeeplink(config)
        
        if (!deeplinkResult.success || deeplinkResult.url == null) {
            return false
        }
        
        return try {
            // Try to open native app first if not forcing web fallback
            if (!config.forceWebFallback) {
                val nativeIntent = createNativeAppIntent(deeplinkResult.merchant, deeplinkResult.url)
                if (nativeIntent != null && canHandleIntent(nativeIntent)) {
                    context.startActivity(nativeIntent)
                    return true
                }
            }
            
            // Fallback to web browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplinkResult.url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (canHandleIntent(webIntent)) {
                context.startActivity(webIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create native app intent for specific merchants
     */
    private fun createNativeAppIntent(merchant: String, url: String): Intent? {
        val packageName = getMerchantPackageName(merchant)
        
        return if (packageName != null && isAppInstalled(packageName)) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage(packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            // Try custom URI schemes
            val customScheme = getMerchantCustomScheme(merchant, url)
            if (customScheme != null) {
                Intent(Intent.ACTION_VIEW, Uri.parse(customScheme)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                null
            }
        }
    }
    
    /**
     * Get merchant app package name
     */
    private fun getMerchantPackageName(merchant: String): String? {
        return when (merchant.lowercase()) {
            "amazon" -> "com.amazon.mshop.android.shopping"
            "target" -> "com.target.ui"
            "walmart" -> "com.walmart.android"
            "best buy", "bestbuy" -> "com.bestbuy.android"
            "ebay" -> "com.ebay.mobile"
            "etsy" -> "com.etsy.android"
            "home depot", "homedepot" -> "com.homedepot.android"
            "lowes" -> "com.lowes.android"
            else -> null
        }
    }
    
    /**
     * Generate custom URI scheme for merchants
     */
    private fun getMerchantCustomScheme(merchant: String, originalUrl: String): String? {
        return when (merchant.lowercase()) {
            "amazon" -> {
                // Amazon custom scheme format: amazon://www.amazon.com/dp/PRODUCTID
                val productId = extractAmazonProductId(originalUrl)
                if (productId != null) {
                    "amazon://www.amazon.com/dp/$productId"
                } else null
            }
            "target" -> {
                // Target custom scheme
                val productId = extractTargetProductId(originalUrl)
                if (productId != null) {
                    "target://product/$productId"
                } else null
            }
            else -> null
        }
    }
    
    /**
     * Extract Amazon product ID from URL
     */
    private fun extractAmazonProductId(url: String): String? {
        val patterns = listOf(
            Regex("""/dp/([A-Z0-9]{10})"""),
            Regex("""/gp/product/([A-Z0-9]{10})"""),
            Regex("""amazon\.com/([A-Z0-9]{10})""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
    
    /**
     * Extract Target product ID from URL
     */
    private fun extractTargetProductId(url: String): String? {
        val pattern = Regex("""/p/[^/]*/([A-Z0-9-]+)""")
        val match = pattern.find(url)
        return match?.groupValues?.get(1)
    }
    
    /**
     * Check if an app is installed
     */
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if an intent can be handled
     */
    private fun canHandleIntent(intent: Intent): Boolean {
        return context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
    }
    
    /**
     * Get available merchant apps installed on device
     */
    fun getAvailableMerchantApps(): List<MerchantApp> {
        val knownMerchants = mapOf(
            "Amazon" to "com.amazon.mshop.android.shopping",
            "Target" to "com.target.ui",
            "Walmart" to "com.walmart.android",
            "Best Buy" to "com.bestbuy.android",
            "eBay" to "com.ebay.mobile",
            "Etsy" to "com.etsy.android",
            "Home Depot" to "com.homedepot.android",
            "Lowe's" to "com.lowes.android"
        )
        
        return knownMerchants.mapNotNull { (name, packageName) ->
            if (isAppInstalled(packageName)) {
                MerchantApp(name, packageName)
            } else null
        }
    }
    
    /**
     * Generate shareable affiliate link
     */
    suspend fun generateShareableLink(
        itemId: Long,
        merchant: String,
        campaignId: String = "share"
    ): String? {
        val config = DeeplinkConfig(
            itemId = itemId,
            merchant = merchant,
            campaignId = campaignId,
            customParams = mapOf("utm_content" to "share")
        )
        
        val result = generateDeeplink(config)
        return if (result.success) result.url else null
    }
    
    data class MerchantApp(
        val name: String,
        val packageName: String
    )
}
