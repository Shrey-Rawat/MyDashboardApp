package com.mydashboardapp.inventory.ui

import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.ui.SimpleBaseViewModel
import com.mydashboardapp.core.ui.UiState
import com.mydashboardapp.data.dao.InventoryDao
import com.mydashboardapp.data.entities.AffiliateLink
import com.mydashboardapp.data.entities.Item
import com.mydashboardapp.data.entities.Location
// import com.mydashboardapp.data.repository.InventoryRepository // Commented out - needs to be implemented
import com.mydashboardapp.inventory.service.BarcodeScannerService
import com.mydashboardapp.inventory.service.DeeplinkService
import com.mydashboardapp.inventory.service.StockAlertService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main inventory view model
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val barcodeScannerService: BarcodeScannerService,
    private val stockAlertService: StockAlertService,
    private val deeplinkService: DeeplinkService
) : SimpleBaseViewModel() {
    
    // UI State flows
    private val _inventoryUiState = MutableStateFlow(InventoryUiState())
    val inventoryUiState: StateFlow<InventoryUiState> = _inventoryUiState.asStateFlow()
    
    private val _inventoryLoading = MutableStateFlow(false)
    
    // Data flows
    val allItems: StateFlow<List<Item>> = inventoryDao.getAllActiveItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val lowStockItems: StateFlow<List<LowStockAlert>> = flow {
        emit(inventoryDao.getLowStockItems())
    }.map { items ->
            items.map { item ->
                LowStockAlert(
                    itemId = item.id,
                    itemName = item.name,
                    category = item.category,
                    brand = item.brand,
                    currentStock = item.currentStock,
                    minimumStock = item.minimumStock ?: 0,
                    severity = when {
                        item.currentStock <= 0 -> AlertSeverity.OUT
                        item.currentStock < (item.minimumStock ?: 0) * 0.5 -> AlertSeverity.CRITICAL
                        else -> AlertSeverity.LOW
                    }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val locations: StateFlow<List<Location>> = inventoryDao.getAllActiveLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Barcode scanner
    val scanResult: StateFlow<BarcodeScannerService.ScanResult?> = barcodeScannerService.scanResult
    val isScanning: StateFlow<Boolean> = barcodeScannerService.isScanning
    val isBarcodeScanningAvailable = barcodeScannerService.isBarcodeScanningAvailable
    
    data class InventoryUiState(
        val selectedFilter: String = "All",
        val viewMode: ViewMode = ViewMode.LIST,
        val searchQuery: String = "",
        val showScannerDialog: Boolean = false,
        val selectedItem: Item? = null,
        val showItemDetails: Boolean = false,
        val alertSettings: StockAlertService.AlertSettings = StockAlertService.AlertSettings(),
        val merchantApps: List<DeeplinkService.MerchantApp> = emptyList()
    )
    
    enum class ViewMode {
        LIST, GRID
    }
    
    init {
        // Initialize services
        if (isBarcodeScanningAvailable) {
            barcodeScannerService.initialize()
        }
        
        // Load available merchant apps
        loadMerchantApps()
        
        // Get alert summary
        loadAlertSummary()
    }
    
    /**
     * Update UI state
     */
    fun updateUiState(update: (InventoryUiState) -> InventoryUiState) {
        _inventoryUiState.value = update(_inventoryUiState.value)
    }
    
    /**
     * Search items
     */
    fun searchItems(query: String) {
        updateUiState { it.copy(searchQuery = query) }
    }
    
    /**
     * Filter items by category
     */
    fun filterItems(category: String) {
        updateUiState { it.copy(selectedFilter = category) }
    }
    
    /**
     * Toggle view mode between list and grid
     */
    fun toggleViewMode() {
        updateUiState { 
            it.copy(viewMode = if (it.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST)
        }
    }
    
    /**
     * Show barcode scanner
     */
    fun showBarcodeScanner() {
        if (isBarcodeScanningAvailable) {
            updateUiState { it.copy(showScannerDialog = true) }
        }
    }
    
    /**
     * Hide barcode scanner
     */
    fun hideBarcodeScanner() {
        barcodeScannerService.stopCamera()
        updateUiState { it.copy(showScannerDialog = false) }
    }
    
    /**
     * Process barcode scan result
     */
    fun processBarcodeResult(barcode: String) {
        viewModelScope.launch {
            try {
                _inventoryLoading.value = true
                
                // Look up item by barcode
                val item = inventoryDao.getItemByBarcode(barcode)
                
                if (item != null) {
                    // Item found - show details
                    updateUiState { 
                        it.copy(
                            selectedItem = item,
                            showItemDetails = true,
                            showScannerDialog = false
                        )
                    }
                } else {
                    // Item not found - could show add item dialog
                    setError("Item with barcode $barcode not found in inventory")
                }
                
            } catch (e: Exception) {
                setError("Error processing barcode: ${e.message ?: "Unknown error"}")
            } finally {
                _inventoryLoading.value = false
                barcodeScannerService.clearScanResult()
            }
        }
    }
    
    /**
     * Show item details
     */
    fun showItemDetails(item: Item) {
        updateUiState { 
            it.copy(
                selectedItem = item,
                showItemDetails = true
            )
        }
    }
    
    /**
     * Hide item details
     */
    fun hideItemDetails() {
        updateUiState { 
            it.copy(
                selectedItem = null,
                showItemDetails = false
            )
        }
    }
    
    /**
     * Launch affiliate deeplink
     */
    fun launchAffiliateLink(itemId: Long, merchant: String) {
        viewModelScope.launch {
            try {
                _inventoryLoading.value = true
                
                val config = DeeplinkService.DeeplinkConfig(
                    itemId = itemId,
                    merchant = merchant,
                    campaignId = "inventory_item_view"
                )
                
                val success = deeplinkService.launchDeeplink(config)
                
                if (!success) {
                    setError("Could not open $merchant app or website")
                }
                
            } catch (e: Exception) {
                setError("Error launching affiliate link: ${e.message ?: "Unknown error"}")
            } finally {
                _inventoryLoading.value = false
            }
        }
    }
    
    /**
     * Update stock for an item
     */
    fun updateStock(itemId: Long, newStock: Int, reason: String = "Manual update") {
        viewModelScope.launch {
            try {
                _inventoryLoading.value = true
                
                val currentStock = inventoryDao.getCurrentStockForItem(itemId)
                val difference = newStock - currentStock
                
                if (difference != 0) {
                    val movementType = if (difference > 0) "In" else "Out"
                    val quantity = kotlin.math.abs(difference.toDouble()).toInt()
                    
                    inventoryDao.recordStockMovement(
                        itemId = itemId,
                        type = movementType,
                        quantity = quantity,
                        reason = reason
                    )
                }
                
                // Stock updated successfully - could emit success state if needed
                
            } catch (e: Exception) {
                setError("Error updating stock: ${e.message ?: "Unknown error"}")
            } finally {
                _inventoryLoading.value = false
            }
        }
    }
    
    /**
     * Check stock alerts
     */
    fun checkStockAlerts() {
        viewModelScope.launch {
            try {
                _inventoryLoading.value = true
                
                val alerts = stockAlertService.performImmediateStockCheck(
                    _inventoryUiState.value.alertSettings
                )
                
                // Alerts checked - could update UI state with results if needed
                if (alerts.isNotEmpty()) {
                    setError("Found ${alerts.size} low stock items")
                }
                
            } catch (e: Exception) {
                setError("Error checking stock alerts: ${e.message ?: "Unknown error"}")
            } finally {
                _inventoryLoading.value = false
            }
        }
    }
    
    /**
     * Load merchant apps
     */
    private fun loadMerchantApps() {
        val apps = deeplinkService.getAvailableMerchantApps()
        updateUiState { it.copy(merchantApps = apps) }
    }
    
    /**
     * Load alert summary
     */
    private fun loadAlertSummary() {
        viewModelScope.launch {
            try {
                val summary = stockAlertService.getAlertSummary()
                // Update UI with summary info if needed
            } catch (e: Exception) {
                // Handle error silently for background operation
            }
        }
    }
    
    /**
     * Get filtered and searched items
     */
    fun getFilteredItems(): StateFlow<List<Item>> = combine(
        allItems,
        _inventoryUiState
    ) { items, uiState ->
        items.filter { item ->
            // Apply search filter
            val matchesSearch = if (uiState.searchQuery.isBlank()) {
                true
            } else {
                item.name.contains(uiState.searchQuery, ignoreCase = true) ||
                item.brand?.contains(uiState.searchQuery, ignoreCase = true) == true ||
                item.category?.contains(uiState.searchQuery, ignoreCase = true) == true
            }
            
            // Apply category filter
            val matchesCategory = if (uiState.selectedFilter == "All") {
                true
            } else {
                item.category?.equals(uiState.selectedFilter, ignoreCase = true) == true
            }
            
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    override fun onCleared() {
        super.onCleared()
        barcodeScannerService.cleanup()
    }
}

// Helper data classes for the view model
data class LowStockAlert(
    val itemId: Long,
    val itemName: String,
    val category: String?,
    val brand: String?,
    val currentStock: Int,
    val minimumStock: Int,
    val severity: AlertSeverity,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AlertSeverity {
    LOW, CRITICAL, OUT
}
