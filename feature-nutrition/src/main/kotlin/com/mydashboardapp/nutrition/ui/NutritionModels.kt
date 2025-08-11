package com.mydashboardapp.nutrition.ui

import com.mydashboardapp.core.ui.UiState
import com.mydashboardapp.core.data.UserPreferences
import com.mydashboardapp.data.entities.Food
import com.mydashboardapp.data.entities.Meal

/**
 * UI State for nutrition home screen
 */
data class NutritionUiState(
    val foods: List<Food> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val searchResults: List<Food> = emptyList(),
    val searchQuery: String = "",
    val selectedFood: Food? = null,
    val featureFlags: UserPreferences.FeatureFlags? = null,
    val canAddMoreEntries: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
) : UiState

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

/**
 * Data class for selected food items with quantity
 */
data class SelectedFoodItem(
    val food: Food,
    val quantity: Double = 1.0
) {
    val totalCalories: Int
        get() = (food.caloriesPerServing * quantity).toInt()
    
    val totalProtein: Double
        get() = food.protein * quantity
    
    val totalCarbs: Double
        get() = food.carbohydrates * quantity
    
    val totalFat: Double
        get() = food.fat * quantity
    
    val totalFiber: Double?
        get() = food.fiber?.let { it * quantity }
    
    val totalSugar: Double?
        get() = food.sugar?.let { it * quantity }
    
    val totalSodium: Double?
        get() = food.sodium?.let { it * quantity }
}

/**
 * Calculated macronutrient totals
 */
data class CalculatedMacros(
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalSugar: Double = 0.0,
    val totalSodium: Double = 0.0
)
