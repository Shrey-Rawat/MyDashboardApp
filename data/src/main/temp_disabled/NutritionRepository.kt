package com.mydashboardapp.data.repository

import com.mydashboardapp.core.data.BaseRepository
import com.mydashboardapp.core.data.BaseRepositoryImpl
import com.mydashboardapp.core.data.BaseRemoteDataSource
import com.mydashboardapp.core.data.RemoteDataSource
import com.mydashboardapp.data.entities.*
import com.mydashboardapp.data.dao.NutritionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface NutritionRepository : BaseRepository<Food, Long> {
    // Food operations
    fun getAllFoods(): Flow<List<Food>>
    suspend fun getFoodById(id: Long): Food?
    suspend fun getFoodByBarcode(barcode: String): Food?
    suspend fun searchFoods(query: String): List<Food>
    suspend fun insertFood(food: Food): Long
    suspend fun updateFood(food: Food)
    suspend fun deleteFood(food: Food)
    
    // Ingredient operations
    fun getAllIngredients(): Flow<List<Ingredient>>
    suspend fun getIngredientById(id: Long): Ingredient?
    suspend fun getIngredientsByCategory(category: String): List<Ingredient>
    suspend fun insertIngredient(ingredient: Ingredient): Long
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)
    
    // Meal operations
    fun getAllMeals(): Flow<List<Meal>>
    suspend fun getMealById(id: Long): Meal?
    suspend fun getMealsByDateRange(startDate: Long, endDate: Long): List<Meal>
    suspend fun getMealsByType(mealType: String): List<Meal>
    suspend fun insertMeal(meal: Meal): Long
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMeal(meal: Meal)
    
    // MealFood associations
    suspend fun getFoodsForMeal(mealId: Long): List<MealFoodCrossRef>
    suspend fun getFoodsByMealId(mealId: Long): List<Food>
    suspend fun addFoodToMeal(mealFoodCrossRef: MealFoodCrossRef)
    suspend fun removeFoodFromMeal(mealFoodCrossRef: MealFoodCrossRef)
    suspend fun removeAllFoodsFromMeal(mealId: Long)
    
    // Analytics
    suspend fun getNutritionSummary(startDate: Long, endDate: Long): NutritionDao.NutritionSummary
}

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val nutritionDao: NutritionDao,
    private val nutritionRemoteDataSource: NutritionRemoteDataSource? = null
) : BaseRepository() {
    localDataSource = nutritionDao,
    remoteDataSource = nutritionRemoteDataSource
), NutritionRepository {
    
    override fun getAllFoods(): Flow<List<Food>> = nutritionDao.getAllFoods()
    
    override suspend fun getFoodById(id: Long): Food? = nutritionDao.getFoodById(id)
    
    override suspend fun getFoodByBarcode(barcode: String): Food? = nutritionDao.getFoodByBarcode(barcode)
    
    override suspend fun searchFoods(query: String): List<Food> = nutritionDao.searchFoods(query)
    
    override suspend fun insertFood(food: Food): Long = nutritionDao.insertFood(food)
    
    override suspend fun updateFood(food: Food) = nutritionDao.updateFood(food)
    
    override suspend fun deleteFood(food: Food) = nutritionDao.deleteFood(food)
    
    override fun getAllIngredients(): Flow<List<Ingredient>> = nutritionDao.getAllIngredients()
    
    override suspend fun getIngredientById(id: Long): Ingredient? = nutritionDao.getIngredientById(id)
    
    override suspend fun getIngredientsByCategory(category: String): List<Ingredient> = 
        nutritionDao.getIngredientsByCategory(category)
    
    override suspend fun insertIngredient(ingredient: Ingredient): Long = nutritionDao.insertIngredient(ingredient)
    
    override suspend fun updateIngredient(ingredient: Ingredient) = nutritionDao.updateIngredient(ingredient)
    
    override suspend fun deleteIngredient(ingredient: Ingredient) = nutritionDao.deleteIngredient(ingredient)
    
    override fun getAllMeals(): Flow<List<Meal>> = nutritionDao.getAllMeals()
    
    override suspend fun getMealById(id: Long): Meal? = nutritionDao.getMealById(id)
    
    override suspend fun getMealsByDateRange(startDate: Long, endDate: Long): List<Meal> = 
        nutritionDao.getMealsByDateRange(startDate, endDate)
    
    override suspend fun getMealsByType(mealType: String): List<Meal> = nutritionDao.getMealsByType(mealType)
    
    override suspend fun insertMeal(meal: Meal): Long = nutritionDao.insertMeal(meal)
    
    override suspend fun updateMeal(meal: Meal) = nutritionDao.updateMeal(meal)
    
    override suspend fun deleteMeal(meal: Meal) = nutritionDao.deleteMeal(meal)
    
    override suspend fun getFoodsForMeal(mealId: Long): List<MealFoodCrossRef> = 
        nutritionDao.getFoodsForMeal(mealId)
    
    override suspend fun getFoodsByMealId(mealId: Long): List<Food> = nutritionDao.getFoodsByMealId(mealId)
    
    override suspend fun addFoodToMeal(mealFoodCrossRef: MealFoodCrossRef) = 
        nutritionDao.insertMealFoodCrossRef(mealFoodCrossRef)
    
    override suspend fun removeFoodFromMeal(mealFoodCrossRef: MealFoodCrossRef) = 
        nutritionDao.deleteMealFoodCrossRef(mealFoodCrossRef)
    
    override suspend fun removeAllFoodsFromMeal(mealId: Long) = nutritionDao.deleteAllFoodsForMeal(mealId)
    
    override suspend fun getNutritionSummary(startDate: Long, endDate: Long): NutritionDao.NutritionSummary = 
        nutritionDao.getNutritionSummary(startDate, endDate)

    // BaseRepository implementation
    override fun getAll(): Flow<List<Food>> = getAllFoods()
    
    override suspend fun getById(id: Long): Food? = getFoodById(id)
    
    override suspend fun insert(entity: Food): Long = insertFood(entity)
    
    override suspend fun insertAll(entities: List<Food>): List<Long> = 
        entities.map { insertFood(it) }
    
    override suspend fun update(entity: Food) = updateFood(entity)
    
    override suspend fun updateAll(entities: List<Food>) = 
        entities.forEach { updateFood(it) }
    
    override suspend fun delete(entity: Food) = deleteFood(entity)
    
    override suspend fun deleteById(id: Long) {
        getFoodById(id)?.let { deleteFood(it) }
    }
    
    override suspend fun deleteAll(entities: List<Food>) = 
        entities.forEach { deleteFood(it) }

    // BaseRepositoryImpl abstract methods implementation
    override suspend fun handleRemoteCreated(items: List<Food>) {
        items.forEach { insertFood(it) }
    }
    
    override suspend fun handleRemoteUpdated(items: List<Food>) {
        items.forEach { updateFood(it) }
    }
    
    override suspend fun handleRemoteDeleted(deletedIds: List<Long>) {
        deletedIds.forEach { id ->
            getFoodById(id)?.let { deleteFood(it) }
        }
    }
}

/**
 * Remote data source interface for nutrition data
 */
interface NutritionRemoteDataSource : BaseRemoteDataSource<Food, Long> {
    suspend fun searchFoodsRemote(query: String): List<Food>
    suspend fun getFoodByBarcodeRemote(barcode: String): Food?
}

/**
 * Modern remote data source implementation for nutrition data using the new abstract class
 */
abstract class NutritionRemoteDataSourceImpl : RemoteDataSource<Food, Long>() {
    abstract suspend fun searchFoodsRemote(query: String): List<Food>
    abstract suspend fun getFoodByBarcodeRemote(barcode: String): Food?
    
    // The pushChanges() and pullUpdates() methods are inherited with no-op default implementations
    // Concrete implementations can override these for actual sync functionality
}
