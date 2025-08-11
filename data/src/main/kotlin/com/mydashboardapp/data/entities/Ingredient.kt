package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String?, // e.g., "Vegetable", "Protein", "Grain"
    val caloriesPerGram: Double,
    val proteinPerGram: Double,
    val carbsPerGram: Double,
    val fatPerGram: Double,
    val fiberPerGram: Double?,
    val sugarPerGram: Double?,
    val sodiumPerGram: Double?, // in mg
    val isAllergen: Boolean = false,
    val allergenInfo: String?, // e.g., "Contains gluten"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
