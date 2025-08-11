package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streaks",
    indices = [Index("goalId"), Index("taskId")],
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Streak(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Name of the streak (e.g., "Daily Exercise", "Morning Meditation")
    val description: String?,
    val type: StreakType = StreakType.HABIT,
    val goalId: Long?, // Associated goal
    val taskId: Long?, // Associated recurring task
    val currentStreak: Int = 0, // Current consecutive days
    val longestStreak: Int = 0, // Best streak ever achieved
    val totalCount: Int = 0, // Total times completed
    val startDate: Long, // When streak tracking started
    val lastCompletedDate: Long?, // Last time it was completed
    val frequency: StreakFrequency = StreakFrequency.DAILY,
    val targetValue: Int = 1, // How many times per frequency period
    val unit: String?, // What we're measuring (e.g., "minutes", "reps", "pages")
    val isActive: Boolean = true,
    val reminderTime: Long?, // When to remind user
    val category: String?, // Category for grouping
    val priority: String?, // Priority level
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "streak_logs",
    indices = [Index("streakId"), Index("date")],
    foreignKeys = [
        ForeignKey(
            entity = Streak::class,
            parentColumns = ["id"],
            childColumns = ["streakId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StreakLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val streakId: Long,
    val date: Long, // Date when completed (midnight timestamp)
    val value: Int = 1, // How much was completed (e.g., 30 for 30 minutes)
    val notes: String?,
    val mood: String?, // How user felt
    val quality: Int?, // Self-rated quality (1-10)
    val createdAt: Long = System.currentTimeMillis()
)

enum class StreakType {
    HABIT,      // Daily habits (exercise, meditation, etc.)
    GOAL,       // Goal-based streaks
    TASK,       // Task completion streaks
    CUSTOM      // User-defined streaks
}

enum class StreakFrequency {
    DAILY,      // Every day
    WEEKLY,     // Once per week
    WEEKDAYS,   // Monday to Friday
    WEEKENDS    // Saturday and Sunday
}
