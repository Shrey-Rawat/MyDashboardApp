package com.mydashboardapp.export.di

import android.content.Context
import com.mydashboardapp.data.repository.NutritionRepository
import com.mydashboardapp.export.NutritionExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExportModule {

    @Provides
    @Singleton
    fun provideNutritionExporter(
        @ApplicationContext context: Context,
        nutritionRepository: NutritionRepository
    ): NutritionExporter {
        return NutritionExporter(context, nutritionRepository)
    }
}
