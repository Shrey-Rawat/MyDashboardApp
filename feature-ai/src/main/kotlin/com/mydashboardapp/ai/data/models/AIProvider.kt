package com.mydashboardapp.ai.data.models

import kotlinx.serialization.Serializable

/**
 * Enum representing different AI providers
 */
enum class AIProvider(
    val displayName: String,
    val baseUrl: String,
    val requiresApiKey: Boolean
) {
    OPENAI("OpenAI", "https://api.openai.com/v1", true),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1", true),
    GOOGLE("Google AI", "https://generativelanguage.googleapis.com/v1", true),
    OLLAMA("Ollama (Local)", "http://localhost:11434", false),
    CUSTOM("Custom", "", true)
}

/**
 * AI model configuration
 */
@Serializable
data class AIModel(
    val id: String,
    val name: String,
    val provider: AIProvider,
    val contextWindow: Int = 4096,
    val maxTokens: Int = 1024,
    val supportStreaming: Boolean = true,
    val costPer1kTokens: Double = 0.0
)

/**
 * Provider configuration for encrypted storage
 */
@Serializable
data class ProviderConfig(
    val provider: AIProvider,
    val apiKey: String,
    val baseUrl: String = provider.baseUrl,
    val selectedModel: String = "",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1024,
    val isEnabled: Boolean = true,
    val customHeaders: Map<String, String> = emptyMap()
)

/**
 * Chat message for UI
 */
@Serializable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val model: String? = null,
    val tokens: Int? = null
)

/**
 * Chat session for organizing conversations
 */
@Serializable
data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val promptTemplate: String? = null
)

/**
 * Prompt template for reusable prompts
 */
@Serializable
data class PromptTemplate(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val content: String,
    val category: String,
    val variables: List<String> = emptyList(),
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Available AI models by provider
 */
object AIModels {
    val OPENAI_MODELS = listOf(
        AIModel("gpt-4-turbo-preview", "GPT-4 Turbo", AIProvider.OPENAI, 128000, 4096, true, 0.01),
        AIModel("gpt-4", "GPT-4", AIProvider.OPENAI, 8192, 4096, true, 0.03),
        AIModel("gpt-3.5-turbo", "GPT-3.5 Turbo", AIProvider.OPENAI, 16385, 4096, true, 0.0015)
    )
    
    val ANTHROPIC_MODELS = listOf(
        AIModel("claude-3-opus-20240229", "Claude 3 Opus", AIProvider.ANTHROPIC, 200000, 4096, true, 0.015),
        AIModel("claude-3-sonnet-20240229", "Claude 3 Sonnet", AIProvider.ANTHROPIC, 200000, 4096, true, 0.003),
        AIModel("claude-3-haiku-20240307", "Claude 3 Haiku", AIProvider.ANTHROPIC, 200000, 4096, true, 0.00025)
    )
    
    val GOOGLE_MODELS = listOf(
        AIModel("gemini-pro", "Gemini Pro", AIProvider.GOOGLE, 30720, 2048, true, 0.0005),
        AIModel("gemini-pro-vision", "Gemini Pro Vision", AIProvider.GOOGLE, 16384, 2048, false, 0.0005)
    )
    
    fun getAllModels(): List<AIModel> = OPENAI_MODELS + ANTHROPIC_MODELS + GOOGLE_MODELS
    
    fun getModelsForProvider(provider: AIProvider): List<AIModel> = when (provider) {
        AIProvider.OPENAI -> OPENAI_MODELS
        AIProvider.ANTHROPIC -> ANTHROPIC_MODELS
        AIProvider.GOOGLE -> GOOGLE_MODELS
        AIProvider.OLLAMA -> emptyList() // Ollama models are fetched dynamically
        AIProvider.CUSTOM -> emptyList() // Custom models are user-defined
    }
}
