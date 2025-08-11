package com.mydashboardapp.ai.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mydashboardapp.ai.data.models.PromptTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptTemplatesScreen(
    modifier: Modifier = Modifier,
    viewModel: PromptTemplatesViewModel = hiltViewModel(),
    onTemplateSelected: (PromptTemplate) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prompt Templates") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateTemplateDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Template")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateTemplateDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Template")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category filter chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All") }
                    )
                    uiState.categories.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }
            }
            
            // Templates list
            items(uiState.filteredTemplates) { template ->
                TemplateCard(
                    template = template,
                    onUseTemplate = { 
                        onTemplateSelected(template)
                        onNavigateBack()
                    },
                    onEditTemplate = { viewModel.editTemplate(template) },
                    onDeleteTemplate = { viewModel.deleteTemplate(template) }
                )
            }
        }
    }
    
    // Create/Edit Template Dialog
    if (uiState.showTemplateDialog) {
        TemplateDialog(
            template = uiState.editingTemplate,
            onSave = { template ->
                viewModel.saveTemplate(template)
            },
            onDismiss = { viewModel.hideTemplateDialog() }
        )
    }
    
    // Delete confirmation dialog
    uiState.templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Template") },
            text = { Text("Are you sure you want to delete '${template.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateCard(
    template: PromptTemplate,
    onUseTemplate: () -> Unit,
    onEditTemplate: () -> Unit,
    onDeleteTemplate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (template.isBuiltIn) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = { },
                                label = { Text("Built-in", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = template.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column {
                    IconButton(onClick = onUseTemplate) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Use Template",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            if (template.variables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Variables: ${template.variables.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!template.isBuiltIn) {
                    TextButton(onClick = onEditTemplate) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    
                    TextButton(
                        onClick = onDeleteTemplate,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
                
                Button(onClick = onUseTemplate) {
                    Text("Use Template")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateDialog(
    template: PromptTemplate?,
    onSave: (PromptTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(template) { mutableStateOf(template?.title ?: "") }
    var description by remember(template) { mutableStateOf(template?.description ?: "") }
    var content by remember(template) { mutableStateOf(template?.content ?: "") }
    var category by remember(template) { mutableStateOf(template?.category ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Create Template" else "Edit Template") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Template Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    placeholder = { Text("Use {{variable_name}} for variables") }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tip: Use {{variable_name}} to create placeholders that can be filled in later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        val newTemplate = PromptTemplate(
                            id = template?.id ?: "",
                            title = title,
                            description = description,
                            content = content,
                            category = category.ifBlank { "General" },
                            variables = extractVariables(content),
                            isBuiltIn = false
                        )
                        onSave(newTemplate)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun extractVariables(content: String): List<String> {
    val pattern = "\\{\\{([^}]+)\\}\\}".toRegex()
    return pattern.findAll(content).map { it.groupValues[1] }.distinct().toList()
}
