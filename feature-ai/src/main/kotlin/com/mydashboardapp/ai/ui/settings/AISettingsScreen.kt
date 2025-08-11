package com.mydashboardapp.ai.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mydashboardapp.ai.data.models.AIProvider
import com.mydashboardapp.ai.data.models.ProviderConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AISettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "AI Provider Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure your AI providers and API keys",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        items(AIProvider.values().toList()) { provider ->
            ProviderConfigCard(
                provider = provider,
                config = uiState.providerConfigs[provider],
                onConfigUpdate = { viewModel.updateProviderConfig(it) },
                onTestConnection = { viewModel.testConnection(provider) },
                isLoading = uiState.testingProvider == provider
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "General Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Default Provider
                    ExposedDropdownMenuBox(
                        expanded = uiState.showProviderDropdown,
                        onExpandedChange = { viewModel.toggleProviderDropdown() }
                    ) {
                        OutlinedTextField(
                            value = uiState.defaultProvider?.displayName ?: "Select Provider",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Default Provider") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.showProviderDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = uiState.showProviderDropdown,
                            onDismissRequest = { viewModel.toggleProviderDropdown() }
                        ) {
                            uiState.configuredProviders.forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text(provider.displayName) },
                                    onClick = {
                                        viewModel.setDefaultProvider(provider)
                                        viewModel.toggleProviderDropdown()
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Streaming enabled
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Streaming",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Stream responses in real-time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.streamingEnabled,
                            onCheckedChange = { viewModel.setStreamingEnabled(it) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Save chat history
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Save Chat History",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Keep conversation history locally",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.saveChatHistory,
                            onCheckedChange = { viewModel.setSaveChatHistory(it) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Temperature slider
                    Column {
                        Text(
                            text = "Temperature: ${String.format("%.1f", uiState.defaultTemperature)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Controls randomness in responses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = uiState.defaultTemperature,
                            onValueChange = { viewModel.setDefaultTemperature(it) },
                            valueRange = 0f..2f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Clear all data button
        item {
            OutlinedButton(
                onClick = { viewModel.showClearDataDialog() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All AI Data")
            }
        }
    }
    
    // Clear data confirmation dialog
    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDataDialog() },
            title = { Text("Clear All AI Data") },
            text = { Text("This will delete all API keys, chat history, and settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearAllData() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDataDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Show connection test results
    LaunchedEffect(uiState.connectionTestResult) {
        uiState.connectionTestResult?.let { result ->
            // Show snackbar with result
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderConfigCard(
    provider: AIProvider,
    config: ProviderConfig?,
    onConfigUpdate: (ProviderConfig) -> Unit,
    onTestConnection: () -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var apiKey by remember(config) { mutableStateOf(config?.apiKey ?: "") }
    var baseUrl by remember(config) { mutableStateOf(config?.baseUrl ?: provider.baseUrl) }
    var selectedModel by remember(config) { mutableStateOf(config?.selectedModel ?: "") }
    var isEnabled by remember(config) { mutableStateOf(config?.isEnabled ?: true) }
    var showPassword by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (provider) {
                            AIProvider.OPENAI -> Icons.Default.SmartToy
                            AIProvider.ANTHROPIC -> Icons.Default.Psychology
                            AIProvider.GOOGLE -> Icons.Default.Search
                            AIProvider.OLLAMA -> Icons.Default.Computer
                            AIProvider.CUSTOM -> Icons.Default.Settings
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = provider.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (config != null && config.apiKey.isNotBlank()) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Configured",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    IconButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enable/Disable switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enabled", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { 
                            isEnabled = it
                            onConfigUpdate(
                                config?.copy(isEnabled = it) ?: ProviderConfig(
                                    provider = provider,
                                    apiKey = apiKey,
                                    baseUrl = baseUrl,
                                    selectedModel = selectedModel,
                                    isEnabled = it
                                )
                            )
                        }
                    )
                }
                
                if (provider.requiresApiKey) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { 
                            apiKey = it
                            onConfigUpdate(
                                config?.copy(apiKey = it) ?: ProviderConfig(
                                    provider = provider,
                                    apiKey = it,
                                    baseUrl = baseUrl,
                                    selectedModel = selectedModel,
                                    isEnabled = isEnabled
                                )
                            )
                        },
                        label = { Text("API Key") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide" else "Show"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (provider == AIProvider.CUSTOM) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { 
                            baseUrl = it
                            onConfigUpdate(
                                config?.copy(baseUrl = it) ?: ProviderConfig(
                                    provider = provider,
                                    apiKey = apiKey,
                                    baseUrl = it,
                                    selectedModel = selectedModel,
                                    isEnabled = isEnabled
                                )
                            )
                        },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = { 
                        selectedModel = it
                        onConfigUpdate(
                            config?.copy(selectedModel = it) ?: ProviderConfig(
                                provider = provider,
                                apiKey = apiKey,
                                baseUrl = baseUrl,
                                selectedModel = it,
                                isEnabled = isEnabled
                            )
                        )
                    },
                    label = { Text("Model") },
                    placeholder = { Text("e.g., gpt-4, claude-3-sonnet") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (config?.apiKey?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onTestConnection,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing...")
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Connection")
                        }
                    }
                }
            }
        }
    }
}
