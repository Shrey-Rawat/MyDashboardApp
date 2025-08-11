package com.mydashboardapp.nutrition.ui

import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.ui.BaseViewModel
import com.mydashboardapp.core.ui.UiState
import com.mydashboardapp.data.entities.Food
import com.mydashboardapp.data.entities.Meal
import com.mydashboardapp.data.entities.MealFoodCrossRef
import com.mydashboardapp.data.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for meal logging screen
 */
data class MealLoggingUiState(
    val currentMeal: Meal? = null,
    val selectedFoods: List<SelectedFoodItem> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val calculatedMacros: CalculatedMacros = CalculatedMacros(),
    val showMealTypeSelector: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

@OptIn(FlowPreview::class)
@HiltViewModel
class MealLoggingViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : BaseViewModel<MealLoggingUiState>(
    initialState = MealLoggingUiState()
) {

    private val _searchQuery = MutableStateFlow("")
    
    init {
        // Initialize with a new meal
        initializeNewMeal()
        
        // Setup reactive search
        setupSearchFlow()
    }
    
    private fun initializeNewMeal() {
        val newMeal = Meal(
            name = "New Meal",
            mealType = "Breakfast",
            dateConsumed = System.currentTimeMillis(),
            totalCalories = 0,
            totalProtein = 0.0,
            totalCarbs = 0.0,
            totalFat = 0.0
        )
        
        updateState { 
            it.copy(currentMeal = newMeal)
        }
    }
    
    private fun setupSearchFlow() {
        _searchQuery
            .debounce(300) // Debounce search queries
            .distinctUntilChanged()
            .filter { it.isNotBlank() && it.length >= 2 }
            .onEach { query ->
                updateState { it.copy(isLoading = true) }
                searchFoodsInternal(query)
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        updateState { it.copy(searchQuery = query) }
        _searchQuery.value = query
        
        // Clear results if query is empty
        if (query.isBlank()) {
            updateState { it.copy(searchResults = emptyList(), isLoading = false) }
        }
    }
    
    /**
     * Search foods manually (for button click)
     */
    fun searchFoods() {
        val query = currentState.searchQuery
        if (query.isNotBlank()) {
            _searchQuery.value = query
        }
    }
    
    private fun searchFoodsInternal(query: String) {
        launchWithErrorHandling {
            try {
                val results = nutritionRepository.searchFoods(query)
                updateState { currentState ->
                    currentState.copy(
                        searchResults = results,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "Error searching foods: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Add food to current meal
     */
    fun addFoodToMeal(food: Food) {
        val currentSelectedFoods = currentState.selectedFoods.toMutableList()
        
        // Check if food is already selected
        val existingIndex = currentSelectedFoods.indexOfFirst { it.food.id == food.id }
        
        if (existingIndex >= 0) {
            // Increase quantity if already selected
            val existing = currentSelectedFoods[existingIndex]
            currentSelectedFoods[existingIndex] = existing.copy(quantity = existing.quantity + 1.0)
        } else {
            // Add new food
            currentSelectedFoods.add(SelectedFoodItem(food = food, quantity = 1.0))
        }
        
        updateState { currentState ->
            currentState.copy(
                selectedFoods = currentSelectedFoods,
                calculatedMacros = calculateMacros(currentSelectedFoods)
            )
        }
    }
    
    /**
     * Remove food from current meal
     */
    fun removeFoodFromMeal(food: Food) {
        val currentSelectedFoods = currentState.selectedFoods.toMutableList()
        currentSelectedFoods.removeAll { it.food.id == food.id }
        
        updateState { currentState ->
            currentState.copy(
                selectedFoods = currentSelectedFoods,
                calculatedMacros = calculateMacros(currentSelectedFoods)
            )
        }
    }
    
    /**
     * Update quantity of selected food
     */
    fun updateFoodQuantity(food: Food, quantity: Double) {
        if (quantity <= 0) {
            removeFoodFromMeal(food)
            return
        }
        
        val currentSelectedFoods = currentState.selectedFoods.toMutableList()
        val index = currentSelectedFoods.indexOfFirst { it.food.id == food.id }
        
        if (index >= 0) {
            currentSelectedFoods[index] = currentSelectedFoods[index].copy(quantity = quantity)
            
            updateState { currentState ->
                currentState.copy(
                    selectedFoods = currentSelectedFoods,
                    calculatedMacros = calculateMacros(currentSelectedFoods)
                )
            }
        }
    }
    
    /**
     * Show meal type selector dialog
     */
    fun showMealTypeSelector() {
        updateState { it.copy(showMealTypeSelector = true) }
    }
    
    /**
     * Hide meal type selector dialog
     */
    fun hideMealTypeSelector() {
        updateState { it.copy(showMealTypeSelector = false) }
    }
    
    /**
     * Select meal type
     */
    fun selectMealType(mealType: String) {
        updateState { currentState ->
            val updatedMeal = currentState.currentMeal?.copy(
                mealType = mealType,
                name = "$mealType - ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            )
            currentState.copy(
                currentMeal = updatedMeal,
                showMealTypeSelector = false
            )
        }
    }
    
    /**
     * Save the current meal
     */
    fun saveMeal() {
        val currentMeal = currentState.currentMeal
        val selectedFoods = currentState.selectedFoods
        
        if (currentMeal == null || selectedFoods.isEmpty()) {
            updateState { 
                it.copy(errorMessage = "Please add at least one food item to save the meal.")
            }
            return
        }
        
        launchWithErrorHandling {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val macros = currentState.calculatedMacros
                
                // Create final meal with calculated macros
                val finalMeal = currentMeal.copy(
                    totalCalories = macros.totalCalories,
                    totalProtein = macros.totalProtein,
                    totalCarbs = macros.totalCarbs,
                    totalFat = macros.totalFat,
                    totalFiber = macros.totalFiber,
                    totalSugar = macros.totalSugar,
                    totalSodium = macros.totalSodium
                )
                
                // Save meal to database
                val mealId = nutritionRepository.insertMeal(finalMeal)
                
                // Save food associations
                selectedFoods.forEach { selectedFood ->
                    val mealFoodCrossRef = MealFoodCrossRef(
                        mealId = mealId,
                        foodId = selectedFood.food.id,
                        quantity = selectedFood.quantity,
                        weight = selectedFood.food.servingSize * selectedFood.quantity,
                        notes = null
                    )
                    nutritionRepository.addFoodToMeal(mealFoodCrossRef)
                }
                
                // Reset state for new meal
                initializeNewMeal()
                updateState { currentState ->
                    currentState.copy(
                        selectedFoods = emptyList(),
                        searchQuery = "",
                        searchResults = emptyList(),
                        calculatedMacros = CalculatedMacros(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
                
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "Error saving meal: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Calculate total macros for selected foods
     */
    private fun calculateMacros(selectedFoods: List<SelectedFoodItem>): CalculatedMacros {
        var totalCalories = 0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        var totalFiber = 0.0
        var totalSugar = 0.0
        var totalSodium = 0.0
        
        selectedFoods.forEach { selectedFood ->
            totalCalories += selectedFood.totalCalories
            totalProtein += selectedFood.totalProtein
            totalCarbs += selectedFood.totalCarbs
            totalFat += selectedFood.totalFat
            totalFiber += selectedFood.totalFiber ?: 0.0
            totalSugar += selectedFood.totalSugar ?: 0.0
            totalSodium += selectedFood.totalSodium ?: 0.0
        }
        
        return CalculatedMacros(
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            totalFiber = totalFiber,
            totalSugar = totalSugar,
            totalSodium = totalSodium
        )
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }
    
    /**
     * Load existing meal for editing
     */
    fun loadMeal(mealId: Long) {
        launchWithErrorHandling {
            updateState { it.copy(isLoading = true) }
            
            try {
                val meal = nutritionRepository.getMealById(mealId)
                val foods = nutritionRepository.getFoodsForMeal(mealId)
                
                if (meal != null) {
                    val selectedFoods = foods.mapNotNull { mealFood ->
                        nutritionRepository.getFoodById(mealFood.foodId)?.let { food ->
                            SelectedFoodItem(
                                food = food,
                                quantity = mealFood.quantity
                            )
                        }
                    }
                    
                    updateState { currentState ->
                        currentState.copy(
                            currentMeal = meal,
                            selectedFoods = selectedFoods,
                            calculatedMacros = calculateMacros(selectedFoods),
                            isLoading = false
                        )
                    }
                } else {
                    updateState { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Meal not found"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "Error loading meal: ${e.message}"
                    )
                }
            }
        }
    }
}
