package com.mydashboardapp.data.dao

import androidx.room.*
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    
    // Exercise operations
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>
    
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): Exercise?
    
    @Query("SELECT * FROM exercises WHERE category = :category")
    suspend fun getExercisesByCategory(category: String): List<Exercise>
    
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup")
    suspend fun getExercisesByMuscleGroup(muscleGroup: String): List<Exercise>
    
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%'")
    suspend fun searchExercises(query: String): List<Exercise>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long
    
    @Update
    suspend fun updateExercise(exercise: Exercise)
    
    @Delete
    suspend fun deleteExercise(exercise: Exercise)
    
    // Workout operations
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): Workout?
    
    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getWorkoutsByDateRange(startDate: Long, endDate: Long): List<Workout>
    
    @Query("SELECT * FROM workouts WHERE isTemplate = 1")
    suspend fun getWorkoutTemplates(): List<Workout>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long
    
    @Update
    suspend fun updateWorkout(workout: Workout)
    
    @Delete
    suspend fun deleteWorkout(workout: Workout)
    
    // WorkoutExerciseCrossRef operations
    @Query("SELECT * FROM workout_exercise_cross_ref WHERE workoutId = :workoutId ORDER BY orderInWorkout")
    suspend fun getExercisesForWorkout(workoutId: Long): List<WorkoutExerciseCrossRef>
    
    @Query("""
        SELECT e.*, wec.orderInWorkout, wec.targetSets, wec.targetReps 
        FROM exercises e 
        INNER JOIN workout_exercise_cross_ref wec ON e.id = wec.exerciseId 
        WHERE wec.workoutId = :workoutId 
        ORDER BY wec.orderInWorkout
    """)
    suspend fun getExercisesByWorkoutId(workoutId: Long): List<ExerciseWithWorkoutInfo>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExerciseCrossRef(workoutExerciseCrossRef: WorkoutExerciseCrossRef): Long
    
    @Update
    suspend fun updateWorkoutExerciseCrossRef(workoutExerciseCrossRef: WorkoutExerciseCrossRef)
    
    @Delete
    suspend fun deleteWorkoutExerciseCrossRef(workoutExerciseCrossRef: WorkoutExerciseCrossRef)
    
    @Query("DELETE FROM workout_exercise_cross_ref WHERE workoutId = :workoutId")
    suspend fun deleteAllExercisesForWorkout(workoutId: Long)
    
    // Set operations
    @Query("SELECT * FROM sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber")
    suspend fun getSetsForWorkoutExercise(workoutExerciseId: Long): List<com.mydashboardapp.data.entities.Set>
    
    @Query("SELECT * FROM sets WHERE id = :id")
    suspend fun getSetById(id: Long): com.mydashboardapp.data.entities.Set?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: com.mydashboardapp.data.entities.Set): Long
    
    @Update
    suspend fun updateSet(set: com.mydashboardapp.data.entities.Set)
    
    @Delete
    suspend fun deleteSet(set: com.mydashboardapp.data.entities.Set)
    
    @Query("DELETE FROM sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun deleteAllSetsForWorkoutExercise(workoutExerciseId: Long)
    
    // Analytics queries
    @Query("""
        SELECT COUNT(*) as totalWorkouts,
               SUM(duration) as totalMinutes,
               SUM(totalCaloriesBurned) as totalCalories
        FROM workouts 
        WHERE date BETWEEN :startDate AND :endDate
        AND endTime IS NOT NULL
    """)
    suspend fun getWorkoutSummary(startDate: Long, endDate: Long): WorkoutSummary
    
    @Query("""
        SELECT e.muscleGroup, COUNT(*) as exerciseCount
        FROM exercises e
        INNER JOIN workout_exercise_cross_ref wec ON e.id = wec.exerciseId
        INNER JOIN workouts w ON wec.workoutId = w.id
        WHERE w.date BETWEEN :startDate AND :endDate
        GROUP BY e.muscleGroup
        ORDER BY exerciseCount DESC
    """)
    suspend fun getMuscleGroupFrequency(startDate: Long, endDate: Long): List<MuscleGroupFrequency>
    
    data class ExerciseWithWorkoutInfo(
        val id: Long,
        val name: String,
        val category: String,
        val muscleGroup: String?,
        val equipment: String?,
        val orderInWorkout: Int,
        val targetSets: Int?,
        val targetReps: Int?
    )
    
    data class WorkoutSummary(
        val totalWorkouts: Int,
        val totalMinutes: Int,
        val totalCalories: Int
    )
    
    data class MuscleGroupFrequency(
        val muscleGroup: String,
        val exerciseCount: Int
    )
}
