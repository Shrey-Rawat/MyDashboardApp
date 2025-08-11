package com.mydashboardapp.data.repository

import com.mydashboardapp.core.data.BaseRepository
import com.mydashboardapp.data.dao.FinanceDao
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface FinanceRepository {
    // Account operations
    fun getAllAccounts(): Flow<List<Account>>
    suspend fun getAccountById(id: Long): Account?
    suspend fun createAccount(account: Account): Long
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(account: Account)
    suspend fun getTotalBalance(): Double
    
    // Transaction operations
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>>
    suspend fun getTransactionsByCategory(category: String): List<Transaction>
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction>
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    
    // Investment operations
    fun getAllInvestments(): Flow<List<Investment>>
    suspend fun getInvestmentById(id: Long): Investment?
    suspend fun getInvestmentBySymbol(symbol: String): Investment?
    suspend fun insertInvestment(investment: Investment): Long
    suspend fun updateInvestment(investment: Investment)
    suspend fun deleteInvestment(investment: Investment)
    suspend fun updateInvestmentPrice(symbol: String, price: Double, source: String = "Manual")
    
    // Budget operations
    fun getAllBudgetEnvelopes(): Flow<List<BudgetEnvelope>>
    suspend fun getBudgetEnvelopeById(id: Long): BudgetEnvelope?
    suspend fun getBudgetEnvelopesByCategory(category: String): List<BudgetEnvelope>
    suspend fun insertBudgetEnvelope(envelope: BudgetEnvelope): Long
    suspend fun updateBudgetEnvelope(envelope: BudgetEnvelope)
    suspend fun deleteBudgetEnvelope(envelope: BudgetEnvelope)
    
    // Analytics
    suspend fun getFinancialSummary(startDate: Long, endDate: Long): FinanceDao.FinancialSummary
    suspend fun getSpendingByCategory(startDate: Long, endDate: Long): List<FinanceDao.CategorySpending>
    suspend fun getBalanceByAccountType(): List<FinanceDao.AccountTypeBalance>
    suspend fun getInvestmentPerformance(): List<FinanceDao.InvestmentPerformance>
}

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val financeDao: FinanceDao
) : BaseRepository(), FinanceRepository {
    
    // Account operations
    override fun getAllAccounts(): Flow<List<Account>> = financeDao.getAllActiveAccounts()
    
    override suspend fun getAccountById(id: Long): Account? = financeDao.getAccountById(id)
    
    override suspend fun createAccount(account: Account): Long = financeDao.insertAccount(account)
    
    override suspend fun updateAccount(account: Account) = financeDao.updateAccount(account)
    
    override suspend fun deleteAccount(account: Account) = financeDao.deleteAccount(account)
    
    override suspend fun getTotalBalance(): Double = financeDao.getTotalBalance() ?: 0.0
    
    // Transaction operations
    override fun getAllTransactions(): Flow<List<Transaction>> = financeDao.getAllTransactions()
    
    override suspend fun getTransactionById(id: Long): Transaction? = financeDao.getTransactionById(id)
    
    override suspend fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> = 
        kotlinx.coroutines.flow.flowOf(financeDao.getTransactionsByAccountId(accountId))
    
    override suspend fun getTransactionsByCategory(category: String): List<Transaction> = 
        financeDao.getTransactionsByCategory(category)
    
    override suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction> =
        financeDao.getTransactionsByDateRange(startDate, endDate)
    
    override suspend fun insertTransaction(transaction: Transaction): Long = financeDao.insertTransaction(transaction)
    
    override suspend fun updateTransaction(transaction: Transaction) = financeDao.updateTransaction(transaction)
    
    override suspend fun deleteTransaction(transaction: Transaction) = financeDao.deleteTransaction(transaction)
    
    // Investment operations
    override fun getAllInvestments(): Flow<List<Investment>> = financeDao.getAllInvestments()
    
    override suspend fun getInvestmentById(id: Long): Investment? = financeDao.getInvestmentById(id)
    
    override suspend fun getInvestmentBySymbol(symbol: String): Investment? = financeDao.getInvestmentBySymbol(symbol)
    
    override suspend fun insertInvestment(investment: Investment): Long = financeDao.insertInvestment(investment)
    
    override suspend fun updateInvestment(investment: Investment) = financeDao.updateInvestment(investment)
    
    override suspend fun deleteInvestment(investment: Investment) = financeDao.deleteInvestment(investment)
    
    override suspend fun updateInvestmentPrice(symbol: String, price: Double, source: String) {
        val investment = financeDao.getInvestmentBySymbol(symbol)
        investment?.let {
            val updatedInvestment = it.copy(
                currentPrice = price,
                lastPriceUpdate = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            financeDao.updateInvestment(updatedInvestment)
            
            val priceSnapshot = PriceSnapshot(
                investmentId = it.id,
                symbol = symbol,
                price = price,
                timestamp = System.currentTimeMillis(),
                source = source,
                volume = null,
                high = price,
                low = price,
                open = price,
                close = price,
                change = 0.0,
                changePercent = 0.0,
                marketCap = null,
                peRatio = null,
                dividendYield = null,
                notes = null
            )
            financeDao.insertPriceSnapshot(priceSnapshot)
        }
    }
    
    // Budget operations
    override fun getAllBudgetEnvelopes(): Flow<List<BudgetEnvelope>> = financeDao.getAllActiveEnvelopes()
    
    override suspend fun getBudgetEnvelopeById(id: Long): BudgetEnvelope? = financeDao.getEnvelopeById(id)
    
    override suspend fun getBudgetEnvelopesByCategory(category: String): List<BudgetEnvelope> = 
        financeDao.getEnvelopesByCategory(category)
    
    override suspend fun insertBudgetEnvelope(envelope: BudgetEnvelope): Long = financeDao.insertEnvelope(envelope)
    
    override suspend fun updateBudgetEnvelope(envelope: BudgetEnvelope) = financeDao.updateEnvelope(envelope)
    
    override suspend fun deleteBudgetEnvelope(envelope: BudgetEnvelope) = financeDao.deleteEnvelope(envelope)
    
    // Analytics
    override suspend fun getFinancialSummary(startDate: Long, endDate: Long): FinanceDao.FinancialSummary = 
        financeDao.getFinancialSummary(startDate, endDate)
    
    override suspend fun getSpendingByCategory(startDate: Long, endDate: Long): List<FinanceDao.CategorySpending> =
        financeDao.getSpendingByCategory(startDate, endDate)
    
    override suspend fun getBalanceByAccountType(): List<FinanceDao.AccountTypeBalance> = financeDao.getBalanceByAccountType()
    
    override suspend fun getInvestmentPerformance(): List<FinanceDao.InvestmentPerformance> = financeDao.getInvestmentPerformance()
}
