package com.mydashboardapp.data.dao

import androidx.room.*
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    
    // Account operations
    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveAccounts(): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?
    
    @Query("SELECT * FROM accounts WHERE type = :type AND isActive = 1")
    suspend fun getAccountsByType(type: String): List<Account>
    
    @Query("SELECT SUM(currentBalance) FROM accounts WHERE isActive = 1 AND isHidden = 0")
    suspend fun getTotalBalance(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long
    
    @Update
    suspend fun updateAccount(account: Account)
    
    @Delete
    suspend fun deleteAccount(account: Account)
    
    // Transaction operations
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<com.mydashboardapp.data.entities.Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): com.mydashboardapp.data.entities.Transaction?
    
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    suspend fun getTransactionsByAccountId(accountId: Long): List<com.mydashboardapp.data.entities.Transaction>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<com.mydashboardapp.data.entities.Transaction>
    
    @Query("SELECT * FROM transactions WHERE category = :category")
    suspend fun getTransactionsByCategory(category: String): List<com.mydashboardapp.data.entities.Transaction>
    
    @Query("SELECT * FROM transactions WHERE amount > 0 AND date BETWEEN :startDate AND :endDate")
    suspend fun getIncomeTransactions(startDate: Long, endDate: Long): List<com.mydashboardapp.data.entities.Transaction>
    
    @Query("SELECT * FROM transactions WHERE amount < 0 AND date BETWEEN :startDate AND :endDate")
    suspend fun getExpenseTransactions(startDate: Long, endDate: Long): List<com.mydashboardapp.data.entities.Transaction>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: com.mydashboardapp.data.entities.Transaction): Long
    
    @Update
    suspend fun updateTransaction(transaction: com.mydashboardapp.data.entities.Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: com.mydashboardapp.data.entities.Transaction)
    
    // Investment operations
    @Query("SELECT * FROM investments ORDER BY name ASC")
    fun getAllInvestments(): Flow<List<Investment>>
    
    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getInvestmentById(id: Long): Investment?
    
    @Query("SELECT * FROM investments WHERE accountId = :accountId")
    suspend fun getInvestmentsByAccountId(accountId: Long): List<Investment>
    
    @Query("SELECT * FROM investments WHERE symbol = :symbol")
    suspend fun getInvestmentBySymbol(symbol: String): Investment?
    
    @Query("SELECT * FROM investments WHERE isWatchlist = 1")
    suspend fun getWatchlistInvestments(): List<Investment>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: Investment): Long
    
    @Update
    suspend fun updateInvestment(investment: Investment)
    
    @Delete
    suspend fun deleteInvestment(investment: Investment)
    
    // PriceSnapshot operations
    @Query("SELECT * FROM price_snapshots WHERE investmentId = :investmentId ORDER BY timestamp DESC")
    suspend fun getPriceSnapshotsByInvestmentId(investmentId: Long): List<PriceSnapshot>
    
    @Query("SELECT * FROM price_snapshots WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestPriceSnapshots(symbol: String, limit: Int): List<PriceSnapshot>
    
    @Query("SELECT * FROM price_snapshots WHERE symbol = :symbol AND timestamp BETWEEN :startDate AND :endDate")
    suspend fun getPriceSnapshotsByDateRange(symbol: String, startDate: Long, endDate: Long): List<PriceSnapshot>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceSnapshot(priceSnapshot: PriceSnapshot): Long
    
    @Delete
    suspend fun deletePriceSnapshot(priceSnapshot: PriceSnapshot)
    
    @Query("DELETE FROM price_snapshots WHERE timestamp < :cutoffDate")
    suspend fun deleteOldPriceSnapshots(cutoffDate: Long)
    
    // Budget Envelope operations
    @Query("SELECT * FROM budget_envelopes WHERE isActive = 1 ORDER BY priority DESC, name ASC")
    fun getAllActiveEnvelopes(): Flow<List<BudgetEnvelope>>
    
    @Query("SELECT * FROM budget_envelopes WHERE id = :id")
    suspend fun getEnvelopeById(id: Long): BudgetEnvelope?
    
    @Query("SELECT * FROM budget_envelopes WHERE category = :category AND isActive = 1")
    suspend fun getEnvelopesByCategory(category: String): List<BudgetEnvelope>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelope(envelope: BudgetEnvelope): Long
    
    @Update
    suspend fun updateEnvelope(envelope: BudgetEnvelope)
    
    @Delete
    suspend fun deleteEnvelope(envelope: BudgetEnvelope)
    
    @Query("SELECT * FROM budget_periods WHERE isActive = 1 LIMIT 1")
    suspend fun getCurrentBudgetPeriod(): BudgetPeriod?
    
    @Query("SELECT * FROM budget_periods WHERE year = :year AND month = :month")
    suspend fun getBudgetPeriod(year: Int, month: Int): BudgetPeriod?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetPeriod(budgetPeriod: BudgetPeriod): Long
    
    @Update
    suspend fun updateBudgetPeriod(budgetPeriod: BudgetPeriod)
    
    @Query("SELECT * FROM envelope_allocations WHERE envelopeId = :envelopeId AND budgetPeriodId = :budgetPeriodId")
    suspend fun getEnvelopeAllocation(envelopeId: Long, budgetPeriodId: Long): EnvelopeAllocation?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelopeAllocation(allocation: EnvelopeAllocation): Long
    
    @Update
    suspend fun updateEnvelopeAllocation(allocation: EnvelopeAllocation)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelopeTransaction(envelopeTransaction: EnvelopeTransaction): Long
    
    @Query("SELECT SUM(monthlyLimit) FROM budget_envelopes WHERE isActive = 1")
    suspend fun getTotalBudgetAllocated(): Double?
    
    @Query("SELECT SUM(spent) FROM budget_envelopes WHERE isActive = 1")
    suspend fun getTotalBudgetSpent(): Double?
    
    // Analytics queries
    @Query("""
        SELECT SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) as totalIncome,
               SUM(CASE WHEN amount < 0 THEN ABS(amount) ELSE 0 END) as totalExpenses,
               COUNT(*) as totalTransactions
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getFinancialSummary(startDate: Long, endDate: Long): FinancialSummary
    
    @Query("""
        SELECT category, 
               SUM(CASE WHEN amount < 0 THEN ABS(amount) ELSE 0 END) as totalSpent,
               COUNT(*) as transactionCount
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        AND amount < 0
        AND category IS NOT NULL
        GROUP BY category
        ORDER BY totalSpent DESC
    """)
    suspend fun getSpendingByCategory(startDate: Long, endDate: Long): List<CategorySpending>
    
    @Query("""
        SELECT i.*,
               (i.quantity * i.currentPrice) as currentValue,
               (i.quantity * i.currentPrice) - (i.quantity * i.purchasePrice) as gainLoss
        FROM investments i
        WHERE i.isWatchlist = 0
        AND i.currentPrice IS NOT NULL
    """)
    suspend fun getInvestmentPerformance(): List<InvestmentPerformance>
    
    @Query("""
        SELECT type, SUM(currentBalance) as totalBalance
        FROM accounts 
        WHERE isActive = 1 AND isHidden = 0
        GROUP BY type
        ORDER BY totalBalance DESC
    """)
    suspend fun getBalanceByAccountType(): List<AccountTypeBalance>
    
    data class FinancialSummary(
        val totalIncome: Double,
        val totalExpenses: Double,
        val totalTransactions: Int
    )
    
    data class CategorySpending(
        val category: String,
        val totalSpent: Double,
        val transactionCount: Int
    )
    
    data class InvestmentPerformance(
        val id: Long,
        val symbol: String,
        val name: String,
        val quantity: Double,
        val purchasePrice: Double,
        val currentPrice: Double?,
        val currentValue: Double,
        val gainLoss: Double
    )
    
    data class AccountTypeBalance(
        val type: String,
        val totalBalance: Double
    )
}
