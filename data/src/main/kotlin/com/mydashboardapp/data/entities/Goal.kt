package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val category: String?, // e.g., "Career", "Health", "Personal", "Financial"
    val targetDate: Long?,
    val isCompleted: Boolean = false,
    val completedAt: Long?,
    val progress: Float = 0f, // 0.0 to 1.0
    val priority: String?, // e.g., "Low", "Medium", "High"
    val measurable: String?, // how to measure success
    val specificOutcome: String?, // what exactly you want to achieve
    val timeframe: String?, // e.g., "3 months", "1 year"
    val obstacles: String?, // potential challenges
    val resources: String?, // what you need to achieve this goal
    val motivation: String?, // why this goal is important
    val milestones: String?, // key checkpoints (JSON format)
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
