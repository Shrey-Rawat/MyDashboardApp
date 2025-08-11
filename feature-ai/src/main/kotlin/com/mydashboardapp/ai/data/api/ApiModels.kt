package com.mydashboardapp.ai.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// OpenAI API Models
@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Float = 0.7f,
    @SerialName("max_tokens")
    val maxTokens: Int = 1024,
    val stream: Boolean = false
)

@Serializable
data class OpenAIChoice(
    val index: Int,
    val message: OpenAIMessage? = null,
    val delta: OpenAIMessage? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class OpenAIUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

@Serializable
data class OpenAIResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<OpenAIChoice>,
    val usage: OpenAIUsage? = null
)

// Anthropic API Models
@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String
)

@Serializable
data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens")
    val maxTokens: Int = 1024,
    val messages: List<AnthropicMessage>,
    val temperature: Float = 0.7f,
    val stream: Boolean = false
)

@Serializable
data class AnthropicUsage(
    @SerialName("input_tokens")
    val inputTokens: Int,
    @SerialName("output_tokens")
    val outputTokens: Int
)

@Serializable
data class AnthropicContent(
    val type: String,
    val text: String
)

@Serializable
data class AnthropicResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<AnthropicContent>,
    val model: String,
    @SerialName("stop_reason")
    val stopReason: String? = null,
    @SerialName("stop_sequence")
    val stopSequence: String? = null,
    val usage: AnthropicUsage
)

@Serializable
data class AnthropicDelta(
    val type: String,
    val text: String? = null
)

@Serializable
data class AnthropicStreamResponse(
    val type: String,
    val index: Int? = null,
    val delta: AnthropicDelta? = null,
    val message: AnthropicResponse? = null,
    val usage: AnthropicUsage? = null
)

// Google AI API Models
@Serializable
data class GoogleContent(
    val parts: List<GooglePart>,
    val role: String? = null
)

@Serializable
data class GooglePart(
    val text: String
)

@Serializable
data class GoogleGenerationConfig(
    val temperature: Float = 0.7f,
    @SerialName("maxOutputTokens")
    val maxOutputTokens: Int = 1024
)

@Serializable
data class GoogleRequest(
    val contents: List<GoogleContent>,
    @SerialName("generationConfig")
    val generationConfig: GoogleGenerationConfig
)

@Serializable
data class GoogleCandidate(
    val content: GoogleContent,
    @SerialName("finishReason")
    val finishReason: String? = null,
    val index: Int = 0
)

@Serializable
data class GoogleUsageMetadata(
    @SerialName("promptTokenCount")
    val promptTokenCount: Int,
    @SerialName("candidatesTokenCount")
    val candidatesTokenCount: Int,
    @SerialName("totalTokenCount")
    val totalTokenCount: Int
)

@Serializable
data class GoogleResponse(
    val candidates: List<GoogleCandidate>,
    @SerialName("usageMetadata")
    val usageMetadata: GoogleUsageMetadata? = null
)

// Generic error response
@Serializable
data class APIError(
    val error: APIErrorDetails
)

@Serializable
data class APIErrorDetails(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

// Common streaming event
sealed class StreamEvent {
    data class Content(val text: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
    object Done : StreamEvent()
}
