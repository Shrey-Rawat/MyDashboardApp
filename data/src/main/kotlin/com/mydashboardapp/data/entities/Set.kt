package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sets",
    indices = [Index("workoutExerciseId")],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseCrossRef::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Set(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutExerciseId: Long, // reference to WorkoutExerciseCrossRef
    val setNumber: Int, // order within the exercise (1, 2, 3, etc.)
    val reps: Int?,
    val weight: Double?, // in kg or lbs
    val duration: Int?, // in seconds for timed exercises
    val distance: Double?, // in meters/km for cardio
    val restTime: Int?, // in seconds
    val rpe: Int?, // Rate of Perceived Exertion (1-10)
    val notes: String?,
    val isCompleted: Boolean = false,
    val completedAt: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
