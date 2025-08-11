package com.mydashboardapp.ai.data.service

import com.mydashboardapp.ai.data.api.*
import com.mydashboardapp.ai.data.models.AIProvider
import com.mydashboardapp.ai.data.models.ChatMessage
import com.mydashboardapp.ai.data.models.ProviderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIServiceImpl @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val anthropicApi: AnthropicApi,
    private val googleAIApi: GoogleAIApi,
    private val okHttpClient: OkHttpClient
) : AIService {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun sendMessage(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): ChatMessage = withContext(Dispatchers.IO) {
        try {
            when (config.provider) {
                AIProvider.OPENAI -> sendOpenAIMessage(messages, config)
                AIProvider.ANTHROPIC -> sendAnthropicMessage(messages, config)
                AIProvider.GOOGLE -> sendGoogleMessage(messages, config)
                else -> throw UnsupportedOperationException("Provider ${config.provider} not supported for non-streaming")
            }
        } catch (e: Exception) {
            ChatMessage(
                content = "Error: ${e.message}",
                isFromUser = false,
                isError = true
            )
        }
    }
    
    override fun sendMessageStream(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): Flow<StreamEvent> = flow {
        try {
            when (config.provider) {
                AIProvider.OPENAI -> {
                    val request = buildOpenAIRequest(messages, config, stream = true)
                    streamOpenAI(request, config).collect { emit(it) }
                }
                AIProvider.ANTHROPIC -> {
                    val request = buildAnthropicRequest(messages, config, stream = true)
                    streamAnthropic(request, config).collect { emit(it) }
                }
                AIProvider.GOOGLE -> {
                    val request = buildGoogleRequest(messages, config)
                    streamGoogle(request, config).collect { emit(it) }
                }
                else -> emit(StreamEvent.Error("Provider ${config.provider} not supported for streaming"))
            }
        } catch (e: Exception) {
            emit(StreamEvent.Error("Stream error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun validateConnection(config: ProviderConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            val testMessage = ChatMessage(content = "Test connection", isFromUser = true)
            when (config.provider) {
                AIProvider.OPENAI -> {
                    val request = buildOpenAIRequest(listOf(testMessage), config.copy(maxTokens = 1))
                    openAIApi.chatCompletions("Bearer ${config.apiKey}", request)
                    true
                }
                AIProvider.ANTHROPIC -> {
                    val request = buildAnthropicRequest(listOf(testMessage), config.copy(maxTokens = 1))
                    anthropicApi.messages(config.apiKey, "2023-06-01", request)
                    true
                }
                AIProvider.GOOGLE -> {
                    val request = buildGoogleRequest(listOf(testMessage), config.copy(maxTokens = 1))
                    googleAIApi.generateContent(config.selectedModel, config.apiKey, request)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun sendOpenAIMessage(messages: List<ChatMessage>, config: ProviderConfig): ChatMessage {
        val request = buildOpenAIRequest(messages, config)
        val response = openAIApi.chatCompletions("Bearer ${config.apiKey}", request)
        
        return ChatMessage(
            content = response.choices.firstOrNull()?.message?.content ?: "No response",
            isFromUser = false,
            model = response.model,
            tokens = response.usage?.totalTokens
        )
    }
    
    private suspend fun sendAnthropicMessage(messages: List<ChatMessage>, config: ProviderConfig): ChatMessage {
        val request = buildAnthropicRequest(messages, config)
        val response = anthropicApi.messages(config.apiKey, "2023-06-01", request)
        
        return ChatMessage(
            content = response.content.firstOrNull()?.text ?: "No response",
            isFromUser = false,
            model = response.model,
            tokens = response.usage.inputTokens + response.usage.outputTokens
        )
    }
    
    private suspend fun sendGoogleMessage(messages: List<ChatMessage>, config: ProviderConfig): ChatMessage {
        val request = buildGoogleRequest(messages, config)
        val response = googleAIApi.generateContent(config.selectedModel, config.apiKey, request)
        
        return ChatMessage(
            content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response",
            isFromUser = false,
            model = config.selectedModel,
            tokens = response.usageMetadata?.totalTokenCount
        )
    }
    
    private fun streamOpenAI(request: OpenAIRequest, config: ProviderConfig): Flow<StreamEvent> = flow {
        val requestBody = json.encodeToString(OpenAIRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())
        
        val httpRequest = Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Accept", "text/event-stream")
            .post(requestBody)
            .build()
        
        var eventSource: EventSource? = null
        var isDone = false
        
        try {
            val eventSourceListener = object : EventSourceListener() {
                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    if (data == "[DONE]") {
                        isDone = true
                        trySend(StreamEvent.Done)
                        return
                    }
                    
                    try {
                        val response = json.decodeFromString<OpenAIResponse>(data)
                        val delta = response.choices.firstOrNull()?.delta?.content
                        if (!delta.isNullOrEmpty()) {
                            trySend(StreamEvent.Content(delta))
                        }
                    } catch (e: Exception) {
                        // Ignore malformed JSON
                    }
                }
                
                override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                    isDone = true
                    trySend(StreamEvent.Error(t?.message ?: "Stream failed"))
                }
            }
            
            eventSource = EventSources.createFactory(okHttpClient).newEventSource(httpRequest, eventSourceListener)
            
            // Wait until stream is done
            while (!isDone) {
                kotlinx.coroutines.delay(100)
            }
        } finally {
            eventSource?.cancel()
        }
    }
    
    private fun streamAnthropic(request: AnthropicRequest, config: ProviderConfig): Flow<StreamEvent> = flow {
        val requestBody = json.encodeToString(AnthropicRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())
        
        val httpRequest = Request.Builder()
            .url("${config.baseUrl}/messages")
            .header("x-api-key", config.apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Accept", "text/event-stream")
            .post(requestBody)
            .build()
        
        val eventSourceListener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val response = json.decodeFromString<AnthropicStreamResponse>(data)
                    when (response.type) {
                        "content_block_delta" -> {
                            response.delta?.text?.let { text ->
                                emit(StreamEvent.Content(text))
                            }
                        }
                        "message_stop" -> {
                            emit(StreamEvent.Done)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore malformed JSON
                }
            }
            
            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                emit(StreamEvent.Error(t?.message ?: "Stream failed"))
            }
        }
        
        val eventSource = EventSources.createFactory(okHttpClient).newEventSource(httpRequest, eventSourceListener)
        
        try {
            kotlinx.coroutines.delay(Long.MAX_VALUE)
        } finally {
            eventSource.cancel()
        }
    }
    
    private fun streamGoogle(request: GoogleRequest, config: ProviderConfig): Flow<StreamEvent> = flow {
        // Google AI streaming implementation would go here
        // For now, fall back to regular request
        try {
            val response = googleAIApi.generateContent(config.selectedModel, config.apiKey, request)
            val content = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!content.isNullOrEmpty()) {
                emit(StreamEvent.Content(content))
            }
            emit(StreamEvent.Done)
        } catch (e: Exception) {
            emit(StreamEvent.Error(e.message ?: "Google AI request failed"))
        }
    }
    
    private fun buildOpenAIRequest(
        messages: List<ChatMessage>,
        config: ProviderConfig,
        stream: Boolean = false
    ): OpenAIRequest {
        val openAIMessages = messages.map { message ->
            OpenAIMessage(
                role = if (message.isFromUser) "user" else "assistant",
                content = message.content
            )
        }
        
        return OpenAIRequest(
            model = config.selectedModel,
            messages = openAIMessages,
            temperature = config.temperature,
            maxTokens = config.maxTokens,
            stream = stream
        )
    }
    
    private fun buildAnthropicRequest(
        messages: List<ChatMessage>,
        config: ProviderConfig,
        stream: Boolean = false
    ): AnthropicRequest {
        val anthropicMessages = messages.map { message ->
            AnthropicMessage(
                role = if (message.isFromUser) "user" else "assistant",
                content = message.content
            )
        }
        
        return AnthropicRequest(
            model = config.selectedModel,
            maxTokens = config.maxTokens,
            messages = anthropicMessages,
            temperature = config.temperature,
            stream = stream
        )
    }
    
    private fun buildGoogleRequest(
        messages: List<ChatMessage>,
        config: ProviderConfig
    ): GoogleRequest {
        val contents = messages.map { message ->
            GoogleContent(
                parts = listOf(GooglePart(message.content)),
                role = if (message.isFromUser) "user" else "model"
            )
        }
        
        return GoogleRequest(
            contents = contents,
            generationConfig = GoogleGenerationConfig(
                temperature = config.temperature,
                maxOutputTokens = config.maxTokens
            )
        )
    }
}
