package com.mydashboardapp.nutrition.ui

import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.ui.BaseViewModel
import com.mydashboardapp.core.ui.UiState
import com.mydashboardapp.data.entities.Ingredient
import com.mydashboardapp.data.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for ingredient editor screen
 */
data class IngredientEditorUiState(
    val ingredientId: Long? = null,
    val name: String = "",
    val category: String = "",
    val caloriesPerGram: String = "",
    val proteinPerGram: String = "",
    val carbsPerGram: String = "",
    val fatPerGram: String = "",
    val fiberPerGram: String = "",
    val sugarPerGram: String = "",
    val sodiumPerGram: String = "",
    val isAllergen: Boolean = false,
    val allergenInfo: String = "",
    val availableCategories: List<String> = emptyList(),
    val isValid: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) : UiState

@HiltViewModel
class IngredientEditorViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : BaseViewModel<IngredientEditorUiState>(
    initialState = IngredientEditorUiState()
) {

    init {
        loadAvailableCategories()
    }

    private fun loadAvailableCategories() {
        val commonCategories = listOf(
            "Vegetables",
            "Fruits", 
            "Proteins",
            "Grains & Cereals",
            "Dairy & Eggs",
            "Nuts & Seeds",
            "Oils & Fats",
            "Spices & Herbs",
            "Sweeteners",
            "Beverages",
            "Legumes",
            "Fish & Seafood",
            "Meat & Poultry"
        )

        updateState { 
            it.copy(availableCategories = commonCategories)
        }
    }

    /**
     * Update ingredient name
     */
    fun updateName(name: String) {
        updateState { 
            it.copy(name = name)
        }
        validateForm()
    }

    /**
     * Update ingredient category
     */
    fun updateCategory(category: String) {
        updateState { 
            it.copy(category = category)
        }
    }

    /**
     * Update calories per gram
     */
    fun updateCaloriesPerGram(value: String) {
        updateState { 
            it.copy(caloriesPerGram = value)
        }
        validateForm()
    }

    /**
     * Update protein per gram
     */
    fun updateProteinPerGram(value: String) {
        updateState { 
            it.copy(proteinPerGram = value)
        }
        validateForm()
    }

    /**
     * Update carbs per gram
     */
    fun updateCarbsPerGram(value: String) {
        updateState { 
            it.copy(carbsPerGram = value)
        }
        validateForm()
    }

    /**
     * Update fat per gram
     */
    fun updateFatPerGram(value: String) {
        updateState { 
            it.copy(fatPerGram = value)
        }
        validateForm()
    }

    /**
     * Update fiber per gram (optional)
     */
    fun updateFiberPerGram(value: String) {
        updateState { 
            it.copy(fiberPerGram = value)
        }
    }

    /**
     * Update sugar per gram (optional)
     */
    fun updateSugarPerGram(value: String) {
        updateState { 
            it.copy(sugarPerGram = value)
        }
    }

    /**
     * Update sodium per gram (optional)
     */
    fun updateSodiumPerGram(value: String) {
        updateState { 
            it.copy(sodiumPerGram = value)
        }
    }

    /**
     * Update allergen flag
     */
    fun updateIsAllergen(isAllergen: Boolean) {
        updateState { 
            it.copy(
                isAllergen = isAllergen,
                allergenInfo = if (!isAllergen) "" else it.allergenInfo
            )
        }
    }

    /**
     * Update allergen information
     */
    fun updateAllergenInfo(info: String) {
        updateState { 
            it.copy(allergenInfo = info)
        }
    }

    /**
     * Load existing ingredient for editing
     */
    fun loadIngredient(ingredientId: Long) {
        launchWithErrorHandling {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val ingredient = nutritionRepository.getIngredientById(ingredientId)
                
                if (ingredient != null) {
                    updateState { currentState ->
                        currentState.copy(
                            ingredientId = ingredient.id,
                            name = ingredient.name,
                            category = ingredient.category ?: "",
                            caloriesPerGram = ingredient.caloriesPerGram.toString(),
                            proteinPerGram = ingredient.proteinPerGram.toString(),
                            carbsPerGram = ingredient.carbsPerGram.toString(),
                            fatPerGram = ingredient.fatPerGram.toString(),
                            fiberPerGram = ingredient.fiberPerGram?.toString() ?: "",
                            sugarPerGram = ingredient.sugarPerGram?.toString() ?: "",
                            sodiumPerGram = ingredient.sodiumPerGram?.toString() ?: "",
                            isAllergen = ingredient.isAllergen,
                            allergenInfo = ingredient.allergenInfo ?: "",
                            isLoading = false
                        )
                    }
                    validateForm()
                } else {
                    updateState { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ingredient not found"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading ingredient: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Save the ingredient
     */
    fun saveIngredient() {
        if (!currentState.isValid) return

        launchWithErrorHandling {
            updateState { it.copy(isLoading = true, errorMessage = null) }

            try {
                val ingredient = createIngredientFromState()
                
                if (currentState.ingredientId != null) {
                    // Update existing ingredient
                    nutritionRepository.updateIngredient(ingredient)
                } else {
                    // Create new ingredient
                    nutritionRepository.insertIngredient(ingredient)
                }

                updateState { 
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error saving ingredient: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Create ingredient entity from current state
     */
    private fun createIngredientFromState(): Ingredient {
        val state = currentState
        
        return Ingredient(
            id = state.ingredientId ?: 0,
            name = state.name.trim(),
            category = state.category.trim().ifEmpty { null },
            caloriesPerGram = state.caloriesPerGram.toDoubleOrNull() ?: 0.0,
            proteinPerGram = state.proteinPerGram.toDoubleOrNull() ?: 0.0,
            carbsPerGram = state.carbsPerGram.toDoubleOrNull() ?: 0.0,
            fatPerGram = state.fatPerGram.toDoubleOrNull() ?: 0.0,
            fiberPerGram = state.fiberPerGram.toDoubleOrNull(),
            sugarPerGram = state.sugarPerGram.toDoubleOrNull(),
            sodiumPerGram = state.sodiumPerGram.toDoubleOrNull(),
            isAllergen = state.isAllergen,
            allergenInfo = if (state.isAllergen) state.allergenInfo.trim().ifEmpty { null } else null
        )
    }

    /**
     * Validate the form
     */
    private fun validateForm() {
        val state = currentState
        
        val isValid = state.name.isNotBlank() &&
                state.caloriesPerGram.toDoubleOrNull() != null &&
                state.caloriesPerGram.toDoubleOrNull()!! >= 0 &&
                state.proteinPerGram.toDoubleOrNull() != null &&
                state.proteinPerGram.toDoubleOrNull()!! >= 0 &&
                state.carbsPerGram.toDoubleOrNull() != null &&
                state.carbsPerGram.toDoubleOrNull()!! >= 0 &&
                state.fatPerGram.toDoubleOrNull() != null &&
                state.fatPerGram.toDoubleOrNull()!! >= 0 &&
                // Validate optional fields if they're not empty
                (state.fiberPerGram.isEmpty() || (state.fiberPerGram.toDoubleOrNull() != null && state.fiberPerGram.toDoubleOrNull()!! >= 0)) &&
                (state.sugarPerGram.isEmpty() || (state.sugarPerGram.toDoubleOrNull() != null && state.sugarPerGram.toDoubleOrNull()!! >= 0)) &&
                (state.sodiumPerGram.isEmpty() || (state.sodiumPerGram.toDoubleOrNull() != null && state.sodiumPerGram.toDoubleOrNull()!! >= 0))
        
        updateState { 
            it.copy(isValid = isValid)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    /**
     * Reset form to create a new ingredient
     */
    fun resetForm() {
        updateState { 
            IngredientEditorUiState(
                availableCategories = it.availableCategories
            )
        }
    }
}
