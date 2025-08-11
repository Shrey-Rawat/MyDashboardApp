package com.mydashboardapp.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mydashboardapp.data.dao.*
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Provider

@Database(
    entities = [
        // Nutrition entities
        Food::class,
        Ingredient::class,
        Meal::class,
        MealFoodCrossRef::class,
        
        // Training entities
        Exercise::class,
        Workout::class,
        com.mydashboardapp.data.entities.Set::class,
        WorkoutExerciseCrossRef::class,
        
        // Productivity entities
        Task::class,
        Goal::class,
        TimeLog::class,
        PomodoroSession::class,
        Streak::class,
        StreakLog::class,
        
        // Finance entities
        Account::class,
        com.mydashboardapp.data.entities.Transaction::class,
        Investment::class,
        PriceSnapshot::class,
        BudgetEnvelope::class,
        BudgetPeriod::class,
        EnvelopeAllocation::class,
        EnvelopeTransaction::class,
        
        // Inventory entities
        Item::class,
        Location::class,
        StockMovement::class,
        AffiliateLink::class,
        
        // AI entities
        PromptHistory::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BestProductivityDatabase : RoomDatabase() {
    
    abstract fun nutritionDao(): NutritionDao
    abstract fun trainingDao(): TrainingDao
    abstract fun productivityDao(): ProductivityDao
    abstract fun financeDao(): FinanceDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun aiDao(): AIDao
    
    companion object {
        const val DATABASE_NAME = "best_productivity_database"
    }
}

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        return value?.split(",")?.mapNotNull { it.trim().toLongOrNull() }
    }
}

class PrepopulateCallback(
    private val database: Provider<BestProductivityDatabase>,
    private val applicationScope: CoroutineScope
) : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Populate the database in a background thread
        applicationScope.launch {
            populateDatabase()
        }
    }
    
    private suspend fun populateDatabase() {
        // TODO: Implement proper database prepopulation after fixing entity constructors
        // For now, skip prepopulation to get the build working
    }
}
