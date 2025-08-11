package com.mydashboardapp.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.ui.UiState
import com.mydashboardapp.data.entities.*
import com.mydashboardapp.data.importer.FinanceImporter
import com.mydashboardapp.data.repository.FinanceRepository
import com.mydashboardapp.data.service.InvestmentQuoteService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val financeImporter: FinanceImporter,
    private val investmentQuoteService: InvestmentQuoteService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()
    
    // Core data flows
    val accounts = financeRepository.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val transactions = financeRepository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val investments = financeRepository.getAllInvestments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Derived state
    private val _envelopes = MutableStateFlow<List<BudgetEnvelope>>(emptyList())
    val envelopes: StateFlow<List<BudgetEnvelope>> = _envelopes.asStateFlow()
    
    private val _portfolioValue = MutableStateFlow(PortfolioValue())
    val portfolioValue: StateFlow<PortfolioValue> = _portfolioValue.asStateFlow()
    
    private val _financialSummary = MutableStateFlow(FinancialSummaryData())
    val financialSummary: StateFlow<FinancialSummaryData> = _financialSummary.asStateFlow()
    
    init {
        loadFinancialSummary()
    }
    
    // Account Management
    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Accounts are loaded via Flow, no explicit action needed
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = "Failed to load accounts: ${e.message}")
                }
            }
        }
    }
    
    fun createAccount(
        name: String,
        type: String,
        initialBalance: Double,
        currency: String = "USD"
    ) {
        viewModelScope.launch {
            try {
                val account = Account(
                    name = name,
                    type = type,
                    currentBalance = initialBalance,
                    availableBalance = initialBalance,
                    currency = currency
                )
                financeRepository.createAccount(account)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to create account: ${e.message}")
                }
            }
        }
    }
    
    // Transaction Management
    fun logExpense(
        accountId: Long,
        amount: Double,
        description: String,
        category: String,
        subcategory: String? = null,
        merchant: String? = null,
        date: Long = System.currentTimeMillis(),
        notes: String? = null,
        envelopeId: Long? = null
    ) {
        viewModelScope.launch {
            try {
                financeRepository.logExpense(
                    accountId = accountId,
                    amount = amount,
                    description = description,
                    category = category,
                    subcategory = subcategory,
                    merchant = merchant,
                    date = date,
                    notes = notes,
                    envelopeId = envelopeId
                )
                loadFinancialSummary()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to log expense: ${e.message}")
                }
            }
        }
    }
    
    fun logIncome(
        accountId: Long,
        amount: Double,
        description: String,
        category: String = "Income",
        source: String? = null,
        date: Long = System.currentTimeMillis(),
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                financeRepository.logIncome(
                    accountId = accountId,
                    amount = amount,
                    description = description,
                    category = category,
                    source = source,
                    date = date,
                    notes = notes
                )
                loadFinancialSummary()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to log income: ${e.message}")
                }
            }
        }
    }
    
    // Budget Envelope Management
    fun loadEnvelopes() {
        viewModelScope.launch {
            try {
                // Note: This would need to be implemented in the repository
                // For now, we'll create sample data
                val sampleEnvelopes = listOf(
                    BudgetEnvelope(
                        id = 1,
                        name = "Groceries",
                        category = "Food & Dining",
                        monthlyLimit = 500.0,
                        currentBalance = 350.0,
                        spent = 150.0
                    ),
                    BudgetEnvelope(
                        id = 2,
                        name = "Gas",
                        category = "Transportation",
                        monthlyLimit = 200.0,
                        currentBalance = 120.0,
                        spent = 80.0
                    ),
                    BudgetEnvelope(
                        id = 3,
                        name = "Entertainment",
                        category = "Entertainment",
                        monthlyLimit = 150.0,
                        currentBalance = 25.0,
                        spent = 125.0
                    )
                )
                _envelopes.value = sampleEnvelopes
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to load envelopes: ${e.message}")
                }
            }
        }
    }
    
    fun createBudgetEnvelope(
        name: String,
        category: String,
        monthlyLimit: Double,
        rolloverEnabled: Boolean = true,
        color: String? = null,
        icon: String? = null
    ) {
        viewModelScope.launch {
            try {
                financeRepository.createBudgetEnvelope(
                    name = name,
                    category = category,
                    monthlyLimit = monthlyLimit,
                    rolloverEnabled = rolloverEnabled,
                    color = color,
                    icon = icon
                )
                loadEnvelopes()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to create envelope: ${e.message}")
                }
            }
        }
    }
    
    // Investment Management
    fun loadInvestments() {
        viewModelScope.launch {
            try {
                // Investments are loaded via Flow, no explicit action needed
                loadPortfolioValue()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to load investments: ${e.message}")
                }
            }
        }
    }
    
    fun loadPortfolioValue() {
        viewModelScope.launch {
            try {
                val currentInvestments = investments.value
                var totalCost = 0.0
                var currentValue = 0.0
                
                currentInvestments.forEach { investment ->
                    if (!investment.isWatchlist) {
                        val costBasis = investment.quantity * investment.purchasePrice
                        val marketValue = investment.quantity * (investment.currentPrice ?: investment.purchasePrice)
                        
                        totalCost += costBasis
                        currentValue += marketValue
                    }
                }
                
                _portfolioValue.value = PortfolioValue(
                    totalCost = totalCost,
                    currentValue = currentValue,
                    totalGainLoss = currentValue - totalCost
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to calculate portfolio value: ${e.message}")
                }
            }
        }
    }
    
    fun addInvestment(
        accountId: Long,
        symbol: String,
        name: String,
        type: String,
        quantity: Double,
        purchasePrice: Double,
        sector: String? = null,
        exchange: String? = null
    ) {
        viewModelScope.launch {
            try {
                financeRepository.createInvestment(
                    accountId = accountId,
                    symbol = symbol,
                    name = name,
                    type = type,
                    quantity = quantity,
                    purchasePrice = purchasePrice,
                    sector = sector,
                    exchange = exchange
                )
                loadPortfolioValue()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to add investment: ${e.message}")
                }
            }
        }
    }
    
    fun refreshPrices(endpointUrl: String? = null, apiKey: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (endpointUrl != null) {
                    val symbols = investments.value.filter { !it.isWatchlist }.map { it.symbol }
                    val result = investmentQuoteService.updatePricesFromEndpoint(
                        endpointUrl = endpointUrl,
                        symbols = symbols,
                        apiKey = apiKey
                    )
                    
                    if (result.success) {
                        loadPortfolioValue()
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "Updated ${result.updatedCount} prices"
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to update prices: ${result.errors.joinToString()}"
                            )
                        }
                    }
                } else {
                    // Manual price update or default behavior
                    loadPortfolioValue()
                    _uiState.update { 
                        it.copy(isLoading = false, successMessage = "Portfolio refreshed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to refresh prices: ${e.message}"
                    )
                }
            }
        }
    }
    
    // Import Functions
    fun importFromCsv(
        inputStream: InputStream,
        accountId: Long,
        skipFirstRow: Boolean = true,
        delimiter: String = ","
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = financeImporter.importFromCsv(
                    inputStream = inputStream,
                    accountId = accountId,
                    skipFirstRow = skipFirstRow,
                    delimiter = delimiter
                )
                
                if (result.success) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Imported ${result.successCount} transactions"
                        )
                    }
                    loadFinancialSummary()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Import failed: ${result.errors.joinToString()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Import failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun importFromOfx(inputStream: InputStream, accountId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = financeImporter.importFromOfx(inputStream, accountId)
                
                if (result.success) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Imported ${result.successCount} transactions from OFX"
                        )
                    }
                    loadFinancialSummary()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "OFX import failed: ${result.errors.joinToString()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "OFX import failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun importFromJson(inputStream: InputStream, accountId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = financeImporter.importFromJson(inputStream, accountId)
                
                if (result.success) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Imported ${result.successCount} transactions from JSON"
                        )
                    }
                    loadFinancialSummary()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "JSON import failed: ${result.errors.joinToString()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "JSON import failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    // Analytics
    private fun loadFinancialSummary() {
        viewModelScope.launch {
            try {
                val (startDate, endDate) = financeRepository.getCurrentMonthRange()
                val summary = financeRepository.getFinancialSummary(startDate, endDate)
                val spendingByCategory = financeRepository.getSpendingByCategory(startDate, endDate)
                val totalBalance = financeRepository.getTotalBalance()
                
                _financialSummary.value = FinancialSummaryData(
                    totalBalance = totalBalance,
                    monthlyIncome = summary.totalIncome,
                    monthlyExpenses = summary.totalExpenses,
                    netIncome = summary.totalIncome - summary.totalExpenses,
                    transactionCount = summary.totalTransactions,
                    topSpendingCategories = spendingByCategory.take(5)
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to load financial summary: ${e.message}")
                }
            }
        }
    }
    
    fun getSpendingByCategory(months: Int = 1) {
        viewModelScope.launch {
            try {
                val (startDate, endDate) = financeRepository.getDateRangeForPeriod(months)
                val spendingData = financeRepository.getSpendingByCategory(startDate, endDate)
                // Update UI state with spending data if needed
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to load spending data: ${e.message}")
                }
            }
        }
    }
    
    // UI State Management
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

data class FinanceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState

data class PortfolioValue(
    val totalCost: Double = 0.0,
    val currentValue: Double = 0.0,
    val totalGainLoss: Double = 0.0
) {
    val gainLossPercent: Double
        get() = if (totalCost > 0) (totalGainLoss / totalCost) * 100 else 0.0
}

data class FinancialSummaryData(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val netIncome: Double = 0.0,
    val transactionCount: Int = 0,
    val topSpendingCategories: List<com.mydashboardapp.data.dao.FinanceDao.CategorySpending> = emptyList()
)
