package com.mydashboardapp.data.repository

import com.mydashboardapp.data.dao.InventoryDao
import com.mydashboardapp.data.entities.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("InventoryRepository Tests")
class InventoryRepositoryTest {

    private lateinit var inventoryDao: InventoryDao
    private lateinit var repository: InventoryRepository

    @BeforeEach
    fun setup() {
        inventoryDao = mockk()
        repository = InventoryRepository(inventoryDao)
    }

    @Test
    @DisplayName("Should return all active items from DAO")
    fun `getAllActiveItems returns flow from dao`() = runTest {
        // Given
        val expectedItems = listOf(
            mockItem(id = 1, name = "Item 1"),
            mockItem(id = 2, name = "Item 2")
        )
        every { inventoryDao.getAllActiveItems() } returns flowOf(expectedItems)

        // When
        val result = repository.getAllActiveItems().first()

        // Then
        assertEquals(expectedItems, result)
        verify { inventoryDao.getAllActiveItems() }
    }

    @Test
    @DisplayName("Should get item by ID from DAO")
    fun `getItemById returns item from dao`() = runTest {
        // Given
        val itemId = 1L
        val expectedItem = mockItem(id = itemId, name = "Test Item")
        coEvery { inventoryDao.getItemById(itemId) } returns expectedItem

        // When
        val result = repository.getItemById(itemId)

        // Then
        assertEquals(expectedItem, result)
        coVerify { inventoryDao.getItemById(itemId) }
    }

    @Test
    @DisplayName("Should return null when item not found")
    fun `getItemById returns null when item not found`() = runTest {
        // Given
        val itemId = 999L
        coEvery { inventoryDao.getItemById(itemId) } returns null

        // When
        val result = repository.getItemById(itemId)

        // Then
        assertEquals(null, result)
        coVerify { inventoryDao.getItemById(itemId) }
    }

    @Test
    @DisplayName("Should get item by barcode from DAO")
    fun `getItemByBarcode returns item from dao`() = runTest {
        // Given
        val barcode = "123456789"
        val expectedItem = mockItem(barcode = barcode)
        coEvery { inventoryDao.getItemByBarcode(barcode) } returns expectedItem

        // When
        val result = repository.getItemByBarcode(barcode)

        // Then
        assertEquals(expectedItem, result)
        coVerify { inventoryDao.getItemByBarcode(barcode) }
    }

    @Test
    @DisplayName("Should search items using DAO")
    fun `searchItems returns results from dao`() = runTest {
        // Given
        val query = "test"
        val expectedItems = listOf(mockItem(name = "Test Item"))
        coEvery { inventoryDao.searchItems(query) } returns expectedItems

        // When
        val result = repository.searchItems(query)

        // Then
        assertEquals(expectedItems, result)
        coVerify { inventoryDao.searchItems(query) }
    }

    @Test
    @DisplayName("Should insert item and return ID")
    fun `insertItem returns generated ID`() = runTest {
        // Given
        val item = mockItem()
        val expectedId = 123L
        coEvery { inventoryDao.insertItem(item) } returns expectedId

        // When
        val result = repository.insertItem(item)

        // Then
        assertEquals(expectedId, result)
        coVerify { inventoryDao.insertItem(item) }
    }

    @Test
    @DisplayName("Should update item via DAO")
    fun `updateItem calls dao`() = runTest {
        // Given
        val item = mockItem()
        coEvery { inventoryDao.updateItem(item) } just runs

        // When
        repository.updateItem(item)

        // Then
        coVerify { inventoryDao.updateItem(item) }
    }

    @Test
    @DisplayName("Should delete item via DAO")
    fun `deleteItem calls dao`() = runTest {
        // Given
        val item = mockItem()
        coEvery { inventoryDao.deleteItem(item) } just runs

        // When
        repository.deleteItem(item)

        // Then
        coVerify { inventoryDao.deleteItem(item) }
    }

    @Test
    @DisplayName("Should get all active locations")
    fun `getAllActiveLocations returns flow from dao`() = runTest {
        // Given
        val expectedLocations = listOf(mockLocation())
        every { inventoryDao.getAllActiveLocations() } returns flowOf(expectedLocations)

        // When
        val result = repository.getAllActiveLocations().first()

        // Then
        assertEquals(expectedLocations, result)
        verify { inventoryDao.getAllActiveLocations() }
    }

    @Test
    @DisplayName("Should get location by ID")
    fun `getLocationById returns location from dao`() = runTest {
        // Given
        val locationId = 1L
        val expectedLocation = mockLocation(id = locationId)
        coEvery { inventoryDao.getLocationById(locationId) } returns expectedLocation

        // When
        val result = repository.getLocationById(locationId)

        // Then
        assertEquals(expectedLocation, result)
        coVerify { inventoryDao.getLocationById(locationId) }
    }

