package com.mydashboardapp.ai.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydashboardapp.ai.data.models.PromptTemplate
import com.mydashboardapp.ai.data.service.PromptTemplateService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PromptTemplatesUiState(
    val allTemplates: List<PromptTemplate> = emptyList(),
    val filteredTemplates: List<PromptTemplate> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val showTemplateDialog: Boolean = false,
    val editingTemplate: PromptTemplate? = null,
    val templateToDelete: PromptTemplate? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PromptTemplatesViewModel @Inject constructor(
    private val promptTemplateService: PromptTemplateService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PromptTemplatesUiState())
    val uiState: StateFlow<PromptTemplatesUiState> = _uiState.asStateFlow()
    
    init {
        loadTemplates()
    }
    
    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val templates = promptTemplateService.getAllTemplates()
                val categories = templates.map { it.category }.distinct().sorted()
                
                _uiState.value = _uiState.value.copy(
                    allTemplates = templates,
                    filteredTemplates = templates,
                    categories = categories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load templates: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun selectCategory(category: String?) {
        val filteredTemplates = if (category == null) {
            _uiState.value.allTemplates
        } else {
            _uiState.value.allTemplates.filter { it.category == category }
        }
        
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredTemplates = filteredTemplates
        )
    }
    
    fun showCreateTemplateDialog() {
        _uiState.value = _uiState.value.copy(
            showTemplateDialog = true,
            editingTemplate = null
        )
    }
    
    fun editTemplate(template: PromptTemplate) {
        _uiState.value = _uiState.value.copy(
            showTemplateDialog = true,
            editingTemplate = template
        )
    }
    
    fun hideTemplateDialog() {
        _uiState.value = _uiState.value.copy(
            showTemplateDialog = false,
            editingTemplate = null
        )
    }
    
    fun saveTemplate(template: PromptTemplate) {
        viewModelScope.launch {
            try {
                if (_uiState.value.editingTemplate != null) {
                    // Update existing template
                    promptTemplateService.updateUserTemplate(template)
                } else {
                    // Create new template
                    promptTemplateService.addUserTemplate(template)
                }
                
                hideTemplateDialog()
                loadTemplates() // Refresh the list
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save template: ${e.message}"
                )
            }
        }
    }
    
    fun deleteTemplate(template: PromptTemplate) {
        if (template.isBuiltIn) {
            _uiState.value = _uiState.value.copy(
                error = "Cannot delete built-in templates"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(templateToDelete = template)
    }
    
    fun confirmDelete() {
        val templateToDelete = _uiState.value.templateToDelete
        if (templateToDelete != null) {
            viewModelScope.launch {
                try {
                    promptTemplateService.deleteUserTemplate(templateToDelete.id)
                    cancelDelete()
                    loadTemplates() // Refresh the list
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete template: ${e.message}",
                        templateToDelete = null
                    )
                }
            }
        }
    }
    
    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(templateToDelete = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
