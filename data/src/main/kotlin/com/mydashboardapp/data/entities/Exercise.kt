package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // e.g., "Strength", "Cardio", "Flexibility", "Balance"
    val muscleGroup: String?, // e.g., "Chest", "Back", "Legs", "Arms"
    val equipment: String?, // e.g., "Barbell", "Dumbbells", "Bodyweight", "Machine"
    val description: String?,
    val instructions: String?,
    val difficulty: String?, // e.g., "Beginner", "Intermediate", "Advanced"
    val caloriesPerMinute: Double?,
    val videoUrl: String?,
    val imageUrl: String?,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
