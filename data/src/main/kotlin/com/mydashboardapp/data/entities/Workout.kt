package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: Long, // timestamp of workout date
    val startTime: Long?, // timestamp when workout started
    val endTime: Long?, // timestamp when workout ended
    val duration: Int?, // in minutes
    val totalCaloriesBurned: Int?,
    val workoutType: String?, // e.g., "Strength", "Cardio", "Mixed"
    val intensity: String?, // e.g., "Low", "Medium", "High"
    val notes: String?,
    val location: String?, // e.g., "Gym", "Home", "Outdoors"
    val isTemplate: Boolean = false,
    val templateId: Long?, // reference to another workout used as template
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
