package com.mydashboardapp.data.repository

import com.mydashboardapp.core.data.BaseRepository
import com.mydashboardapp.data.dao.FinanceDao
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val financeDao: FinanceDao
) : BaseRepository() {

    // Accounts
    fun getAllAccounts(): Flow<List<Account>> = financeDao.getAllActiveAccounts()
    
    suspend fun createAccount(account: Account): Long = financeDao.insertAccount(account)
    
    suspend fun updateAccount(account: Account) = financeDao.updateAccount(account)
    
    suspend fun getAccountById(id: Long): Account? = financeDao.getAccountById(id)
    
    suspend fun getTotalBalance(): Double = financeDao.getTotalBalance() ?: 0.0
    
    // Transactions & Expense/Income Logging
    fun getAllTransactions(): Flow<List<Transaction>> = financeDao.getAllTransactions()
    
    suspend fun createTransaction(
        accountId: Long,
        amount: Double,
        description: String,
        category: String,
        subcategory: String? = null,
        merchant: String? = null,
        date: Long = System.currentTimeMillis(),
        notes: String? = null,
        tags: String? = null
    ): Long {
        // TODO: Fix Transaction entity constructor - temporarily disabled
        // val transaction = Transaction(...)
        // return financeDao.insertTransaction(transaction)
        
        throw NotImplementedError("Transaction creation temporarily disabled - needs entity constructor fix")
    }
    
    suspend fun logExpense(
        accountId: Long,
        amount: Double,
        description: String,
        category: String,
        subcategory: String? = null,
        merchant: String? = null,
        date: Long = System.currentTimeMillis(),
        notes: String? = null,
        envelopeId: Long? = null
    ): Long {
        val expenseAmount = if (amount > 0) -amount else amount
        val transactionId = createTransaction(
            accountId = accountId,
            amount = expenseAmount,
            description = description,
            category = category,
            subcategory = subcategory,
            merchant = merchant,
            date = date,
            notes = notes
        )
        
        // If envelope is specified, deduct from envelope
        envelopeId?.let { 
            // TODO: Implement envelope deduction logic
            deductFromEnvelope(it, -expenseAmount, transactionId, date)
        }
        
        return transactionId
    }
    
    suspend fun logIncome(
        accountId: Long,
        amount: Double,
        description: String,
        category: String = "Income",
        source: String? = null,
        date: Long = System.currentTimeMillis(),
        notes: String? = null
    ): Long {
        val incomeAmount = if (amount < 0) -amount else amount
        return createTransaction(
            accountId = accountId,
            amount = incomeAmount,
            description = description,
            category = category,
            merchant = source,
            date = date,
            notes = notes
        )
    }
    
    suspend fun getTransactionsByCategory(category: String): List<Transaction> = 
        financeDao.getTransactionsByCategory(category)
    
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction> =
        financeDao.getTransactionsByDateRange(startDate, endDate)
    
    suspend fun getIncomeTransactions(startDate: Long, endDate: Long): List<Transaction> =
        financeDao.getIncomeTransactions(startDate, endDate)
    
    suspend fun getExpenseTransactions(startDate: Long, endDate: Long): List<Transaction> =
        financeDao.getExpenseTransactions(startDate, endDate)
    
    // Budget Envelopes
    suspend fun createBudgetEnvelope(
        name: String,
        category: String,
        monthlyLimit: Double,
        rolloverEnabled: Boolean = true,
        color: String? = null,
        icon: String? = null
    ): BudgetEnvelope {
        val envelope = BudgetEnvelope(
            name = name,
            category = category,
            monthlyLimit = monthlyLimit,
            currentBalance = monthlyLimit,
            rolloverEnabled = rolloverEnabled,
            color = color,
            icon = icon
        )
        // TODO: Add insert method to dao
        return envelope
    }
    
    private suspend fun deductFromEnvelope(
        envelopeId: Long, 
        amount: Double, 
        transactionId: Long, 
        date: Long
    ) {
        // TODO: Implement envelope deduction logic
        // This would:
        // 1. Get current envelope balance
        // 2. Deduct amount from envelope
        // 3. Create envelope transaction record
        // 4. Update envelope spent amount
    }
    
    suspend fun processMonthlyRollover() {
        // TODO: Implement monthly rollover logic
        // This would:
        // 1. Get current budget period
        // 2. Create next month's budget period
        // 3. For each envelope with rollover enabled:
        //    - Calculate rollover amount (within limits)
        //    - Update envelope balances
        //    - Create rollover transactions
        // 4. Reset spent amounts for new month
    }
    
    // Investment Portfolio
    fun getAllInvestments(): Flow<List<Investment>> = financeDao.getAllInvestments()
    
    suspend fun createInvestment(
        accountId: Long,
        symbol: String,
        name: String,
        type: String,
        quantity: Double,
        purchasePrice: Double,
        sector: String? = null,
        exchange: String? = null
    ): Long {
        val investment = Investment(
            accountId = accountId,
            symbol = symbol,
            name = name,
            type = type,
            quantity = quantity,
            purchasePrice = purchasePrice,
            sector = sector,
            exchange = exchange,
            purchaseDate = System.currentTimeMillis()
        )
        return financeDao.insertInvestment(investment)
    }
    
    suspend fun updateInvestmentPrice(symbol: String, price: Double, source: String = "Manual") {
        val investment = financeDao.getInvestmentBySymbol(symbol)
        investment?.let {
            // Update current price
            val updatedInvestment = it.copy(
                currentPrice = price,
                lastPriceUpdate = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            financeDao.updateInvestment(updatedInvestment)
            
            // Create price snapshot
            val priceSnapshot = PriceSnapshot(
                investmentId = it.id,
                symbol = symbol,
                price = price,
                timestamp = System.currentTimeMillis(),
                source = source
            )
            financeDao.insertPriceSnapshot(priceSnapshot)
        }
    }
    
    suspend fun updateInvestmentPricesFromEndpoint(endpoint: String) {
        // TODO: Implement REST endpoint integration
        // This would:
        // 1. Get all investments that need price updates
        // 2. Make HTTP request to user-supplied endpoint
        // 3. Parse response and update prices
        // 4. Create price snapshots
    }
    
    suspend fun addToWatchlist(symbol: String, name: String) {
        val watchlistInvestment = Investment(
            accountId = 0, // Special account ID for watchlist items
            symbol = symbol,
            name = name,
            type = "Watchlist",
            quantity = 0.0,
            purchasePrice = 0.0,
            isWatchlist = true
        )
        financeDao.insertInvestment(watchlistInvestment)
    }
    
    suspend fun getInvestmentPerformance() = financeDao.getInvestmentPerformance()
    
    suspend fun getWatchlistInvestments() = financeDao.getWatchlistInvestments()
    
    // Analytics
    suspend fun getFinancialSummary(startDate: Long, endDate: Long) = 
        financeDao.getFinancialSummary(startDate, endDate)
    
    suspend fun getSpendingByCategory(startDate: Long, endDate: Long) =
        financeDao.getSpendingByCategory(startDate, endDate)
    
    suspend fun getBalanceByAccountType() = financeDao.getBalanceByAccountType()
    
    // Utility functions for date ranges
    fun getCurrentMonthRange(): Pair<Long, Long> {
        val now = LocalDate.now()
        val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
        val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
        
        return Pair(
            startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
    
    fun getDateRangeForPeriod(months: Int): Pair<Long, Long> {
        val now = LocalDate.now()
        val startDate = now.minusMonths(months.toLong())
        
        return Pair(
            startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            now.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }
    
    // Common transaction categories
    companion object {
        val EXPENSE_CATEGORIES = listOf(
            "Food & Dining",
            "Transportation", 
            "Shopping",
            "Entertainment",
            "Bills & Utilities",
            "Healthcare",
            "Education",
            "Travel",
            "Home & Garden",
            "Personal Care",
            "Gifts & Donations",
            "Fees & Charges",
            "Other"
        )
        
        val INCOME_CATEGORIES = listOf(
            "Salary",
            "Freelance",
            "Business Income",
            "Investment Returns",
            "Interest",
            "Dividends",
            "Rental Income",
            "Gifts",
            "Refunds",
            "Other Income"
        )
    }
}
