package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index("goalId"), Index("parentTaskId")],
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["parentTaskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val isCompleted: Boolean = false,
    val priority: String?, // e.g., "Low", "Medium", "High", "Critical"
    val status: String?, // e.g., "Not Started", "In Progress", "Blocked", "Completed"
    val dueDate: Long?,
    val completedAt: Long?,
    val estimatedTimeMinutes: Int?,
    val actualTimeMinutes: Int?,
    val goalId: Long?, // reference to associated goal
    val parentTaskId: Long?, // for subtasks
    val category: String?, // e.g., "Work", "Personal", "Health"
    val tags: String?, // comma-separated tags
    val notes: String?,
    val reminderAt: Long?,
    val recurrenceRule: String?, // for recurring tasks
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
