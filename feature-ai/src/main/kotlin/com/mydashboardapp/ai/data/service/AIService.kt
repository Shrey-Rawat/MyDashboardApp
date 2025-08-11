package com.mydashboardapp.ai.data.service

import com.mydashboardapp.ai.data.api.*
import com.mydashboardapp.ai.data.models.AIProvider
import com.mydashboardapp.ai.data.models.ChatMessage
import com.mydashboardapp.ai.data.models.ProviderConfig
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

/**
 * Interface for AI service operations
 */
interface AIService {
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): ChatMessage
    
    fun sendMessageStream(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): Flow<StreamEvent>
    
    suspend fun validateConnection(config: ProviderConfig): Boolean
}

/**
 * OpenAI API interface
 */
interface OpenAIApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

/**
 * Anthropic API interface
 */
interface AnthropicApi {
    @POST("messages")
    suspend fun messages(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse
}

/**
 * Google AI API interface
 */
interface GoogleAIApi {
    @POST("models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GoogleRequest
    ): GoogleResponse
    
    @POST("models/{model}:streamGenerateContent")
    suspend fun streamGenerateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GoogleRequest
    ): GoogleResponse
}

/**
 * Generic API interface for custom providers
 */
interface GenericAIApi {
    @POST
    suspend fun sendRequest(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body request: Any
    ): Any
}
