package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_logs",
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
data class TimeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long?, // in seconds
    val taskId: Long?, // reference to associated task
    val goalId: Long?, // reference to associated goal
    val activity: String, // description of what was done
    val category: String?, // e.g., "Work", "Learning", "Exercise", "Break"
    val isBreak: Boolean = false,
    val isPomodoro: Boolean = false,
    val pomodoroNumber: Int?, // which pomodoro session this is
    val productivity: Int?, // self-rated productivity (1-10)
    val mood: String?, // e.g., "Focused", "Distracted", "Motivated", "Tired"
    val location: String?, // where the work was done
    val notes: String?,
    val tags: String?, // comma-separated tags
    val isManual: Boolean = false, // true if manually entered, false if tracked
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
