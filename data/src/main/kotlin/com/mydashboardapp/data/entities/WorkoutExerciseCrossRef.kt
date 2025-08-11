package com.mydashboardapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_exercise_cross_ref",
    indices = [Index("workoutId"), Index("exerciseId")],
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutExerciseCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val orderInWorkout: Int, // sequence of exercise in the workout
    val targetSets: Int?,
    val targetReps: Int?,
    val targetWeight: Double?,
    val targetDuration: Int?, // in seconds
    val targetDistance: Double?, // in meters/km
    val restBetweenSets: Int?, // in seconds
    val notes: String?,
    val isSuperset: Boolean = false,
    val supersetGroup: Int?, // group number for supersets
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
