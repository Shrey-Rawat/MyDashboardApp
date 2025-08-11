package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_history")
data class PromptHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userPrompt: String, // the user's input/question
    val aiResponse: String, // the AI's response
    val model: String?, // which AI model was used (e.g., "gpt-4", "claude-3")
    val provider: String?, // AI service provider (e.g., "OpenAI", "Anthropic")
    val context: String?, // what feature/screen this was used in
    val category: String?, // type of request (e.g., "Nutrition", "Training", "Finance")
    val tokens: Int?, // number of tokens used
    val cost: Double?, // cost of this request
    val currency: String = "USD",
    val responseTime: Int?, // response time in milliseconds
    val quality: Int?, // user-rated quality (1-5)
    val isBookmarked: Boolean = false, // user wants to save this
    val isFavorite: Boolean = false, // user marked as favorite
    val tags: String?, // comma-separated tags
    val sessionId: String?, // to group related conversations
    val conversationId: String?, // longer conversation thread
    val parentId: Long?, // reference to previous prompt in conversation
    val temperature: Float?, // AI temperature setting used
    val maxTokens: Int?, // max tokens setting used
    val systemPrompt: String?, // system prompt that was used
    val isSuccessful: Boolean = true, // did the AI request succeed
    val errorMessage: String?, // error message if request failed
    val metadata: String?, // additional metadata (JSON format)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
