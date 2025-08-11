package com.mydashboardapp.data.importer

import com.mydashboardapp.data.entities.Food
import com.mydashboardapp.data.entities.Ingredient
import com.mydashboardapp.data.dao.NutritionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Imports USDA food database from CSV files into Room database
 * 
 * Expected CSV format:
 * fdc_id,description,food_category,energy_kcal,protein,carbs,total_fat,fiber,sugar,sodium
 */
@Singleton
class USDAFoodImporter @Inject constructor(
    private val nutritionDao: NutritionDao
) {
    
    /**
     * Import foods from USDA CSV file
     */
    suspend fun importFoodsFromCsv(inputStream: InputStream): ImportResult = withContext(Dispatchers.IO) {
        val reader = inputStream.bufferedReader()
        val importResult = ImportResult()
        
        try {
            // Skip header line
            reader.readLine()
            
            reader.use { bufferedReader ->
                bufferedReader.forEachLine { line ->
                    try {
                        val food = parseCsvLineToFood(line)
                        if (food != null) {
                            nutritionDao.insertFood(food)
                            importResult.successCount++
                        } else {
                            importResult.errorCount++
                        }
                    } catch (e: Exception) {
                        importResult.errors.add("Error parsing line '$line': ${e.message}")
                        importResult.errorCount++
                    }
                }
            }
        } catch (e: Exception) {
            importResult.errors.add("Error reading CSV file: ${e.message}")
            importResult.errorCount++
        }
        
        importResult
    }
    
    /**
     * Import ingredients from USDA CSV file (per 100g basis)
     */
    suspend fun importIngredientsFromCsv(inputStream: InputStream): ImportResult = withContext(Dispatchers.IO) {
        val reader = inputStream.bufferedReader()
        val importResult = ImportResult()
        
        try {
            // Skip header line
            reader.readLine()
            
            reader.use { bufferedReader ->
                bufferedReader.forEachLine { line ->
                    try {
                        val ingredient = parseCsvLineToIngredient(line)
                        if (ingredient != null) {
                            nutritionDao.insertIngredient(ingredient)
                            importResult.successCount++
                        } else {
                            importResult.errorCount++
                        }
                    } catch (e: Exception) {
                        importResult.errors.add("Error parsing line '$line': ${e.message}")
                        importResult.errorCount++
                    }
                }
            }
        } catch (e: Exception) {
            importResult.errors.add("Error reading CSV file: ${e.message}")
            importResult.errorCount++
        }
        
        importResult
    }
    
    /**
     * Parse CSV line to Food entity
     * Expected format: fdc_id,description,food_category,energy_kcal,protein,carbs,total_fat,fiber,sugar,sodium,serving_size
     */
    private fun parseCsvLineToFood(line: String): Food? {
        val columns = line.split(",").map { it.trim().removeSurrounding("\"") }
        
        if (columns.size < 10) return null
        
        return try {
            val servingSize = if (columns.size > 10 && columns[10].isNotBlank()) {
                columns[10].toDoubleOrNull() ?: 100.0
            } else {
                100.0 // Default serving size
            }
            
            Food(
                name = columns[1].ifBlank { "Unknown Food" },
                brand = "USDA",
                servingSize = servingSize,
                caloriesPerServing = columns[3].toDoubleOrNull()?.toInt() ?: 0,
                proteinPerServing = columns[4].toDoubleOrNull() ?: 0.0,
                carbsPerServing = columns[5].toDoubleOrNull() ?: 0.0,
                fatPerServing = columns[6].toDoubleOrNull() ?: 0.0,
                fiberPerServing = columns[7].toDoubleOrNull(),
                sugarPerServing = columns[8].toDoubleOrNull(),
                sodiumPerServing = columns[9].toDoubleOrNull(),
                barcode = null // USDA foods don't have barcodes
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse CSV line to Ingredient entity (per gram basis)
     * Expected format: fdc_id,description,food_category,energy_kcal,protein,carbs,total_fat,fiber,sugar,sodium
     */
    private fun parseCsvLineToIngredient(line: String): Ingredient? {
        val columns = line.split(",").map { it.trim().removeSurrounding("\"") }
        
        if (columns.size < 10) return null
        
        return try {
            Ingredient(
                name = columns[1].ifBlank { "Unknown Ingredient" },
                category = columns[2].ifBlank { null },
                caloriesPerGram = (columns[3].toDoubleOrNull() ?: 0.0) / 100.0, // Convert per 100g to per 1g
                proteinPerGram = (columns[4].toDoubleOrNull() ?: 0.0) / 100.0,
                carbsPerGram = (columns[5].toDoubleOrNull() ?: 0.0) / 100.0,
                fatPerGram = (columns[6].toDoubleOrNull() ?: 0.0) / 100.0,
                fiberPerGram = columns[7].toDoubleOrNull()?.let { it / 100.0 },
                sugarPerGram = columns[8].toDoubleOrNull()?.let { it / 100.0 },
                sodiumPerGram = columns[9].toDoubleOrNull()?.let { it / 100.0 } // Convert mg per 100g to mg per 1g
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Import sample foods from JSON file for testing/prepopulation
     */
    suspend fun importSampleFoods(foods: List<SampleFood>): ImportResult = withContext(Dispatchers.IO) {
        val importResult = ImportResult()
        
        foods.forEach { sampleFood ->
            try {
                val food = Food(
                    name = sampleFood.name,
                    brand = sampleFood.brand,
                    servingSize = sampleFood.servingSize,
                    caloriesPerServing = sampleFood.caloriesPerServing,
                    proteinPerServing = sampleFood.proteinPerServing,
                    carbsPerServing = sampleFood.carbsPerServing,
                    fatPerServing = sampleFood.fatPerServing,
                    fiberPerServing = sampleFood.fiberPerServing,
                    sugarPerServing = sampleFood.sugarPerServing,
                    sodiumPerServing = sampleFood.sodiumPerServing
                )
                
                nutritionDao.insertFood(food)
                importResult.successCount++
            } catch (e: Exception) {
                importResult.errors.add("Error importing ${sampleFood.name}: ${e.message}")
                importResult.errorCount++
            }
        }
        
        importResult
    }
    
    data class ImportResult(
        var successCount: Int = 0,
        var errorCount: Int = 0,
        val errors: MutableList<String> = mutableListOf()
    ) {
        val totalProcessed: Int get() = successCount + errorCount
        val isSuccess: Boolean get() = errorCount == 0 && successCount > 0
    }
    
    data class SampleFood(
        val name: String,
        val brand: String?,
        val servingSize: Double,
        val caloriesPerServing: Int,
        val proteinPerServing: Double,
        val carbsPerServing: Double,
        val fatPerServing: Double,
        val fiberPerServing: Double?,
        val sugarPerServing: Double?,
        val sodiumPerServing: Double?
    )
}
