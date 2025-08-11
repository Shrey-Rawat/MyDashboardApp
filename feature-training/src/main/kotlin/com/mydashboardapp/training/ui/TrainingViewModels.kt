package com.mydashboardapp.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.ui.SimpleBaseViewModel
import com.mydashboardapp.core.ui.UiState
import com.mydashboardapp.data.entities.*
import com.mydashboardapp.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// Main Training Screen ViewModel
@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : SimpleBaseViewModel() {
    
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()
    
    private val _workoutTemplates = MutableStateFlow<List<Workout>>(emptyList())
    val workoutTemplates: StateFlow<List<Workout>> = _workoutTemplates.asStateFlow()
    
    private val _todayWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val todayWorkouts: StateFlow<List<Workout>> = _todayWorkouts.asStateFlow()
    
    private val _weeklyWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val weeklyWorkouts: StateFlow<List<Workout>> = _weeklyWorkouts.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                setLoading(true)
                
                // Load all workouts
                trainingRepository.getAllWorkouts().collect { workoutList ->
                    _workouts.value = workoutList
                }
                
                // Load templates
                val templates = trainingRepository.getWorkoutTemplates()
                _workoutTemplates.value = templates
                
                // Load today's workouts
                val today = Calendar.getInstance()
                val startOfDay = Calendar.getInstance().apply {
                    time = today.time
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endOfDay = Calendar.getInstance().apply {
                    time = today.time
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                val todayList = trainingRepository.getWorkoutsByDateRange(
                    startOfDay.timeInMillis,
                    endOfDay.timeInMillis
                )
                _todayWorkouts.value = todayList
                
                // Load this week's workouts
                val weekStart = Calendar.getInstance().apply {
                    time = today.time
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val weekEnd = Calendar.getInstance().apply {
                    time = weekStart.time
                    add(Calendar.DAY_OF_YEAR, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                val weeklyList = trainingRepository.getWorkoutsByDateRange(
                    weekStart.timeInMillis,
                    weekEnd.timeInMillis
                )
                _weeklyWorkouts.value = weeklyList
                
                setLoading(false)
            } catch (e: Exception) {
                setError(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun startWorkout(templateId: Long? = null) {
        viewModelScope.launch {
            try {
                val workoutId = if (templateId != null) {
                    trainingRepository.createWorkoutFromTemplate(templateId, System.currentTimeMillis())
                } else {
                    val newWorkout = Workout(
                        name = "New Workout",
                        date = System.currentTimeMillis(),
                        startTime = System.currentTimeMillis()
                    )
                    trainingRepository.insertWorkout(newWorkout)
                }
                // Navigation to live session would be handled in UI
            } catch (e: Exception) {
                setError(e.message ?: "Failed to start workout")
            }
        }
    }
}

// Workout Planner ViewModel
@HiltViewModel
class WorkoutPlannerViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : SimpleBaseViewModel() {
    
    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()
    
    private val _selectedExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val selectedExercises: StateFlow<List<Exercise>> = _selectedExercises.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    private val _workoutName = MutableStateFlow("")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()
    
    private val _isTemplate = MutableStateFlow(false)
    val isTemplate: StateFlow<Boolean> = _isTemplate.asStateFlow()
    
    init {
        loadExercises()
    }
    
    private fun loadExercises() {
        viewModelScope.launch {
            try {
                trainingRepository.getAllExercises().collect { exerciseList ->
                    _exercises.value = exerciseList
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load exercises")
            }
        }
    }
    
    fun searchExercises(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            try {
                val results = if (query.isBlank()) {
                    if (selectedCategory.value != null) {
                        trainingRepository.getExercisesByCategory(selectedCategory.value!!)
                    } else {
                        exercises.value
                    }
                } else {
                    trainingRepository.searchExercises(query)
                }
                _exercises.value = results
            } catch (e: Exception) {
                setError(e.message ?: "Search failed")
            }
        }
    }
    
    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            try {
                val results = if (category != null) {
                    trainingRepository.getExercisesByCategory(category)
                } else {
                    exercises.value
                }
                _exercises.value = results
            } catch (e: Exception) {
                setError(e.message ?: "Filter failed")
            }
        }
    }
    
    fun addExercise(exercise: Exercise) {
        _selectedExercises.value = _selectedExercises.value + exercise
    }
    
    fun removeExercise(exercise: Exercise) {
        _selectedExercises.value = _selectedExercises.value.filter { it.id != exercise.id }
    }
    
    fun setWorkoutName(name: String) {
        _workoutName.value = name
    }
    
    fun setIsTemplate(isTemplate: Boolean) {
        _isTemplate.value = isTemplate
    }
    
    fun saveWorkout() {
        viewModelScope.launch {
            try {
                if (workoutName.value.isBlank()) {
                    setError("Please enter a workout name")
                    return@launch
                }
                
                if (selectedExercises.value.isEmpty()) {
                    setError("Please add at least one exercise")
                    return@launch
                }
                
                val workout = Workout(
                    name = workoutName.value,
                    date = System.currentTimeMillis(),
                    isTemplate = isTemplate.value
                )
                
                val workoutId = trainingRepository.insertWorkout(workout)
                
                // Add exercises to workout
                selectedExercises.value.forEachIndexed { index, exercise ->
                    val workoutExercise = WorkoutExerciseCrossRef(
                        workoutId = workoutId,
                        exerciseId = exercise.id,
                        orderInWorkout = index + 1
                    )
                    trainingRepository.addExerciseToWorkout(workoutExercise)
                }
                
                // Clear form
                _selectedExercises.value = emptyList()
                _workoutName.value = ""
                _isTemplate.value = false
                
            } catch (e: Exception) {
                setError(e.message ?: "Failed to save workout")
            }
        }
    }
}

// Live Session Tracker ViewModel
@HiltViewModel
class LiveSessionViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : SimpleBaseViewModel() {
    
    private val _currentWorkout = MutableStateFlow<Workout?>(null)
    val currentWorkout: StateFlow<Workout?> = _currentWorkout.asStateFlow()
    
    private val _workoutExercises = MutableStateFlow<List<TrainingDao.ExerciseWithWorkoutInfo>>(emptyList())
    val workoutExercises: StateFlow<List<TrainingDao.ExerciseWithWorkoutInfo>> = _workoutExercises.asStateFlow()
    
    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()
    
    private val _currentSets = MutableStateFlow<List<Set>>(emptyList())
    val currentSets: StateFlow<List<Set>> = _currentSets.asStateFlow()
    
    private val _workoutTimer = MutableStateFlow(0L)
    val workoutTimer: StateFlow<Long> = _workoutTimer.asStateFlow()
    
    private val _restTimer = MutableStateFlow(0L)
    val restTimer: StateFlow<Long> = _restTimer.asStateFlow()
    
    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()
    
    private var workoutStartTime = 0L
    private var restStartTime = 0L
    
    fun startWorkout(workoutId: Long) {
        viewModelScope.launch {
            try {
                val workout = trainingRepository.getWorkoutById(workoutId)
                if (workout != null) {
                    workoutStartTime = System.currentTimeMillis()
                    val updatedWorkout = workout.copy(
                        startTime = workoutStartTime
                    )
                    trainingRepository.updateWorkout(updatedWorkout)
                    _currentWorkout.value = updatedWorkout
                    
                    // Load exercises
                    val exercises = trainingRepository.getExercisesByWorkoutId(workoutId)
                    _workoutExercises.value = exercises
                    
                    // Load first exercise sets if any
                    if (exercises.isNotEmpty()) {
                        loadSetsForCurrentExercise()
                    }
                    
                    // Start workout timer
                    startWorkoutTimer()
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to start workout")
            }
        }
    }
    
    private fun loadSetsForCurrentExercise() {
        viewModelScope.launch {
            try {
                val currentExercise = workoutExercises.value[currentExerciseIndex.value]
                // For this we'd need the WorkoutExerciseCrossRef ID, simplified for now
                _currentSets.value = emptyList()
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load sets")
            }
        }
    }
    
    fun addSet(weight: Double?, reps: Int?, duration: Int?, distance: Double?) {
        viewModelScope.launch {
            try {
                val currentExercise = workoutExercises.value.getOrNull(currentExerciseIndex.value)
                if (currentExercise != null && currentWorkout.value != null) {
                    // Simplified - would need actual WorkoutExerciseCrossRef ID
                    val setNumber = currentSets.value.size + 1
                    val newSet = Set(
                        workoutExerciseId = 0, // Would be actual ID
                        setNumber = setNumber,
                        reps = reps,
                        weight = weight,
                        duration = duration,
                        distance = distance,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                    
                    val setId = trainingRepository.insertSet(newSet)
                    loadSetsForCurrentExercise()
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to add set")
            }
        }
    }
    
    fun startRestTimer(seconds: Int) {
        _isResting.value = true
        restStartTime = System.currentTimeMillis()
        _restTimer.value = seconds.toLong()
        
        // Would implement actual countdown timer here
    }
    
    fun stopRestTimer() {
        _isResting.value = false
        _restTimer.value = 0L
    }
    
    fun nextExercise() {
        if (currentExerciseIndex.value < workoutExercises.value.size - 1) {
            _currentExerciseIndex.value += 1
            loadSetsForCurrentExercise()
        }
    }
    
    fun previousExercise() {
        if (currentExerciseIndex.value > 0) {
            _currentExerciseIndex.value -= 1
            loadSetsForCurrentExercise()
        }
    }
    
    fun finishWorkout() {
        viewModelScope.launch {
            try {
                val workout = currentWorkout.value
                if (workout != null) {
                    val endTime = System.currentTimeMillis()
                    val duration = ((endTime - workoutStartTime) / 1000 / 60).toInt() // minutes
                    
                    val finishedWorkout = workout.copy(
                        endTime = endTime,
                        duration = duration
                    )
                    
                    trainingRepository.updateWorkout(finishedWorkout)
                    _currentWorkout.value = null
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to finish workout")
            }
        }
    }
    
    private fun startWorkoutTimer() {
        // Would implement actual timer here using coroutines
        // For now just placeholder
    }
}

// Progress Analytics ViewModel
@HiltViewModel
class ProgressAnalyticsViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : SimpleBaseViewModel() {
    
    private val _workoutSummary = MutableStateFlow<TrainingDao.WorkoutSummary?>(null)
    val workoutSummary: StateFlow<TrainingDao.WorkoutSummary?> = _workoutSummary.asStateFlow()
    
    private val _muscleGroupData = MutableStateFlow<List<TrainingDao.MuscleGroupFrequency>>(emptyList())
    val muscleGroupData: StateFlow<List<TrainingDao.MuscleGroupFrequency>> = _muscleGroupData.asStateFlow()
    
    private val _personalRecords = MutableStateFlow<List<PersonalRecord>>(emptyList())
    val personalRecords: StateFlow<List<PersonalRecord>> = _personalRecords.asStateFlow()
    
    private val _oneRepMaxHistory = MutableStateFlow<List<OneRepMaxData>>(emptyList())
    val oneRepMaxHistory: StateFlow<List<OneRepMaxData>> = _oneRepMaxHistory.asStateFlow()
    
    private val _volumeHistory = MutableStateFlow<List<VolumeData>>(emptyList())
    val volumeHistory: StateFlow<List<VolumeData>> = _volumeHistory.asStateFlow()
    
    private val _selectedTimeRange = MutableStateFlow(TimeRange.MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()
    
    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId: StateFlow<Long?> = _selectedExerciseId.asStateFlow()
    
    enum class TimeRange(val days: Int) {
        WEEK(7),
        MONTH(30),
        THREE_MONTHS(90),
        YEAR(365)
    }
    
    init {
        loadAnalytics()
    }
    
    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        loadAnalytics()
    }
    
    fun setSelectedExercise(exerciseId: Long?) {
        _selectedExerciseId.value = exerciseId
        loadExerciseSpecificData()
    }
    
    private fun loadAnalytics() {
        viewModelScope.launch {
            try {
                setLoading(true)
                
                val endDate = System.currentTimeMillis()
                val startDate = endDate - (selectedTimeRange.value.days * 24 * 60 * 60 * 1000L)
                
                // Load workout summary
                val summary = trainingRepository.getWorkoutSummary(startDate, endDate)
                _workoutSummary.value = summary
                
                // Load muscle group frequency
                val muscleGroupFreq = trainingRepository.getMuscleGroupFrequency(startDate, endDate)
                _muscleGroupData.value = muscleGroupFreq
                
                // Load personal records
                val records = trainingRepository.getPersonalRecords()
                _personalRecords.value = records
                
                setLoading(false)
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load analytics")
            }
        }
    }
    
    private fun loadExerciseSpecificData() {
        viewModelScope.launch {
            try {
                val exerciseId = selectedExerciseId.value ?: return@launch
                
                // Load 1RM history
                val oneRepMax = trainingRepository.getOneRepMaxHistory(
                    exerciseId, 
                    selectedTimeRange.value.days
                )
                _oneRepMaxHistory.value = oneRepMax
                
                // Load volume history
                val volume = trainingRepository.getVolumeHistory(
                    exerciseId, 
                    selectedTimeRange.value.days
                )
                _volumeHistory.value = volume
                
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load exercise data")
            }
        }
    }
}