    @Test
    @DisplayName("Should insert location and return ID")
    fun `insertLocation returns generated ID`() = runTest {
        // Given
        val location = mockLocation()
        val expectedId = 456L
        coEvery { inventoryDao.insertLocation(location) } returns expectedId

        // When
        val result = repository.insertLocation(location)

        // Then
        assertEquals(expectedId, result)
        coVerify { inventoryDao.insertLocation(location) }
    }

    @Test
    @DisplayName("Should get current stock for item")
    fun `getCurrentStockForItem returns stock from dao`() = runTest {
        // Given
        val itemId = 1L
        val expectedStock = 50
        coEvery { inventoryDao.getCurrentStockForItem(itemId) } returns expectedStock

        // When
        val result = repository.getCurrentStockForItem(itemId)

        // Then
        assertEquals(expectedStock, result)
        coVerify { inventoryDao.getCurrentStockForItem(itemId) }
    }

    @Test
    @DisplayName("Should record stock movement from barcode scan")
    fun `recordStockMovementFromScan creates stock movement with correct data`() = runTest {
        // Given
        val itemId = 1L
        val type = "IN"
        val quantity = 10
        val locationId = 2L
        val reason = "Barcode scan"
        val expectedId = 789L
        
        val slot = slot<StockMovement>()
        coEvery { inventoryDao.insertStockMovement(capture(slot)) } returns expectedId

        // When
        val result = repository.recordStockMovementFromScan(
            itemId = itemId,
            type = type,
            quantity = quantity,
            locationId = locationId,
            reason = reason
        )

        // Then
        assertEquals(expectedId, result)
        
        val capturedMovement = slot.captured
        assertEquals(itemId, capturedMovement.itemId)
        assertEquals(type, capturedMovement.type)
        assertEquals(quantity, capturedMovement.quantity)
        assertEquals(locationId, capturedMovement.toLocationId)
        assertEquals(reason, capturedMovement.reason)
        assertEquals(false, capturedMovement.isVerified)
        assertNotNull(capturedMovement.timestamp)
    }

    @Test
    @DisplayName("Should increment affiliate link click count")
    fun `incrementAffiliateLinkClick updates click count and timestamps`() = runTest {
        // Given
        val linkId = 1L
        val existingLink = mockAffiliateLink(
            id = linkId,
            clickCount = 5,
            lastClickAt = 1000L,
            updatedAt = 1000L
        )
        
        val slot = slot<AffiliateLink>()
        coEvery { inventoryDao.getAffiliateLinkById(linkId) } returns existingLink
        coEvery { inventoryDao.updateAffiliateLink(capture(slot)) } just runs

        // When
        repository.incrementAffiliateLinkClick(linkId)

        // Then
        val updatedLink = slot.captured
        assertEquals(6, updatedLink.clickCount)
        assert(updatedLink.lastClickAt > existingLink.lastClickAt)
        assert(updatedLink.updatedAt > existingLink.updatedAt)
        
        coVerify { inventoryDao.getAffiliateLinkById(linkId) }
        coVerify { inventoryDao.updateAffiliateLink(any()) }
    }

    @Test
    @DisplayName("Should handle null affiliate link gracefully")
    fun `incrementAffiliateLinkClick handles null link gracefully`() = runTest {
        // Given
        val linkId = 999L
        coEvery { inventoryDao.getAffiliateLinkById(linkId) } returns null

        // When
        repository.incrementAffiliateLinkClick(linkId)

        // Then
        coVerify { inventoryDao.getAffiliateLinkById(linkId) }
        coVerify(exactly = 0) { inventoryDao.updateAffiliateLink(any()) }
    }

    @Test
    @DisplayName("Should generate affiliate deeplink with tracking")
    fun `generateAffiliateDeeplink builds correct URL with tracking`() = runTest {
        // Given
        val itemId = 1L
        val merchant = "Amazon"
        val campaignId = "test_campaign"
        val affiliateLink = mockAffiliateLink(
            id = 1L,
            affiliateUrl = "https://amazon.com/product",
            trackingId = "track123",
            campaignId = "default_campaign",
            merchant = "Amazon"
        )
        
        val slot = slot<AffiliateLink>()
        coEvery { inventoryDao.getAffiliateLinksByItemId(itemId) } returns listOf(affiliateLink)
        coEvery { inventoryDao.getAffiliateLinkById(1L) } returns affiliateLink
        coEvery { inventoryDao.updateAffiliateLink(capture(slot)) } just runs

        // When
        val result = repository.generateAffiliateDeeplink(itemId, merchant, campaignId)

        // Then
        assertNotNull(result)
        assert(result!!.contains("utm_source=MyDashboardApp"))
        assert(result.contains("utm_medium=affiliate"))
        assert(result.contains("utm_campaign=$campaignId"))
        assert(result.contains("tracking_id=track123"))
        
        // Verify click was tracked
        val updatedLink = slot.captured
        assertEquals(1, updatedLink.clickCount)
        
        coVerify { inventoryDao.updateAffiliateLink(any()) }
    }

