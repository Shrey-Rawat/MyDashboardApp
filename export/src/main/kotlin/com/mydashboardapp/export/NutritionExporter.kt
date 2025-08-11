package com.mydashboardapp.export

import android.content.Context
import com.mydashboardapp.data.entities.Food
import com.mydashboardapp.data.entities.Meal
import com.mydashboardapp.data.entities.Ingredient
import com.mydashboardapp.data.repository.NutritionRepository
import com.mydashboardapp.data.dao.NutritionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for exporting nutrition data to CSV and PDF formats
 */
@Singleton
class NutritionExporter @Inject constructor(
    private val context: Context,
    private val nutritionRepository: NutritionRepository
) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val shortDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Export meals data to CSV format
     */
    suspend fun exportMealsToCSV(
        startDate: Long? = null,
        endDate: Long? = null,
        outputFile: File
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val meals = if (startDate != null && endDate != null) {
                nutritionRepository.getMealsByDateRange(startDate, endDate)
            } else {
                nutritionRepository.getAllMeals().value ?: emptyList()
            }

            val csvContent = buildString {
                // CSV Header
                appendLine("Date,Meal Type,Meal Name,Total Calories,Total Protein (g),Total Carbs (g),Total Fat (g),Total Fiber (g),Total Sugar (g),Total Sodium (mg),Notes")
                
                // CSV Data
                meals.forEach { meal ->
                    val dateStr = dateFormatter.format(Date(meal.dateConsumed))
                    appendLine(
                        "${csvEscape(dateStr)}," +
                        "${csvEscape(meal.mealType)}," +
                        "${csvEscape(meal.name)}," +
                        "${meal.totalCalories}," +
                        "${meal.totalProtein}," +
                        "${meal.totalCarbs}," +
                        "${meal.totalFat}," +
                        "${meal.totalFiber ?: ""}," +
                        "${meal.totalSugar ?: ""}," +
                        "${meal.totalSodium ?: ""}," +
                        "${csvEscape(meal.notes ?: "")}"
                    )
                }
            }

            outputFile.writeText(csvContent)
            
            ExportResult.Success(
                fileName = outputFile.name,
                filePath = outputFile.absolutePath,
                recordCount = meals.size
            )
        } catch (e: Exception) {
            ExportResult.Error("Failed to export meals to CSV: ${e.message}")
        }
    }

    /**
     * Export detailed meal data with foods to CSV
     */
    suspend fun exportDetailedMealsToCSV(
        startDate: Long? = null,
        endDate: Long? = null,
        outputFile: File
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val meals = if (startDate != null && endDate != null) {
                nutritionRepository.getMealsByDateRange(startDate, endDate)
            } else {
                nutritionRepository.getAllMeals().value ?: emptyList()
            }

            val csvContent = buildString {
                // CSV Header
                appendLine("Date,Meal Type,Meal Name,Food Name,Brand,Servings,Calories,Protein (g),Carbs (g),Fat (g),Fiber (g),Sugar (g),Sodium (mg)")
                
                // CSV Data
                meals.forEach { meal ->
                    val dateStr = dateFormatter.format(Date(meal.dateConsumed))
                    val mealFoods = nutritionRepository.getFoodsForMeal(meal.id)
                    
                    if (mealFoods.isEmpty()) {
                        // Meal with no foods
                        appendLine(
                            "${csvEscape(dateStr)}," +
                            "${csvEscape(meal.mealType)}," +
                            "${csvEscape(meal.name)}," +
                            "No foods recorded,,,,,,,,"
                        )
                    } else {
                        mealFoods.forEach { mealFood ->
                            val food = nutritionRepository.getFoodById(mealFood.foodId)
                            if (food != null) {
                                val totalCalories = (food.caloriesPerServing * mealFood.quantity).toInt()
                                val totalProtein = food.proteinPerServing * mealFood.quantity
                                val totalCarbs = food.carbsPerServing * mealFood.quantity
                                val totalFat = food.fatPerServing * mealFood.quantity
                                val totalFiber = food.fiberPerServing?.let { it * mealFood.quantity }
                                val totalSugar = food.sugarPerServing?.let { it * mealFood.quantity }
                                val totalSodium = food.sodiumPerServing?.let { it * mealFood.quantity }
                                
                                appendLine(
                                    "${csvEscape(dateStr)}," +
                                    "${csvEscape(meal.mealType)}," +
                                    "${csvEscape(meal.name)}," +
                                    "${csvEscape(food.name)}," +
                                    "${csvEscape(food.brand ?: "")}," +
                                    "${mealFood.quantity}," +
                                    "$totalCalories," +
                                    "$totalProtein," +
                                    "$totalCarbs," +
                                    "$totalFat," +
                                    "${totalFiber ?: ""}," +
                                    "${totalSugar ?: ""}," +
                                    "${totalSodium ?: ""}"
                                )
                            }
                        }
                    }
                }
            }

            outputFile.writeText(csvContent)
            
            ExportResult.Success(
                fileName = outputFile.name,
                filePath = outputFile.absolutePath,
                recordCount = meals.size
            )
        } catch (e: Exception) {
            ExportResult.Error("Failed to export detailed meals to CSV: ${e.message}")
        }
    }

    /**
     * Export foods database to CSV
     */
    suspend fun exportFoodsToCSV(outputFile: File): ExportResult = withContext(Dispatchers.IO) {
        try {
            val foods = nutritionRepository.getAllFoods().value ?: emptyList()

            val csvContent = buildString {
                // CSV Header
                appendLine("Name,Brand,Serving Size (g),Calories per Serving,Protein (g),Carbs (g),Fat (g),Fiber (g),Sugar (g),Sodium (mg),Barcode,Created At")
                
                // CSV Data
                foods.forEach { food ->
                    val createdDate = dateFormatter.format(Date(food.createdAt))
                    appendLine(
                        "${csvEscape(food.name)}," +
                        "${csvEscape(food.brand ?: "")}," +
                        "${food.servingSize}," +
                        "${food.caloriesPerServing}," +
                        "${food.proteinPerServing}," +
                        "${food.carbsPerServing}," +
                        "${food.fatPerServing}," +
                        "${food.fiberPerServing ?: ""}," +
                        "${food.sugarPerServing ?: ""}," +
                        "${food.sodiumPerServing ?: ""}," +
                        "${csvEscape(food.barcode ?: "")}," +
                        "${csvEscape(createdDate)}"
                    )
                }
            }

            outputFile.writeText(csvContent)
            
            ExportResult.Success(
                fileName = outputFile.name,
                filePath = outputFile.absolutePath,
                recordCount = foods.size
            )
        } catch (e: Exception) {
            ExportResult.Error("Failed to export foods to CSV: ${e.message}")
        }
    }

    /**
     * Export ingredients database to CSV
     */
    suspend fun exportIngredientsToCSV(outputFile: File): ExportResult = withContext(Dispatchers.IO) {
        try {
            val ingredients = nutritionRepository.getAllIngredients().value ?: emptyList()

            val csvContent = buildString {
                // CSV Header
                appendLine("Name,Category,Calories per Gram,Protein per Gram,Carbs per Gram,Fat per Gram,Fiber per Gram,Sugar per Gram,Sodium per Gram,Is Allergen,Allergen Info,Created At")
                
                // CSV Data
                ingredients.forEach { ingredient ->
                    val createdDate = dateFormatter.format(Date(ingredient.createdAt))
                    appendLine(
                        "${csvEscape(ingredient.name)}," +
                        "${csvEscape(ingredient.category ?: "")}," +
                        "${ingredient.caloriesPerGram}," +
                        "${ingredient.proteinPerGram}," +
                        "${ingredient.carbsPerGram}," +
                        "${ingredient.fatPerGram}," +
                        "${ingredient.fiberPerGram ?: ""}," +
                        "${ingredient.sugarPerGram ?: ""}," +
                        "${ingredient.sodiumPerGram ?: ""}," +
                        "${ingredient.isAllergen}," +
                        "${csvEscape(ingredient.allergenInfo ?: "")}," +
                        "${csvEscape(createdDate)}"
                    )
                }
            }

            outputFile.writeText(csvContent)
            
            ExportResult.Success(
                fileName = outputFile.name,
                filePath = outputFile.absolutePath,
                recordCount = ingredients.size
            )
        } catch (e: Exception) {
            ExportResult.Error("Failed to export ingredients to CSV: ${e.message}")
        }
    }

    /**
     * Export nutrition summary report to CSV
     */
    suspend fun exportNutritionSummaryToCSV(
        startDate: Long,
        endDate: Long,
        outputFile: File
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val summary = nutritionRepository.getNutritionSummary(startDate, endDate)
            val meals = nutritionRepository.getMealsByDateRange(startDate, endDate)
            
            // Group meals by date
            val mealsByDate = meals.groupBy { 
                shortDateFormatter.format(Date(it.dateConsumed))
            }

            val csvContent = buildString {
                // Summary Header
                appendLine("Nutrition Summary Report")
                appendLine("Period: ${shortDateFormatter.format(Date(startDate))} to ${shortDateFormatter.format(Date(endDate))}")
                appendLine("")
                
                // Overall Summary
                appendLine("Overall Summary")
                appendLine("Total Calories,Total Protein (g),Total Carbs (g),Total Fat (g)")
                appendLine("${summary.totalCalories},${summary.totalProtein},${summary.totalCarbs},${summary.totalFat}")
                appendLine("")
                
                // Daily Breakdown Header
                appendLine("Daily Breakdown")
                appendLine("Date,Meals Count,Total Calories,Total Protein (g),Total Carbs (g),Total Fat (g),Avg Calories per Meal")
                
                // Daily Data
                mealsByDate.forEach { (date, dayMeals) ->
                    val dayCalories = dayMeals.sumOf { it.totalCalories }
                    val dayProtein = dayMeals.sumOf { it.totalProtein }
                    val dayCarbs = dayMeals.sumOf { it.totalCarbs }
                    val dayFat = dayMeals.sumOf { it.totalFat }
                    val avgCalories = if (dayMeals.isNotEmpty()) dayCalories / dayMeals.size else 0
                    
                    appendLine("$date,${dayMeals.size},$dayCalories,$dayProtein,$dayCarbs,$dayFat,$avgCalories")
                }
            }

            outputFile.writeText(csvContent)
            
            ExportResult.Success(
                fileName = outputFile.name,
                filePath = outputFile.absolutePath,
                recordCount = mealsByDate.size
            )
        } catch (e: Exception) {
            ExportResult.Error("Failed to export nutrition summary to CSV: ${e.message}")
        }
    }

    /**
     * Export nutrition data to PDF format (basic implementation)
     */
    suspend fun exportNutritionToPDF(
        startDate: Long? = null,
        endDate: Long? = null,
        outputFile: File
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // For now, create a simple text-based PDF content
            // In a real implementation, you would use a PDF library like iText
            
            val meals = if (startDate != null && endDate != null) {
                nutritionRepository.getMealsByDateRange(startDate, endDate)
            } else {
                nutritionRepository.getAllMeals().value ?: emptyList()
            }

            val summary = if (startDate != null && endDate != null) {
                nutritionRepository.getNutritionSummary(startDate, endDate)
            } else {
                null
            }

            val pdfContent = buildString {
                appendLine("NUTRITION REPORT")
                appendLine("================")
                appendLine("")
                
                if (startDate != null && endDate != null) {
                    appendLine("Period: ${shortDateFormatter.format(Date(startDate))} to ${shortDateFormatter.format(Date(endDate))}")
                    appendLine("")
                    
                    summary?.let {
                        appendLine("SUMMARY")
                        appendLine("-------")
                        appendLine("Total Calories: ${it.totalCalories}")
                        appendLine("Total Protein: ${it.totalProtein}g")
                        appendLine("Total Carbs: ${it.totalCarbs}g")
                        appendLine("Total Fat: ${it.totalFat}g")
                        appendLine("")
                    }
                }
                
                appendLine("MEALS")
                appendLine("-----")
                meals.forEach { meal ->
                    val dateStr = dateFormatter.format(Date(meal.dateConsumed))
                    appendLine("Date: $dateStr")
                    appendLine("Type: ${meal.mealType}")
                    appendLine("Name: ${meal.name}")
                    appendLine("Calories: ${meal.totalCalories}")
                    appendLine("Protein: ${meal.totalProtein}g")
                    appendLine("Carbs: ${meal.totalCarbs}g")
                    appendLine("Fat: ${meal.totalFat}g")
                    if (meal.notes?.isNotEmpty() == true) {
                        appendLine("Notes: ${meal.notes}")
                    }
                    appendLine("")
                }
            }

            // Write as text file with .pdf extension
            // In production, this should use a proper PDF library
            outputFile.writeText(pdfContent)
            
            ExportResult.Success(
                fileName = outputFile.name,
                filePath = outputFile.absolutePath,
                recordCount = meals.size
            )
        } catch (e: Exception) {
            ExportResult.Error("Failed to export nutrition to PDF: ${e.message}")
        }
    }

    /**
     * Generate a nutrition report with statistics
     */
    suspend fun generateNutritionReport(
        startDate: Long,
        endDate: Long
    ): NutritionReport = withContext(Dispatchers.IO) {
        try {
            val meals = nutritionRepository.getMealsByDateRange(startDate, endDate)
            val summary = nutritionRepository.getNutritionSummary(startDate, endDate)
            
            val totalDays = ((endDate - startDate) / (24 * 60 * 60 * 1000)).toInt() + 1
            val avgCaloriesPerDay = if (totalDays > 0) summary.totalCalories / totalDays else 0
            val avgMealsPerDay = if (totalDays > 0) meals.size.toDouble() / totalDays else 0.0
            
            // Group by meal type
            val mealTypeBreakdown = meals.groupBy { it.mealType }
                .mapValues { (_, typeMeals) ->
                    MealTypeStats(
                        count = typeMeals.size,
                        totalCalories = typeMeals.sumOf { it.totalCalories },
                        avgCalories = if (typeMeals.isNotEmpty()) typeMeals.sumOf { it.totalCalories } / typeMeals.size else 0
                    )
                }
            
            NutritionReport(
                startDate = startDate,
                endDate = endDate,
                totalMeals = meals.size,
                totalDays = totalDays,
                summary = summary,
                avgCaloriesPerDay = avgCaloriesPerDay,
                avgMealsPerDay = avgMealsPerDay,
                mealTypeBreakdown = mealTypeBreakdown
            )
        } catch (e: Exception) {
            throw Exception("Failed to generate nutrition report: ${e.message}")
        }
    }

    /**
     * Helper function to escape CSV values
     */
    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\"" // Escape quotes by doubling them
        } else {
            value
        }
    }
}

/**
 * Result of an export operation
 */
sealed class ExportResult {
    data class Success(
        val fileName: String,
        val filePath: String,
        val recordCount: Int
    ) : ExportResult()
    
    data class Error(
        val message: String
    ) : ExportResult()
}

/**
 * Comprehensive nutrition report
 */
data class NutritionReport(
    val startDate: Long,
    val endDate: Long,
    val totalMeals: Int,
    val totalDays: Int,
    val summary: NutritionDao.NutritionSummary,
    val avgCaloriesPerDay: Int,
    val avgMealsPerDay: Double,
    val mealTypeBreakdown: Map<String, MealTypeStats>
)

/**
 * Statistics for a specific meal type
 */
data class MealTypeStats(
    val count: Int,
    val totalCalories: Int,
    val avgCalories: Int
)
