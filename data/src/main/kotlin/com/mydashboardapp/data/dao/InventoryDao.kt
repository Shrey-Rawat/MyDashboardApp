package com.mydashboardapp.data.dao

import androidx.room.*
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    
    // Item operations
    @Query("SELECT * FROM items WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveItems(): Flow<List<Item>>
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): Item?
    
    @Query("SELECT * FROM items WHERE category = :category AND isActive = 1")
    suspend fun getItemsByCategory(category: String): List<Item>
    
    @Query("SELECT * FROM items WHERE barcode = :barcode")
    suspend fun getItemByBarcode(barcode: String): Item?
    
    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    suspend fun searchItems(query: String): List<Item>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long
    
    @Update
    suspend fun updateItem(item: Item)
    
    @Delete
    suspend fun deleteItem(item: Item)
    
    // Location operations
    @Query("SELECT * FROM locations WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveLocations(): Flow<List<Location>>
    
    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): Location?
    
    @Query("SELECT * FROM locations WHERE parentLocationId = :parentId")
    suspend fun getChildLocations(parentId: Long): List<Location>
    
    @Query("SELECT * FROM locations WHERE parentLocationId IS NULL")
    suspend fun getRootLocations(): List<Location>
    
    @Query("SELECT * FROM locations WHERE type = :type AND isActive = 1")
    suspend fun getLocationsByType(type: String): List<Location>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long
    
    @Update
    suspend fun updateLocation(location: Location)
    
    @Delete
    suspend fun deleteLocation(location: Location)
    
    // StockMovement operations
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllStockMovements(): Flow<List<StockMovement>>
    
    @Query("SELECT * FROM stock_movements WHERE id = :id")
    suspend fun getStockMovementById(id: Long): StockMovement?
    
    @Query("SELECT * FROM stock_movements WHERE itemId = :itemId ORDER BY timestamp DESC")
    suspend fun getStockMovementsByItemId(itemId: Long): List<StockMovement>
    
    @Query("SELECT * FROM stock_movements WHERE fromLocationId = :locationId OR toLocationId = :locationId ORDER BY timestamp DESC")
    suspend fun getStockMovementsByLocationId(locationId: Long): List<StockMovement>
    
    @Query("SELECT * FROM stock_movements WHERE timestamp BETWEEN :startDate AND :endDate")
    suspend fun getStockMovementsByDateRange(startDate: Long, endDate: Long): List<StockMovement>
    
    @Query("SELECT * FROM stock_movements WHERE type = :type")
    suspend fun getStockMovementsByType(type: String): List<StockMovement>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(stockMovement: StockMovement): Long
    
    @Update
    suspend fun updateStockMovement(stockMovement: StockMovement)
    
    @Delete
    suspend fun deleteStockMovement(stockMovement: StockMovement)
    
    // AffiliateLink operations
    @Query("SELECT * FROM affiliate_links WHERE isActive = 1 ORDER BY isPrimary DESC")
    fun getAllActiveAffiliateLinks(): Flow<List<AffiliateLink>>
    
    @Query("SELECT * FROM affiliate_links WHERE id = :id")
    suspend fun getAffiliateLinkById(id: Long): AffiliateLink?
    
    @Query("SELECT * FROM affiliate_links WHERE itemId = :itemId AND isActive = 1")
    suspend fun getAffiliateLinksByItemId(itemId: Long): List<AffiliateLink>
    
    @Query("SELECT * FROM affiliate_links WHERE merchant = :merchant AND isActive = 1")
    suspend fun getAffiliateLinksByMerchant(merchant: String): List<AffiliateLink>
    
    @Query("SELECT * FROM affiliate_links WHERE itemId = :itemId AND isPrimary = 1")
    suspend fun getPrimaryAffiliateLinkForItem(itemId: Long): AffiliateLink?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAffiliateLink(affiliateLink: AffiliateLink): Long
    
    @Update
    suspend fun updateAffiliateLink(affiliateLink: AffiliateLink)
    
    @Delete
    suspend fun deleteAffiliateLink(affiliateLink: AffiliateLink)
    
    /**
     * Increment click count for an affiliate link
     */
    @Query("UPDATE affiliate_links SET clickCount = clickCount + 1 WHERE id = :linkId")
    suspend fun incrementAffiliateLinkClick(linkId: Long)
    
    /**
     * Record a stock movement with automatic timestamp
     */
    suspend fun recordStockMovement(
        itemId: Long,
        type: String,
        quantity: Int,
        fromLocationId: Long? = null,
        toLocationId: Long? = null,
        cost: Double? = null,
        notes: String? = null,
        reason: String? = null,
        reference: String? = null,
        supplier: String? = null,
        recipient: String? = null,
        batchNumber: String? = null,
        expiryDate: Long? = null,
        condition: String? = null,
        documentUrl: String? = null,
        createdBy: String? = null
    ): Long {
        val stockMovement = StockMovement(
            id = 0, // Room will auto-generate
            itemId = itemId,
            type = type,
            quantity = quantity,
            fromLocationId = fromLocationId,
            toLocationId = toLocationId,
            timestamp = System.currentTimeMillis(),
            reason = reason,
            reference = reference,
            cost = cost,
            totalCost = cost?.let { it * quantity },
            supplier = supplier,
            recipient = recipient,
            batchNumber = batchNumber,
            expiryDate = expiryDate,
            condition = condition,
            notes = notes,
            documentUrl = documentUrl,
            verifiedBy = null,
            verifiedAt = null,
            createdBy = createdBy
        )
        return insertStockMovement(stockMovement)
    }
    
    // Analytics queries
    @Query("""
        SELECT 
            SUM(CASE WHEN sm.type = 'In' THEN sm.quantity ELSE 0 END) -
            SUM(CASE WHEN sm.type = 'Out' THEN sm.quantity ELSE 0 END) -
            SUM(CASE WHEN sm.type = 'Consumed' THEN sm.quantity ELSE 0 END) as currentStock
        FROM stock_movements sm
        WHERE sm.itemId = :itemId
    """)
    suspend fun getCurrentStockForItem(itemId: Long): Int
    
    @Query("""
        SELECT i.*, 
               (SELECT 
                    SUM(CASE WHEN sm.type = 'In' THEN sm.quantity ELSE 0 END) -
                    SUM(CASE WHEN sm.type = 'Out' THEN sm.quantity ELSE 0 END) -
                    SUM(CASE WHEN sm.type = 'Consumed' THEN sm.quantity ELSE 0 END)
                FROM stock_movements sm WHERE sm.itemId = i.id) as currentStock
        FROM items i
        WHERE i.isActive = 1
        AND i.minimumStock IS NOT NULL
        AND (SELECT 
                    SUM(CASE WHEN sm.type = 'In' THEN sm.quantity ELSE 0 END) -
                    SUM(CASE WHEN sm.type = 'Out' THEN sm.quantity ELSE 0 END) -
                    SUM(CASE WHEN sm.type = 'Consumed' THEN sm.quantity ELSE 0 END)
                FROM stock_movements sm WHERE sm.itemId = i.id) < i.minimumStock
    """)
    suspend fun getLowStockItems(): List<ItemWithStock>
    
    @Query("""
        SELECT l.name as locationName, COUNT(sm.id) as movementCount
        FROM locations l
        LEFT JOIN stock_movements sm ON (l.id = sm.fromLocationId OR l.id = sm.toLocationId)
        WHERE l.isActive = 1
        AND sm.timestamp BETWEEN :startDate AND :endDate
        GROUP BY l.id, l.name
        ORDER BY movementCount DESC
    """)
    suspend fun getLocationActivity(startDate: Long, endDate: Long): List<LocationActivity>
    
    @Query("""
        SELECT sm.type, COUNT(*) as count, SUM(sm.totalCost) as totalCost
        FROM stock_movements sm
        WHERE sm.timestamp BETWEEN :startDate AND :endDate
        AND sm.totalCost IS NOT NULL
        GROUP BY sm.type
        ORDER BY totalCost DESC
    """)
    suspend fun getMovementSummary(startDate: Long, endDate: Long): List<MovementSummary>
    
    @Query("""
        SELECT al.merchant, 
               AVG(al.price) as avgPrice,
               COUNT(*) as linkCount,
               SUM(al.clickCount) as totalClicks
        FROM affiliate_links al
        WHERE al.isActive = 1
        AND al.price IS NOT NULL
        GROUP BY al.merchant
        ORDER BY totalClicks DESC
    """)
    suspend fun getMerchantStats(): List<MerchantStats>
    
    @Query("""
        SELECT i.category, COUNT(*) as itemCount
        FROM items i
        WHERE i.isActive = 1
        AND i.category IS NOT NULL
        GROUP BY i.category
        ORDER BY itemCount DESC
    """)
    suspend fun getItemsByCategory(): List<CategoryCount>
    
    data class ItemWithStock(
        val id: Long,
        val name: String,
        val category: String?,
        val brand: String?,
        val minimumStock: Int?,
        val currentStock: Int
    )
    
    data class LocationActivity(
        val locationName: String,
        val movementCount: Int
    )
    
    data class MovementSummary(
        val type: String,
        val count: Int,
        val totalCost: Double?
    )
    
    data class MerchantStats(
        val merchant: String,
        val avgPrice: Double,
        val linkCount: Int,
        val totalClicks: Int
    )
    
    data class CategoryCount(
        val category: String,
        val itemCount: Int
    )
}
