package com.mydashboardapp.nutrition.ui

import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.data.UserPreferences
import com.mydashboardapp.core.data.UserPreferencesRepository
import com.mydashboardapp.core.ui.BaseViewModel
import com.mydashboardapp.core.ui.StandardUiState
import com.mydashboardapp.data.entities.Food
import com.mydashboardapp.data.entities.Meal
import com.mydashboardapp.data.repository.NutritionRepository
import com.mydashboardapp.data.importer.USDAFoodImporter
import com.mydashboardapp.export.NutritionExporter
import com.mydashboardapp.export.ExportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Use the NutritionUiState from NutritionModels.kt

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val usdaFoodImporter: USDAFoodImporter,
    private val nutritionExporter: NutritionExporter
) : BaseViewModel<NutritionUiState>(
    initialState = NutritionUiState()
) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadInitialData()
        observeFeatureFlags()
    }

    private fun loadInitialData() {
        launchWithErrorHandling {
            setLoading(true)
            
            // Combine foods and meals flows
            combine(
                nutritionRepository.getAllFoods(),
                nutritionRepository.getAllMeals()
            ) { foods, meals ->
                updateState { currentState ->
                    currentState.copy(
                        foods = foods,
                        meals = meals,
                        isLoading = false,
                        // isEmpty calculated automatically by the UI state
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun observeFeatureFlags() {
        userPreferencesRepository.featureFlags
            .onEach { flags ->
                updateState { currentState ->
                    val entryCount = currentState.meals.size
                    val canAdd = if (flags.maxNutritionEntries == -1) {
                        true // Unlimited for pro users
                    } else {
                        entryCount < flags.maxNutritionEntries
                    }
                    
                    currentState.copy(
                        featureFlags = flags,
                        canAddMoreEntries = canAdd
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Search for foods by query
     */
    fun searchFoods(query: String) {
        _searchQuery.value = query
        
        launchWithErrorHandling {
            updateState { it.copy(searchQuery = query, isLoading = true) }
            
            val results = if (query.isBlank()) {
                emptyList()
            } else {
                nutritionRepository.searchFoods(query)
            }
            
            updateState { currentState ->
                currentState.copy(
                    searchResults = results,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Select a food item
     */
    fun selectFood(food: Food) {
        updateState { it.copy(selectedFood = food) }
    }

    /**
     * Clear food selection
     */
    fun clearFoodSelection() {
        updateState { it.copy(selectedFood = null) }
    }

    /**
     * Add a new food entry
     */
    fun addFood(food: Food) {
        if (!currentState.canAddMoreEntries) {
            updateState { 
                it.copy(errorMessage = "You've reached the maximum number of nutrition entries for the free version. Upgrade to Pro for unlimited entries.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            nutritionRepository.insertFood(food)
            setLoading(false)
        }
    }

    /**
     * Add a new meal entry
     */
    fun addMeal(meal: Meal) {
        if (!currentState.canAddMoreEntries) {
            updateState { 
                it.copy(errorMessage = "You've reached the maximum number of nutrition entries for the free version. Upgrade to Pro for unlimited entries.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            nutritionRepository.insertMeal(meal)
            setLoading(false)
        }
    }

    /**
     * Update an existing food
     */
    fun updateFood(food: Food) {
        launchWithErrorHandling {
            setLoading(true)
            nutritionRepository.updateFood(food)
            setLoading(false)
        }
    }

    /**
     * Update an existing meal
     */
    fun updateMeal(meal: Meal) {
        launchWithErrorHandling {
            setLoading(true)
            nutritionRepository.updateMeal(meal)
            setLoading(false)
        }
    }

    /**
     * Delete a food item
     */
    fun deleteFood(food: Food) {
        launchWithErrorHandling {
            setLoading(true)
            nutritionRepository.deleteFood(food)
            setLoading(false)
        }
    }

    /**
     * Delete a meal
     */
    fun deleteMeal(meal: Meal) {
        launchWithErrorHandling {
            setLoading(true)
            nutritionRepository.deleteMeal(meal)
            setLoading(false)
        }
    }

    /**
     * Get nutrition summary for date range
     */
    fun getNutritionSummary(startDate: Long, endDate: Long) {
        launchWithErrorHandling {
            setLoading(true)
            val summary = nutritionRepository.getNutritionSummary(startDate, endDate)
            // Handle the summary result as needed
            setLoading(false)
        }
    }

    /**
     * Search foods by barcode (Pro feature)
     */
    fun searchByBarcode(barcode: String) {
        val flags = currentState.featureFlags
        if (flags?.advancedAnalyticsEnabled != true) {
            updateState { 
                it.copy(errorMessage = "Barcode scanning is a Pro feature. Upgrade to access advanced nutrition tracking.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            val food = nutritionRepository.getFoodByBarcode(barcode)
            food?.let { selectFood(it) }
            setLoading(false)
        }
    }

    /**
     * Sync nutrition data (Pro feature)
     */
    fun syncData() {
        val flags = currentState.featureFlags
        if (flags?.cloudSyncEnabled != true) {
            updateState { 
                it.copy(errorMessage = "Cloud sync is a Pro feature. Upgrade to sync your data across devices.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            // Implement sync logic here
            setLoading(false)
        }
    }

    /**
     * Import USDA foods from CSV
     */
    fun importUSDAFoods(inputStream: InputStream, onResult: (USDAFoodImporter.ImportResult) -> Unit) {
        launchWithErrorHandling {
            setLoading(true)
            try {
                val result = usdaFoodImporter.importFoodsFromCsv(inputStream)
                onResult(result)
                if (result.isSuccess) {
                    updateState { 
                        it.copy(errorMessage = "Successfully imported ${result.successCount} foods from USDA database.")
                    }
                } else {
                    updateState { 
                        it.copy(errorMessage = "Import completed with ${result.errorCount} errors. Imported ${result.successCount} foods.")
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(errorMessage = "Failed to import USDA foods: ${e.message}")
                }
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Import USDA ingredients from CSV
     */
    fun importUSDAIngredients(inputStream: InputStream, onResult: (USDAFoodImporter.ImportResult) -> Unit) {
        launchWithErrorHandling {
            setLoading(true)
            try {
                val result = usdaFoodImporter.importIngredientsFromCsv(inputStream)
                onResult(result)
                if (result.isSuccess) {
                    updateState { 
                        it.copy(errorMessage = "Successfully imported ${result.successCount} ingredients from USDA database.")
                    }
                } else {
                    updateState { 
                        it.copy(errorMessage = "Import completed with ${result.errorCount} errors. Imported ${result.successCount} ingredients.")
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(errorMessage = "Failed to import USDA ingredients: ${e.message}")
                }
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Export meals to CSV
     */
    fun exportMealsToCSV(outputFile: File, startDate: Long? = null, endDate: Long? = null, onResult: (ExportResult) -> Unit) {
        val flags = currentState.featureFlags
        if (flags?.advancedAnalyticsEnabled != true) {
            updateState { 
                it.copy(errorMessage = "Data export is a Pro feature. Upgrade to export your nutrition data.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            try {
                val result = nutritionExporter.exportMealsToCSV(startDate, endDate, outputFile)
                onResult(result)
                when (result) {
                    is ExportResult.Success -> {
                        updateState { 
                            it.copy(errorMessage = "Successfully exported ${result.recordCount} meals to ${result.fileName}")
                        }
                    }
                    is ExportResult.Error -> {
                        updateState { 
                            it.copy(errorMessage = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(errorMessage = "Failed to export meals: ${e.message}")
                }
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Export detailed meals to CSV (with foods)
     */
    fun exportDetailedMealsToCSV(outputFile: File, startDate: Long? = null, endDate: Long? = null, onResult: (ExportResult) -> Unit) {
        val flags = currentState.featureFlags
        if (flags?.advancedAnalyticsEnabled != true) {
            updateState { 
                it.copy(errorMessage = "Data export is a Pro feature. Upgrade to export your nutrition data.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            try {
                val result = nutritionExporter.exportDetailedMealsToCSV(startDate, endDate, outputFile)
                onResult(result)
                when (result) {
                    is ExportResult.Success -> {
                        updateState { 
                            it.copy(errorMessage = "Successfully exported detailed meal data to ${result.fileName}")
                        }
                    }
                    is ExportResult.Error -> {
                        updateState { 
                            it.copy(errorMessage = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(errorMessage = "Failed to export detailed meals: ${e.message}")
                }
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Export foods database to CSV
     */
    fun exportFoodsToCSV(outputFile: File, onResult: (ExportResult) -> Unit) {
        val flags = currentState.featureFlags
        if (flags?.advancedAnalyticsEnabled != true) {
            updateState { 
                it.copy(errorMessage = "Data export is a Pro feature. Upgrade to export your nutrition data.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            try {
                val result = nutritionExporter.exportFoodsToCSV(outputFile)
                onResult(result)
                when (result) {
                    is ExportResult.Success -> {
                        updateState { 
                            it.copy(errorMessage = "Successfully exported ${result.recordCount} foods to ${result.fileName}")
                        }
                    }
                    is ExportResult.Error -> {
                        updateState { 
                            it.copy(errorMessage = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(errorMessage = "Failed to export foods: ${e.message}")
                }
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Export nutrition data to PDF
     */
    fun exportNutritionToPDF(outputFile: File, startDate: Long? = null, endDate: Long? = null, onResult: (ExportResult) -> Unit) {
        val flags = currentState.featureFlags
        if (flags?.advancedAnalyticsEnabled != true) {
            updateState { 
                it.copy(errorMessage = "PDF export is a Pro feature. Upgrade to export your nutrition data to PDF.") 
            }
            return
        }

        launchWithErrorHandling {
            setLoading(true)
            try {
                val result = nutritionExporter.exportNutritionToPDF(startDate, endDate, outputFile)
                onResult(result)
                when (result) {
                    is ExportResult.Success -> {
                        updateState { 
                            it.copy(errorMessage = "Successfully exported nutrition report to ${result.fileName}")
                        }
                    }
                    is ExportResult.Error -> {
                        updateState { 
                            it.copy(errorMessage = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    it.copy(errorMessage = "Failed to export PDF: ${e.message}")
                }
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        updateState { it.copy(errorMessage = null) }
    }

    private fun setLoading(isLoading: Boolean) {
        updateState { it.copy(isLoading = isLoading) }
    }
}
