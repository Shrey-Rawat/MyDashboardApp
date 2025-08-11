package com.mydashboardapp.data.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.mydashboardapp.core.security.KeystoreManager
import com.mydashboardapp.data.BestProductivityDatabase
import com.mydashboardapp.data.PrepopulateCallback
import com.mydashboardapp.data.dao.*
import com.mydashboardapp.data.migrations.DatabaseMigrations
import com.mydashboardapp.data.repository.NutritionRepository
import com.mydashboardapp.data.repository.TrainingRepository
import com.mydashboardapp.data.repository.TrainingRepositoryImpl
import com.mydashboardapp.data.importer.USDAFoodImporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @ApplicationScope
    fun providesCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope applicationScope: CoroutineScope,
        databaseProvider: Provider<BestProductivityDatabase>,
        keystoreManager: KeystoreManager
    ): BestProductivityDatabase {
        // Initialize SQLCipher
        SQLiteDatabase.loadLibs(context)
        
        // Get encryption key from Android Keystore
        val databaseKey = keystoreManager.getDatabaseKey()
        val supportFactory = SupportFactory(databaseKey)
        
        return Room.databaseBuilder(
            context,
            BestProductivityDatabase::class.java,
            BestProductivityDatabase.DATABASE_NAME
        )
            .openHelperFactory(supportFactory)
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .addCallback(PrepopulateCallback(databaseProvider, applicationScope))
            .fallbackToDestructiveMigration() // Remove this in production
            .build()
    }

    // DAO Providers
    @Provides
    fun provideNutritionDao(database: BestProductivityDatabase): NutritionDao {
        return database.nutritionDao()
    }

    @Provides
    fun provideTrainingDao(database: BestProductivityDatabase): TrainingDao {
        return database.trainingDao()
    }

    @Provides
    fun provideProductivityDao(database: BestProductivityDatabase): ProductivityDao {
        return database.productivityDao()
    }

    @Provides
    fun provideFinanceDao(database: BestProductivityDatabase): FinanceDao {
        return database.financeDao()
    }

    @Provides
    fun provideInventoryDao(database: BestProductivityDatabase): InventoryDao {
        return database.inventoryDao()
    }

    @Provides
    fun provideAIDao(database: BestProductivityDatabase): AIDao {
        return database.aiDao()
    }

    // Repository Providers
    @Provides
    @Singleton
    fun provideNutritionRepository(nutritionDao: NutritionDao): NutritionRepository {
        return NutritionRepositoryImpl(nutritionDao, null)
    }

    @Provides
    @Singleton
    fun provideTrainingRepository(trainingDao: TrainingDao): TrainingRepository {
        return TrainingRepositoryImpl(trainingDao)
    }

    @Provides
    @Singleton
    fun provideProductivityRepository(productivityDao: ProductivityDao): ProductivityRepository {
        return ProductivityRepositoryImpl(productivityDao)
    }

    @Provides
    @Singleton
    fun provideFinanceRepository(financeDao: FinanceDao): FinanceRepository {
        return FinanceRepositoryImpl(financeDao)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(inventoryDao: InventoryDao): InventoryRepository {
        return InventoryRepositoryImpl(inventoryDao)
    }

    @Provides
    @Singleton
    fun provideAIRepository(aiDao: AIDao): AIRepository {
        return AIRepositoryImpl(aiDao)
    }
    
    // Importer Provider
    @Provides
    @Singleton
    fun provideUSDAFoodImporter(nutritionDao: NutritionDao): USDAFoodImporter {
        return USDAFoodImporter(nutritionDao)
    }
}

// Placeholder repository implementations (you would create these similar to NutritionRepositoryImpl)
interface ProductivityRepository  
interface FinanceRepository
interface InventoryRepository
interface AIRepository

class ProductivityRepositoryImpl(private val productivityDao: ProductivityDao) : ProductivityRepository
class FinanceRepositoryImpl(private val financeDao: FinanceDao) : FinanceRepository
class InventoryRepositoryImpl(private val inventoryDao: InventoryDao) : InventoryRepository
class AIRepositoryImpl(private val aiDao: AIDao) : AIRepository
