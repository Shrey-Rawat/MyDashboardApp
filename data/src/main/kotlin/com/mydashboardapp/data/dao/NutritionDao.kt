package com.mydashboardapp.data.dao

import androidx.room.*
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    
    // Food operations
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<Food>>
    
    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Long): Food?
    
    @Query("SELECT * FROM foods WHERE barcode = :barcode")
    suspend fun getFoodByBarcode(barcode: String): Food?
    
    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    suspend fun searchFoods(query: String): List<Food>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food): Long
    
    @Update
    suspend fun updateFood(food: Food)
    
    @Delete
    suspend fun deleteFood(food: Food)
    
    // Ingredient operations
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): Ingredient?
    
    @Query("SELECT * FROM ingredients WHERE category = :category")
    suspend fun getIngredientsByCategory(category: String): List<Ingredient>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long
    
    @Update
    suspend fun updateIngredient(ingredient: Ingredient)
    
    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)
    
    // Meal operations
    @Query("SELECT * FROM meals ORDER BY dateConsumed DESC")
    fun getAllMeals(): Flow<List<Meal>>
    
    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: Long): Meal?
    
    @Query("SELECT * FROM meals WHERE dateConsumed BETWEEN :startDate AND :endDate ORDER BY dateConsumed DESC")
    suspend fun getMealsByDateRange(startDate: Long, endDate: Long): List<Meal>
    
    @Query("SELECT * FROM meals WHERE mealType = :mealType ORDER BY dateConsumed DESC")
    suspend fun getMealsByType(mealType: String): List<Meal>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long
    
    @Update
    suspend fun updateMeal(meal: Meal)
    
    @Delete
    suspend fun deleteMeal(meal: Meal)
    
    // MealFoodCrossRef operations
    @Query("SELECT * FROM meal_food_cross_ref WHERE mealId = :mealId")
    suspend fun getFoodsForMeal(mealId: Long): List<MealFoodCrossRef>
    
    @Query("SELECT f.* FROM foods f INNER JOIN meal_food_cross_ref mf ON f.id = mf.foodId WHERE mf.mealId = :mealId")
    suspend fun getFoodsByMealId(mealId: Long): List<Food>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealFoodCrossRef(mealFoodCrossRef: MealFoodCrossRef)
    
    @Delete
    suspend fun deleteMealFoodCrossRef(mealFoodCrossRef: MealFoodCrossRef)
    
    @Query("DELETE FROM meal_food_cross_ref WHERE mealId = :mealId")
    suspend fun deleteAllFoodsForMeal(mealId: Long)
    
    // Analytics queries
    @Query("""
        SELECT SUM(totalCalories) as totalCalories, 
               SUM(totalProtein) as totalProtein,
               SUM(totalCarbs) as totalCarbs, 
               SUM(totalFat) as totalFat
        FROM meals 
        WHERE dateConsumed BETWEEN :startDate AND :endDate
    """)
    suspend fun getNutritionSummary(startDate: Long, endDate: Long): NutritionSummary
    
    data class NutritionSummary(
        val totalCalories: Int,
        val totalProtein: Double,
        val totalCarbs: Double,
        val totalFat: Double
    )
}
