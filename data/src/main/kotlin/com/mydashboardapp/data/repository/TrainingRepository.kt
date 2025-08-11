package com.mydashboardapp.data.repository

import com.mydashboardapp.data.dao.TrainingDao
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface TrainingRepository {
    // Exercise operations
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun getExercisesByCategory(category: String): List<Exercise>
    suspend fun getExercisesByMuscleGroup(muscleGroup: String): List<Exercise>
    suspend fun searchExercises(query: String): List<Exercise>
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(exercise: Exercise)
    
    // Workout operations
    fun getAllWorkouts(): Flow<List<Workout>>
    suspend fun getWorkoutById(id: Long): Workout?
    suspend fun getWorkoutsByDateRange(startDate: Long, endDate: Long): List<Workout>
    suspend fun getWorkoutTemplates(): List<Workout>
    suspend fun insertWorkout(workout: Workout): Long
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
    suspend fun createWorkoutFromTemplate(templateId: Long, date: Long): Long
    
    // Workout exercises
    suspend fun getExercisesForWorkout(workoutId: Long): List<WorkoutExerciseCrossRef>
    suspend fun getExercisesByWorkoutId(workoutId: Long): List<TrainingDao.ExerciseWithWorkoutInfo>
    suspend fun addExerciseToWorkout(workoutExercise: WorkoutExerciseCrossRef): Long
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExerciseCrossRef)
    suspend fun removeExerciseFromWorkout(workoutExercise: WorkoutExerciseCrossRef)
    suspend fun deleteAllExercisesForWorkout(workoutId: Long)
    
    // Set operations - temporarily disabled due to type conflicts
    // suspend fun getSetsForWorkoutExercise(workoutExerciseId: Long): List<com.mydashboardapp.data.entities.Set>
    // suspend fun getSetById(id: Long): com.mydashboardapp.data.entities.Set?
    // suspend fun insertSet(set: com.mydashboardapp.data.entities.Set): Long
    // suspend fun updateSet(set: com.mydashboardapp.data.entities.Set)
    // suspend fun deleteSet(set: com.mydashboardapp.data.entities.Set)
    suspend fun deleteAllSetsForWorkoutExercise(workoutExerciseId: Long)
    
    // Analytics
    suspend fun getWorkoutSummary(startDate: Long, endDate: Long): TrainingDao.WorkoutSummary
    suspend fun getMuscleGroupFrequency(startDate: Long, endDate: Long): List<TrainingDao.MuscleGroupFrequency>
    suspend fun getOneRepMaxHistory(exerciseId: Long, days: Int): List<OneRepMaxData>
    suspend fun getVolumeHistory(exerciseId: Long, days: Int): List<VolumeData>
    suspend fun getPersonalRecords(): List<PersonalRecord>
}

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val trainingDao: TrainingDao
) : TrainingRepository {
    
    override fun getAllExercises(): Flow<List<Exercise>> = trainingDao.getAllExercises()
    
    override suspend fun getExerciseById(id: Long): Exercise? = trainingDao.getExerciseById(id)
    
    override suspend fun getExercisesByCategory(category: String): List<Exercise> = 
        trainingDao.getExercisesByCategory(category)
    
    override suspend fun getExercisesByMuscleGroup(muscleGroup: String): List<Exercise> = 
        trainingDao.getExercisesByMuscleGroup(muscleGroup)
    
    override suspend fun searchExercises(query: String): List<Exercise> = 
        trainingDao.searchExercises(query)
    
    override suspend fun insertExercise(exercise: Exercise): Long = 
        trainingDao.insertExercise(exercise)
    
    override suspend fun updateExercise(exercise: Exercise) = 
        trainingDao.updateExercise(exercise)
    
    override suspend fun deleteExercise(exercise: Exercise) = 
        trainingDao.deleteExercise(exercise)
    
    override fun getAllWorkouts(): Flow<List<Workout>> = trainingDao.getAllWorkouts()
    
    override suspend fun getWorkoutById(id: Long): Workout? = trainingDao.getWorkoutById(id)
    
    override suspend fun getWorkoutsByDateRange(startDate: Long, endDate: Long): List<Workout> = 
        trainingDao.getWorkoutsByDateRange(startDate, endDate)
    
    override suspend fun getWorkoutTemplates(): List<Workout> = trainingDao.getWorkoutTemplates()
    
    override suspend fun insertWorkout(workout: Workout): Long = trainingDao.insertWorkout(workout)
    
    override suspend fun updateWorkout(workout: Workout) = trainingDao.updateWorkout(workout)
    
    override suspend fun deleteWorkout(workout: Workout) = trainingDao.deleteWorkout(workout)
    
    override suspend fun createWorkoutFromTemplate(templateId: Long, date: Long): Long {
        val template = getWorkoutById(templateId) ?: throw IllegalArgumentException("Template not found")
        val newWorkout = template.copy(
            id = 0,
            name = template.name,
            date = date,
            startTime = null,
            endTime = null,
            duration = null,
            totalCaloriesBurned = null,
            isTemplate = false,
            templateId = templateId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val workoutId = insertWorkout(newWorkout)
        
        // Copy exercises from template
        val templateExercises = getExercisesForWorkout(templateId)
        templateExercises.forEach { templateExercise ->
            val newWorkoutExercise = templateExercise.copy(
                id = 0,
                workoutId = workoutId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            addExerciseToWorkout(newWorkoutExercise)
        }
        
        return workoutId
    }
    
    override suspend fun getExercisesForWorkout(workoutId: Long): List<WorkoutExerciseCrossRef> = 
        trainingDao.getExercisesForWorkout(workoutId)
    
    override suspend fun getExercisesByWorkoutId(workoutId: Long): List<TrainingDao.ExerciseWithWorkoutInfo> = 
        trainingDao.getExercisesByWorkoutId(workoutId)
    
    override suspend fun addExerciseToWorkout(workoutExercise: WorkoutExerciseCrossRef): Long = 
        trainingDao.insertWorkoutExerciseCrossRef(workoutExercise)
    
    override suspend fun updateWorkoutExercise(workoutExercise: WorkoutExerciseCrossRef) = 
        trainingDao.updateWorkoutExerciseCrossRef(workoutExercise)
    
    override suspend fun removeExerciseFromWorkout(workoutExercise: WorkoutExerciseCrossRef) = 
        trainingDao.deleteWorkoutExerciseCrossRef(workoutExercise)
    
    override suspend fun deleteAllExercisesForWorkout(workoutId: Long) = 
        trainingDao.deleteAllExercisesForWorkout(workoutId)
    
    /*
    override suspend fun getSetsForWorkoutExercise(workoutExerciseId: Long): List<com.mydashboardapp.data.entities.Set> = 
        trainingDao.getSetsForWorkoutExercise(workoutExerciseId)
    
    override suspend fun getSetById(id: Long): com.mydashboardapp.data.entities.Set? = trainingDao.getSetById(id)
    
    override suspend fun insertSet(set: com.mydashboardapp.data.entities.Set): Long = trainingDao.insertSet(set)
    
    override suspend fun updateSet(set: com.mydashboardapp.data.entities.Set) = trainingDao.updateSet(set)
    
    override suspend fun deleteSet(set: com.mydashboardapp.data.entities.Set) = trainingDao.deleteSet(set)
    */
    
    override suspend fun deleteAllSetsForWorkoutExercise(workoutExerciseId: Long) = 
        trainingDao.deleteAllSetsForWorkoutExercise(workoutExerciseId)
    
    override suspend fun getWorkoutSummary(startDate: Long, endDate: Long): TrainingDao.WorkoutSummary = 
        trainingDao.getWorkoutSummary(startDate, endDate)
    
    override suspend fun getMuscleGroupFrequency(startDate: Long, endDate: Long): List<TrainingDao.MuscleGroupFrequency> = 
        trainingDao.getMuscleGroupFrequency(startDate, endDate)
    
    override suspend fun getOneRepMaxHistory(exerciseId: Long, days: Int): List<OneRepMaxData> {
        // Implementation would calculate 1RM from recent sets
        // For now, return mock data - you can implement actual 1RM calculation
        return emptyList()
    }
    
    override suspend fun getVolumeHistory(exerciseId: Long, days: Int): List<VolumeData> {
        // Implementation would calculate volume (weight * reps * sets) from recent workouts
        // For now, return mock data - you can implement actual volume calculation
        return emptyList()
    }
    
    override suspend fun getPersonalRecords(): List<PersonalRecord> {
        // Implementation would find best lifts for each exercise
        // For now, return mock data - you can implement actual PR calculation
        return emptyList()
    }
}

data class OneRepMaxData(
    val date: Long,
    val oneRepMax: Double,
    val exerciseId: Long
)

data class VolumeData(
    val date: Long,
    val volume: Double, // weight * reps * sets
    val exerciseId: Long
)

data class PersonalRecord(
    val exerciseId: Long,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val date: Long,
    val recordType: String // "1RM", "Volume", "Reps", etc.
)
