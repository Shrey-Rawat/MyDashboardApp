package com.mydashboardapp.data.dao

import androidx.room.*
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AIDao {
    
    // PromptHistory operations
    @Query("SELECT * FROM prompt_history ORDER BY createdAt DESC")
    fun getAllPromptHistory(): Flow<List<PromptHistory>>
    
    @Query("SELECT * FROM prompt_history WHERE id = :id")
    suspend fun getPromptHistoryById(id: Long): PromptHistory?
    
    @Query("SELECT * FROM prompt_history WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    suspend fun getPromptHistoryBySessionId(sessionId: String): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    suspend fun getPromptHistoryByConversationId(conversationId: String): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getPromptHistoryByCategory(category: String): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE context = :context ORDER BY createdAt DESC")
    suspend fun getPromptHistoryByContext(context: String): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE isBookmarked = 1 ORDER BY createdAt DESC")
    suspend fun getBookmarkedPromptHistory(): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE isFavorite = 1 ORDER BY createdAt DESC")
    suspend fun getFavoritePromptHistory(): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE userPrompt LIKE '%' || :query || '%' OR aiResponse LIKE '%' || :query || '%'")
    suspend fun searchPromptHistory(query: String): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE model = :model ORDER BY createdAt DESC")
    suspend fun getPromptHistoryByModel(model: String): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE provider = :provider ORDER BY createdAt DESC")
    suspend fun getPromptHistoryByProvider(provider: String): List<PromptHistory>
    
    @Query("SELECT * FROM prompt_history WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getPromptHistoryByDateRange(startDate: Long, endDate: Long): List<PromptHistory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromptHistory(promptHistory: PromptHistory): Long
    
    @Update
    suspend fun updatePromptHistory(promptHistory: PromptHistory)
    
    @Delete
    suspend fun deletePromptHistory(promptHistory: PromptHistory)
    
    @Query("DELETE FROM prompt_history WHERE createdAt < :cutoffDate AND isBookmarked = 0 AND isFavorite = 0")
    suspend fun deleteOldPromptHistory(cutoffDate: Long)
    
    // Analytics queries
    @Query("""
        SELECT COUNT(*) as totalPrompts,
               SUM(tokens) as totalTokens,
               SUM(cost) as totalCost,
               AVG(responseTime) as avgResponseTime,
               AVG(quality) as avgQuality
        FROM prompt_history
        WHERE createdAt BETWEEN :startDate AND :endDate
        AND isSuccessful = 1
    """)
    suspend fun getPromptStatistics(startDate: Long, endDate: Long): PromptStatistics
    
    @Query("""
        SELECT model, 
               COUNT(*) as promptCount,
               SUM(tokens) as totalTokens,
               SUM(cost) as totalCost,
               AVG(responseTime) as avgResponseTime
        FROM prompt_history
        WHERE createdAt BETWEEN :startDate AND :endDate
        AND model IS NOT NULL
        AND isSuccessful = 1
        GROUP BY model
        ORDER BY promptCount DESC
    """)
    suspend fun getModelUsageStats(startDate: Long, endDate: Long): List<ModelUsageStats>
    
    @Query("""
        SELECT category, 
               COUNT(*) as promptCount,
               AVG(quality) as avgQuality
        FROM prompt_history
        WHERE createdAt BETWEEN :startDate AND :endDate
        AND category IS NOT NULL
        AND quality IS NOT NULL
        GROUP BY category
        ORDER BY promptCount DESC
    """)
    suspend fun getCategoryUsageStats(startDate: Long, endDate: Long): List<CategoryUsageStats>
    
    @Query("""
        SELECT provider,
               COUNT(*) as promptCount,
               SUM(cost) as totalCost,
               AVG(responseTime) as avgResponseTime,
               SUM(CASE WHEN isSuccessful = 1 THEN 1 ELSE 0 END) as successCount
        FROM prompt_history
        WHERE createdAt BETWEEN :startDate AND :endDate
        AND provider IS NOT NULL
        GROUP BY provider
        ORDER BY promptCount DESC
    """)
    suspend fun getProviderStats(startDate: Long, endDate: Long): List<ProviderStats>
    
    @Query("""
        SELECT DATE(createdAt / 1000, 'unixepoch') as date,
               COUNT(*) as promptCount,
               SUM(tokens) as totalTokens,
               SUM(cost) as totalCost
        FROM prompt_history
        WHERE createdAt BETWEEN :startDate AND :endDate
        AND isSuccessful = 1
        GROUP BY date
        ORDER BY date DESC
    """)
    suspend fun getDailyUsageStats(startDate: Long, endDate: Long): List<DailyUsageStats>
    
    @Query("""
        SELECT DISTINCT sessionId
        FROM prompt_history
        WHERE conversationId = :conversationId
        ORDER BY createdAt DESC
    """)
    suspend fun getSessionsInConversation(conversationId: String): List<String>
    
    @Query("""
        SELECT COUNT(DISTINCT conversationId) as totalConversations,
               COUNT(DISTINCT sessionId) as totalSessions
        FROM prompt_history
        WHERE createdAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getConversationStats(startDate: Long, endDate: Long): ConversationStats
    
    data class PromptStatistics(
        val totalPrompts: Int,
        val totalTokens: Int?,
        val totalCost: Double?,
        val avgResponseTime: Double?,
        val avgQuality: Double?
    )
    
    data class ModelUsageStats(
        val model: String,
        val promptCount: Int,
        val totalTokens: Int?,
        val totalCost: Double?,
        val avgResponseTime: Double?
    )
    
    data class CategoryUsageStats(
        val category: String,
        val promptCount: Int,
        val avgQuality: Double?
    )
    
    data class ProviderStats(
        val provider: String,
        val promptCount: Int,
        val totalCost: Double?,
        val avgResponseTime: Double?,
        val successCount: Int
    )
    
    data class DailyUsageStats(
        val date: String,
        val promptCount: Int,
        val totalTokens: Int?,
        val totalCost: Double?
    )
    
    data class ConversationStats(
        val totalConversations: Int,
        val totalSessions: Int
    )
}