    @Test
    @DisplayName("Should return null when no affiliate link found")
    fun `generateAffiliateDeeplink returns null when no link found`() = runTest {
        // Given
        val itemId = 999L
        val merchant = "NonExistent"
        coEvery { inventoryDao.getAffiliateLinksByItemId(itemId) } returns emptyList()
        coEvery { inventoryDao.getPrimaryAffiliateLinkForItem(itemId) } returns null

        // When
        val result = repository.generateAffiliateDeeplink(itemId, merchant)

        // Then
        assertEquals(null, result)
        coVerify { inventoryDao.getAffiliateLinksByItemId(itemId) }
        coVerify { inventoryDao.getPrimaryAffiliateLinkForItem(itemId) }
    }

    @Test
    @DisplayName("Should get low stock items flow")
    fun `getLowStockItemsFlow returns items below minimum stock`() = runTest {
        // Given
        val item1 = mockItem(id = 1, name = "Item 1", minimumStock = 10)
        val item2 = mockItem(id = 2, name = "Item 2", minimumStock = 5)
        val item3 = mockItem(id = 3, name = "Item 3", minimumStock = null) // No minimum
        
        every { inventoryDao.getAllActiveItems() } returns flowOf(listOf(item1, item2, item3))
        coEvery { inventoryDao.getCurrentStockForItem(1) } returns 5 // Below minimum
        coEvery { inventoryDao.getCurrentStockForItem(2) } returns 10 // Above minimum
        coEvery { inventoryDao.getCurrentStockForItem(3) } returns 3 // No minimum set

        // When
        val result = repository.getLowStockItemsFlow().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Item 1", result[0].name)
        assertEquals(5, result[0].currentStock)
        assertEquals(10, result[0].minimumStock)
    }

    @Test
    @DisplayName("Should get analytics data")
    fun `analytics methods call dao correctly`() = runTest {
        // Given
        val lowStockItems = listOf(mockItemWithStock())
        val merchantStats = listOf(mockMerchantStats())
        val categoryCount = listOf(mockCategoryCount())
        
        coEvery { inventoryDao.getLowStockItems() } returns lowStockItems
        coEvery { inventoryDao.getMerchantStats() } returns merchantStats
        coEvery { inventoryDao.getItemsByCategory() } returns categoryCount

        // When & Then
        assertEquals(lowStockItems, repository.getLowStockItems())
        assertEquals(merchantStats, repository.getMerchantStats())
        assertEquals(categoryCount, repository.getItemsByCategory())
        
        coVerify { inventoryDao.getLowStockItems() }
        coVerify { inventoryDao.getMerchantStats() }
        coVerify { inventoryDao.getItemsByCategory() }
    }

    // Helper methods for creating mock objects
    private fun mockItem(
        id: Long = 1L,
        name: String = "Test Item",
        barcode: String? = null,
        minimumStock: Int? = null
    ): Item = Item(
        id = id,
        name = name,
        barcode = barcode,
        category = "Test Category",
        brand = "Test Brand",
        description = "Test Description",
        minimumStock = minimumStock,
        purchasePrice = 10.0,
        sellPrice = 15.0,
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun mockLocation(id: Long = 1L): Location = Location(
        id = id,
        name = "Test Location",
        type = "Warehouse",
        description = "Test Description",
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun mockAffiliateLink(
        id: Long = 1L,
        clickCount: Int = 0,
        lastClickAt: Long? = null,
        updatedAt: Long = System.currentTimeMillis(),
        affiliateUrl: String = "https://example.com",
        trackingId: String? = null,
        campaignId: String? = null,
        merchant: String = "Test Merchant"
    ): AffiliateLink = AffiliateLink(
        id = id,
        itemId = 1L,
        affiliateUrl = affiliateUrl,
        merchant = merchant,
        trackingId = trackingId,
        campaignId = campaignId,
        clickCount = clickCount,
        lastClickAt = lastClickAt,
        commissionRate = 5.0,
        isPrimary = false,
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = updatedAt
    )

    private fun mockItemWithStock(): InventoryDao.ItemWithStock = InventoryDao.ItemWithStock(
        id = 1L,
        name = "Test Item",
        category = "Test Category",
        brand = "Test Brand",
        minimumStock = 10,
        currentStock = 5
    )

    private fun mockMerchantStats(): InventoryDao.MerchantStats = InventoryDao.MerchantStats(
        merchant = "Test Merchant",
        totalItems = 10,
        totalClicks = 50,
        averageCommission = 5.5
    )

    private fun mockCategoryCount(): InventoryDao.CategoryCount = InventoryDao.CategoryCount(
        category = "Test Category",
        itemCount = 25
    )
}
