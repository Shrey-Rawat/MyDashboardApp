package com.mydashboardapp.data.repository

import com.mydashboardapp.core.data.BaseRepository
import com.mydashboardapp.data.dao.InventoryDao
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) : BaseRepository() {
    
    // Item operations
    fun getAllActiveItems(): Flow<List<Item>> = inventoryDao.getAllActiveItems()
    
    suspend fun getItemById(id: Long): Item? = inventoryDao.getItemById(id)
    
    suspend fun getItemByBarcode(barcode: String): Item? = inventoryDao.getItemByBarcode(barcode)
    
    suspend fun searchItems(query: String): List<Item> = inventoryDao.searchItems(query)
    
    suspend fun insertItem(item: Item): Long = inventoryDao.insertItem(item)
    
    suspend fun updateItem(item: Item) = inventoryDao.updateItem(item)
    
    suspend fun deleteItem(item: Item) = inventoryDao.deleteItem(item)
    
    // Location operations
    fun getAllActiveLocations(): Flow<List<Location>> = inventoryDao.getAllActiveLocations()
    
    suspend fun getLocationById(id: Long): Location? = inventoryDao.getLocationById(id)
    
    suspend fun insertLocation(location: Location): Long = inventoryDao.insertLocation(location)
    
    suspend fun updateLocation(location: Location) = inventoryDao.updateLocation(location)
    
    // Stock movement operations
    fun getAllStockMovements(): Flow<List<StockMovement>> = inventoryDao.getAllStockMovements()
    
    suspend fun getStockMovementsByItemId(itemId: Long): List<StockMovement> = 
        inventoryDao.getStockMovementsByItemId(itemId)
    
    suspend fun insertStockMovement(stockMovement: StockMovement): Long = 
        inventoryDao.insertStockMovement(stockMovement)
    
    suspend fun getCurrentStockForItem(itemId: Long): Int = 
        inventoryDao.getCurrentStockForItem(itemId)
    
    // Affiliate link operations
    suspend fun getAffiliateLinksByItemId(itemId: Long): List<AffiliateLink> = 
        inventoryDao.getAffiliateLinksByItemId(itemId)
    
    suspend fun getPrimaryAffiliateLinkForItem(itemId: Long): AffiliateLink? = 
        inventoryDao.getPrimaryAffiliateLinkForItem(itemId)
    
    suspend fun insertAffiliateLink(affiliateLink: AffiliateLink): Long = 
        inventoryDao.insertAffiliateLink(affiliateLink)
    
    suspend fun updateAffiliateLink(affiliateLink: AffiliateLink) = 
        inventoryDao.updateAffiliateLink(affiliateLink)
    
    suspend fun incrementAffiliateLinkClick(linkId: Long) {
        val link = inventoryDao.getAffiliateLinkById(linkId)
        link?.let {
            inventoryDao.updateAffiliateLink(
                it.copy(
                    clickCount = it.clickCount + 1,
                    lastClickAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    
    // Analytics operations
    suspend fun getLowStockItems(): List<InventoryDao.ItemWithStock> = 
        inventoryDao.getLowStockItems()
    
    suspend fun getMerchantStats(): List<InventoryDao.MerchantStats> = 
        inventoryDao.getMerchantStats()
    
    suspend fun getItemsByCategory(): List<InventoryDao.CategoryCount> = 
        inventoryDao.getItemsByCategory()
    
    // Low stock alerts
    fun getLowStockItemsFlow(): Flow<List<InventoryDao.ItemWithStock>> = 
        getAllActiveItems().map { items ->
            items.mapNotNull { item ->
                val currentStock = getCurrentStockForItem(item.id)
                if (item.minimumStock != null && currentStock < item.minimumStock) {
                    InventoryDao.ItemWithStock(
                        id = item.id,
                        name = item.name,
                        category = item.category,
                        brand = item.brand,
                        minimumStock = item.minimumStock,
                        currentStock = currentStock
                    )
                } else null
            }
        }
    
    // Create stock movement from barcode scan
    suspend fun recordStockMovementFromScan(
        itemId: Long,
        type: String,
        quantity: Int,
        locationId: Long? = null,
        reason: String = "Barcode scan"
    ): Long {
        val stockMovement = StockMovement(
            itemId = itemId,
            type = type,
            quantity = quantity,
            toLocationId = locationId,
            timestamp = System.currentTimeMillis(),
            reason = reason,
            isVerified = false
        )
        return insertStockMovement(stockMovement)
    }
    
    // Generate affiliate deeplink URL
    suspend fun generateAffiliateDeeplink(
        itemId: Long,
        merchant: String,
        campaignId: String? = null
    ): String? {
        val primaryLink = getPrimaryAffiliateLinkForItem(itemId)
        val merchantLink = getAffiliateLinksByItemId(itemId)
            .find { it.merchant.equals(merchant, ignoreCase = true) }
        
        val link = merchantLink ?: primaryLink
        
        return link?.let { affiliateLink ->
            // Track the click
            incrementAffiliateLinkClick(affiliateLink.id)
            
            // Build deeplink with tracking parameters
            val baseUrl = affiliateLink.affiliateUrl
            val trackingId = affiliateLink.trackingId ?: "default"
            val campaign = campaignId ?: affiliateLink.campaignId ?: "inventory_app"
            
            buildDeeplink(baseUrl, trackingId, campaign)
        }
    }
    
    private fun buildDeeplink(
        affiliateUrl: String,
        trackingId: String,
        campaignId: String
    ): String {
        val separator = if (affiliateUrl.contains("?")) "&" else "?"
        return "${affiliateUrl}${separator}utm_source=MyDashboardApp&utm_medium=affiliate&utm_campaign=${campaignId}&tracking_id=${trackingId}"
    }
}
