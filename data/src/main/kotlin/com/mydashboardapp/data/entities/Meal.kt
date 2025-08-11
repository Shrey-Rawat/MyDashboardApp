package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mealType: String, // e.g., "Breakfast", "Lunch", "Dinner", "Snack"
    val dateConsumed: Long, // timestamp
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double?,
    val totalSugar: Double?,
    val totalSodium: Double?,
    val notes: String?,
    val photoUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
