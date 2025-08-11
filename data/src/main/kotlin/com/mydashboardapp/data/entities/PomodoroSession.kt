package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pomodoro_sessions",
    indices = [Index("taskId"), Index("goalId")],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val durationMinutes: Int = 25, // Default Pomodoro length
    val type: PomodoroType = PomodoroType.WORK,
    val taskId: Long?, // Optional associated task
    val goalId: Long?, // Optional associated goal
    val isCompleted: Boolean = false,
    val wasInterrupted: Boolean = false,
    val interruptionReason: String?, // e.g., "Phone call", "Meeting", etc.
    val productivity: Int?, // Self-rated productivity (1-10)
    val mood: String?, // How user felt during session
    val notes: String?,
    val tags: String?, // comma-separated tags
    val createdAt: Long = System.currentTimeMillis()
)

enum class PomodoroType {
    WORK,           // 25 minutes work session
    SHORT_BREAK,    // 5 minutes break
    LONG_BREAK      // 15-30 minutes break
}
