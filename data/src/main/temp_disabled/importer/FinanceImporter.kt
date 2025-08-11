package com.mydashboardapp.data.importer

import com.mydashboardapp.data.entities.Account
import com.mydashboardapp.data.entities.Transaction
import com.mydashboardapp.data.repository.FinanceRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceImporter @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    
    /**
     * Import transactions from CSV file (Free feature)
     * Expected CSV format: Date,Description,Amount,Category,Account
     */
    suspend fun importFromCsv(
        inputStream: InputStream,
        accountId: Long,
        skipFirstRow: Boolean = true,
        delimiter: String = ","
    ): ImportResult {
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            val transactions = mutableListOf<Transaction>()
            val errors = mutableListOf<String>()
            
            var processedLines = 0
            var successCount = 0
            
            lines.forEachIndexed { index, line ->
                if (index == 0 && skipFirstRow) return@forEachIndexed
                
                processedLines++
                
                try {
                    val parts = parseCsvLine(line, delimiter)
                    if (parts.size >= 3) {
                        val transaction = createTransactionFromCsvParts(parts, accountId)
                        financeRepository.createTransaction(
                            accountId = transaction.accountId,
                            amount = transaction.amount,
                            description = transaction.description,
                            category = transaction.category ?: "Other",
                            subcategory = transaction.subcategory,
                            date = transaction.date,
                            notes = "Imported from CSV"
                        )
                        transactions.add(transaction)
                        successCount++
                    } else {
                        errors.add("Line ${index + 1}: Insufficient columns")
                    }
                } catch (e: Exception) {
                    errors.add("Line ${index + 1}: ${e.message}")
                }
            }
            
            ImportResult(
                success = true,
                processedCount = processedLines,
                successCount = successCount,
                errorCount = errors.size,
                errors = errors,
                transactions = transactions
            )
            
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errorCount = 1,
                errors = listOf("Failed to process CSV: ${e.message}")
            )
        }
    }
    
    /**
     * Import transactions from OFX file (Pro feature)
     */
    suspend fun importFromOfx(
        inputStream: InputStream,
        accountId: Long
    ): ImportResult {
        return try {
            val ofxContent = inputStream.bufferedReader().use { it.readText() }
            val transactions = parseOfxTransactions(ofxContent, accountId)
            val errors = mutableListOf<String>()
            var successCount = 0
            
            transactions.forEach { transaction ->
                try {
                    financeRepository.createTransaction(
                        accountId = transaction.accountId,
                        amount = transaction.amount,
                        description = transaction.description,
                        category = transaction.category ?: "Other",
                        subcategory = transaction.subcategory,
                        date = transaction.date,
                        notes = "Imported from OFX",
                        tags = transaction.tags
                    )
                    successCount++
                } catch (e: Exception) {
                    errors.add("Transaction ${transaction.description}: ${e.message}")
                }
            }
            
            ImportResult(
                success = true,
                processedCount = transactions.size,
                successCount = successCount,
                errorCount = errors.size,
                errors = errors,
                transactions = transactions
            )
            
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errorCount = 1,
                errors = listOf("Failed to process OFX: ${e.message}")
            )
        }
    }
    
    /**
     * Import transactions from JSON file (Pro feature)
     * Expected format: {"transactions": [{"date": "...", "description": "...", "amount": "...", ...}]}
     */
    suspend fun importFromJson(
        inputStream: InputStream,
        accountId: Long
    ): ImportResult {
        return try {
            val jsonContent = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonContent)
            val transactionsArray = jsonObject.getJSONArray("transactions")
            
            val transactions = mutableListOf<Transaction>()
            val errors = mutableListOf<String>()
            var successCount = 0
            
            for (i in 0 until transactionsArray.length()) {
                try {
                    val transactionJson = transactionsArray.getJSONObject(i)
                    val transaction = createTransactionFromJson(transactionJson, accountId)
                    
                    financeRepository.createTransaction(
                        accountId = transaction.accountId,
                        amount = transaction.amount,
                        description = transaction.description,
                        category = transaction.category ?: "Other",
                        subcategory = transaction.subcategory,
                        merchant = transaction.merchant,
                        date = transaction.date,
                        notes = "Imported from JSON",
                        tags = transaction.tags
                    )
                    
                    transactions.add(transaction)
                    successCount++
                    
                } catch (e: Exception) {
                    errors.add("Transaction $i: ${e.message}")
                }
            }
            
            ImportResult(
                success = true,
                processedCount = transactionsArray.length(),
                successCount = successCount,
                errorCount = errors.size,
                errors = errors,
                transactions = transactions
            )
            
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errorCount = 1,
                errors = listOf("Failed to process JSON: ${e.message}")
            )
        }
    }
    
    /**
     * Parse CSV line handling quoted fields
     */
    private fun parseCsvLine(line: String, delimiter: String): List<String> {
        val parts = mutableListOf<String>()
        var currentPart = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char.toString() == delimiter && !inQuotes -> {
                    parts.add(currentPart.toString().trim())
                    currentPart = StringBuilder()
                }
                else -> currentPart.append(char)
            }
        }
        
        parts.add(currentPart.toString().trim())
        return parts
    }
    
    /**
     * Create transaction from CSV parts
     * Expected order: Date, Description, Amount, Category, [Subcategory], [Merchant]
     */
    private fun createTransactionFromCsvParts(
        parts: List<String>,
        accountId: Long
    ): Transaction {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val altDateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        
        val date = try {
            dateFormatter.parse(parts[0])?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            try {
                altDateFormatter.parse(parts[0])?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
        
        val amount = parts[2].replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0
        val description = parts[1]
        val category = if (parts.size > 3) parts[3] else null
        val subcategory = if (parts.size > 4) parts[4] else null
        val merchant = if (parts.size > 5) parts[5] else null
        
        return Transaction(
            accountId = accountId,
            amount = amount,
            description = description,
            category = category,
            subcategory = subcategory,
            merchant = merchant,
            date = date,
            type = if (amount > 0) "Credit" else "Debit",
            isManual = false,
            externalId = "CSV_${System.currentTimeMillis()}_${description.hashCode()}"
        )
    }
    
    /**
     * Parse OFX transactions (simplified implementation)
     */
    private fun parseOfxTransactions(ofxContent: String, accountId: Long): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // This is a simplified OFX parser
        // Real implementation would need proper OFX parsing library
        val transactionPattern = Regex("<STMTTRN>(.*?)</STMTTRN>", RegexOption.DOT_MATCHES_ALL)
        val matches = transactionPattern.findAll(ofxContent)
        
        matches.forEach { match ->
            try {
                val transactionData = match.groupValues[1]
                val transaction = parseOfxTransaction(transactionData, accountId)
                transactions.add(transaction)
            } catch (e: Exception) {
                // Skip malformed transactions
            }
        }
        
        return transactions
    }
    
    private fun parseOfxTransaction(transactionData: String, accountId: Long): Transaction {
        val amount = extractOfxValue(transactionData, "TRNAMT")?.toDoubleOrNull() ?: 0.0
        val description = extractOfxValue(transactionData, "NAME") ?: "OFX Transaction"
        val dateStr = extractOfxValue(transactionData, "DTPOSTED") ?: ""
        val fitId = extractOfxValue(transactionData, "FITID")
        
        val date = parseOfxDate(dateStr)
        
        return Transaction(
            accountId = accountId,
            amount = amount,
            description = description,
            date = date,
            type = if (amount > 0) "Credit" else "Debit",
            isManual = false,
            externalId = fitId
        )
    }
    
    private fun extractOfxValue(content: String, tag: String): String? {
        val pattern = Regex("<$tag>(.*?)(?=<|$)", RegexOption.DOT_MATCHES_ALL)
        return pattern.find(content)?.groupValues?.get(1)?.trim()
    }
    
    private fun parseOfxDate(dateStr: String): Long {
        return try {
            // OFX date format: YYYYMMDD[HHMMSS]
            val cleanDate = dateStr.take(8)
            val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            formatter.parse(cleanDate)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * Create transaction from JSON object
     */
    private fun createTransactionFromJson(
        jsonTransaction: JSONObject,
        accountId: Long
    ): Transaction {
        val dateStr = jsonTransaction.optString("date", "")
        val date = parseJsonDate(dateStr)
        
        val amount = jsonTransaction.optDouble("amount", 0.0)
        val description = jsonTransaction.optString("description", "JSON Transaction")
        val category = jsonTransaction.optString("category", null)
        val subcategory = jsonTransaction.optString("subcategory", null)
        val merchant = jsonTransaction.optString("merchant", null)
        val reference = jsonTransaction.optString("reference", null)
        val tags = jsonTransaction.optString("tags", null)
        
        return Transaction(
            accountId = accountId,
            amount = amount,
            description = description,
            category = category,
            subcategory = subcategory,
            merchant = merchant,
            reference = reference,
            tags = tags,
            date = date,
            type = if (amount > 0) "Credit" else "Debit",
            isManual = false,
            externalId = "JSON_${System.currentTimeMillis()}_${description.hashCode()}"
        )
    }
    
    private fun parseJsonDate(dateStr: String): Long {
        val formatters = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        )
        
        formatters.forEach { formatter ->
            try {
                return formatter.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                // Continue to next formatter
            }
        }
        
        return System.currentTimeMillis()
    }
}

data class ImportResult(
    val success: Boolean,
    val processedCount: Int = 0,
    val successCount: Int = 0,
    val errorCount: Int = 0,
    val errors: List<String> = emptyList(),
    val transactions: List<Transaction> = emptyList()
)
