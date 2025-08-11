package com.mydashboardapp.ai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydashboardapp.ai.data.api.StreamEvent
import com.mydashboardapp.ai.data.models.*
import com.mydashboardapp.ai.data.security.SecureStorage
import com.mydashboardapp.ai.data.service.AIService
import com.mydashboardapp.ai.data.service.PromptTemplateService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val currentSession: ChatSession? = null,
    val currentProvider: AIProvider? = null,
    val hasConfiguredProvider: Boolean = false,
    val quickTemplates: List<PromptTemplate> = emptyList(),
    val streamingEnabled: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiService: AIService,
    private val secureStorage: SecureStorage,
    private val promptTemplateService: PromptTemplateService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()
    
    private var currentStreamingMessage: ChatMessage? = null
    private var currentProviderConfig: ProviderConfig? = null
    
    init {
        initializeChat()
    }
    
    private fun initializeChat() {
        viewModelScope.launch {
            try {
                // Load settings
                val streamingEnabled = secureStorage.getAIPreference(
                    SecureStorage.PREF_STREAMING_ENABLED, true
                )
                
                val defaultProviderName = secureStorage.getAIPreference(
                    SecureStorage.PREF_DEFAULT_PROVIDER
                )
                
                val defaultProvider = if (defaultProviderName.isNotBlank()) {
                    AIProvider.values().find { it.name == defaultProviderName }
                } else {
                    null
                }
                
                val providerConfig = defaultProvider?.let { provider ->
                    secureStorage.getProviderConfig(provider)
                }
                
                currentProviderConfig = providerConfig
                
                // Load quick templates
                val quickTemplates = promptTemplateService.getAllTemplates()
                    .filter { it.category in listOf("Productivity", "Planning", "Analytics") }
                    .take(5)
                
                _uiState.value = _uiState.value.copy(
                    currentProvider = defaultProvider,
                    hasConfiguredProvider = providerConfig != null && providerConfig.apiKey.isNotBlank(),
                    streamingEnabled = streamingEnabled,
                    quickTemplates = quickTemplates
                )
                
                // Start a new chat session
                startNewChat()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to initialize chat: ${e.message}"
                )
            }
        }
    }
    
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }
    
    fun sendMessage() {
        val inputText = _uiState.value.inputText.trim()
        if (inputText.isBlank() || _uiState.value.isStreaming) return
        
        val config = currentProviderConfig
        if (config == null || !_uiState.value.hasConfiguredProvider) {
            _uiState.value = _uiState.value.copy(
                error = "Please configure an AI provider first"
            )
            return
        }
        
        // Add user message
        val userMessage = ChatMessage(
            content = inputText,
            isFromUser = true
        )
        
        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            inputText = "",
            isStreaming = true
        )
        
        // Send to AI
        if (_uiState.value.streamingEnabled) {
            sendStreamingMessage(updatedMessages, config)
        } else {
            sendNonStreamingMessage(updatedMessages, config)
        }
    }
    
    private fun sendStreamingMessage(messages: List<ChatMessage>, config: ProviderConfig) {
        viewModelScope.launch {
            try {
                var streamedContent = ""
                currentStreamingMessage = ChatMessage(
                    content = "",
                    isFromUser = false,
                    isStreaming = true
                )
                
                aiService.sendMessageStream(messages, config).collect { event ->
                    when (event) {
                        is StreamEvent.Content -> {
                            streamedContent += event.text
                            val updatedMessage = currentStreamingMessage?.copy(
                                content = streamedContent
                            )
                            
                            if (updatedMessage != null) {
                                currentStreamingMessage = updatedMessage
                                updateStreamingMessage(updatedMessage)
                            }
                        }
                        is StreamEvent.Done -> {
                            val finalMessage = currentStreamingMessage?.copy(
                                isStreaming = false,
                                model = config.selectedModel
                            )
                            
                            if (finalMessage != null) {
                                completeStreamingMessage(finalMessage)
                            }
                        }
                        is StreamEvent.Error -> {
                            val errorMessage = ChatMessage(
                                content = event.message,
                                isFromUser = false,
                                isError = true
                            )
                            completeStreamingMessage(errorMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Error: ${e.message}",
                    isFromUser = false,
                    isError = true
                )
                completeStreamingMessage(errorMessage)
            }
        }
    }
    
    private fun sendNonStreamingMessage(messages: List<ChatMessage>, config: ProviderConfig) {
        viewModelScope.launch {
            try {
                val response = aiService.sendMessage(messages, config)
                val updatedMessages = _uiState.value.messages + response
                
                _uiState.value = _uiState.value.copy(
                    messages = updatedMessages,
                    isStreaming = false
                )
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Error: ${e.message}",
                    isFromUser = false,
                    isError = true
                )
                val updatedMessages = _uiState.value.messages + errorMessage
                
                _uiState.value = _uiState.value.copy(
                    messages = updatedMessages,
                    isStreaming = false
                )
            }
        }
    }
    
    private fun updateStreamingMessage(message: ChatMessage) {
        val messages = _uiState.value.messages.toMutableList()
        
        // Find and update streaming message, or add it if it doesn't exist
        val streamingIndex = messages.indexOfLast { it.isStreaming }
        if (streamingIndex != -1) {
            messages[streamingIndex] = message
        } else {
            messages.add(message)
        }
        
        _uiState.value = _uiState.value.copy(messages = messages)
    }
    
    private fun completeStreamingMessage(message: ChatMessage) {
        val messages = _uiState.value.messages.toMutableList()
        
        // Replace streaming message with final message
        val streamingIndex = messages.indexOfLast { it.isStreaming }
        if (streamingIndex != -1) {
            messages[streamingIndex] = message
        } else {
            messages.add(message)
        }
        
        _uiState.value = _uiState.value.copy(
            messages = messages,
            isStreaming = false
        )
        
        currentStreamingMessage = null
    }
    
    fun regenerateLastResponse() {
        val messages = _uiState.value.messages
        if (messages.isEmpty() || _uiState.value.isStreaming) return
        
        // Find the last user message and remove all messages after it
        val lastUserMessageIndex = messages.indexOfLast { it.isFromUser }
        if (lastUserMessageIndex == -1) return
        
        val messagesToKeep = messages.take(lastUserMessageIndex + 1)
        
        val config = currentProviderConfig
        if (config == null || !_uiState.value.hasConfiguredProvider) {
            _uiState.value = _uiState.value.copy(
                error = "Please configure an AI provider first"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            messages = messagesToKeep,
            isStreaming = true
        )
        
        // Regenerate response
        if (_uiState.value.streamingEnabled) {
            sendStreamingMessage(messagesToKeep, config)
        } else {
            sendNonStreamingMessage(messagesToKeep, config)
        }
    }
    
    fun selectTemplate(template: PromptTemplate) {
        // If template has variables, we should show a dialog to fill them
        // For now, we'll just use the template content as is
        _uiState.value = _uiState.value.copy(inputText = template.content)
    }
    
    fun startNewChat() {
        val newSession = ChatSession(
            title = "New Chat",
            messages = emptyList()
        )
        
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            currentSession = newSession,
            inputText = "",
            isStreaming = false
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
