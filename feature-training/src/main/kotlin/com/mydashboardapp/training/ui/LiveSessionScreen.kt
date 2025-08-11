package com.mydashboardapp.training.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mydashboardapp.data.entities.Set
import com.mydashboardapp.data.dao.TrainingDao
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSessionScreen(
    workoutId: Long,
    onFinishWorkout: () -> Unit = {},
    viewModel: LiveSessionViewModel = hiltViewModel()
) {
    val currentWorkout by viewModel.currentWorkout.collectAsState()
    val workoutExercises by viewModel.workoutExercises.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val currentSets by viewModel.currentSets.collectAsState()
    val workoutTimer by viewModel.workoutTimer.collectAsState()
    val restTimer by viewModel.restTimer.collectAsState()
    val isResting by viewModel.isResting.collectAsState()
    
    LaunchedEffect(workoutId) {
        viewModel.startWorkout(workoutId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(currentWorkout?.name ?: "Workout Session") 
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.finishWorkout()
                            onFinishWorkout()
                        }
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Finish Workout")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (workoutExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No exercises in this workout")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Workout timer and status
                WorkoutStatusCard(
                    workoutTimer = workoutTimer,
                    restTimer = restTimer,
                    isResting = isResting,
                    currentExerciseIndex = currentExerciseIndex,
                    totalExercises = workoutExercises.size
                )
                
                // Current exercise
                if (currentExerciseIndex < workoutExercises.size) {
                    CurrentExerciseCard(
                        exercise = workoutExercises[currentExerciseIndex],
                        sets = currentSets,
                        onAddSet = viewModel::addSet,
                        onStartRest = viewModel::startRestTimer,
                        onStopRest = viewModel::stopRestTimer,
                        isResting = isResting
                    )
                }
                
                // Exercise navigation
                ExerciseNavigationCard(
                    currentIndex = currentExerciseIndex,
                    totalExercises = workoutExercises.size,
                    onPrevious = viewModel::previousExercise,
                    onNext = viewModel::nextExercise,
                    exercises = workoutExercises
                )
            }
        }
    }
}

@Composable
private fun WorkoutStatusCard(
    workoutTimer: Long,
    restTimer: Long,
    isResting: Boolean,
    currentExerciseIndex: Int,
    totalExercises: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isResting) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isResting) {
                Text(
                    text = "REST TIME",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = formatTime(restTimer),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                Text(
                    text = "WORKOUT TIME",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = formatTime(workoutTimer),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Exercise ${currentExerciseIndex + 1} of $totalExercises",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isResting) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CurrentExerciseCard(
    exercise: TrainingDao.ExerciseWithWorkoutInfo,
    sets: List<Set>,
    onAddSet: (weight: Double?, reps: Int?, duration: Int?, distance: Double?) -> Unit,
    onStartRest: (Int) -> Unit,
    onStopRest: () -> Unit,
    isResting: Boolean
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exercise info
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${exercise.category} • ${exercise.muscleGroup}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Target info if available
            if (exercise.targetSets != null || exercise.targetReps != null) {
                Text(
                    text = buildString {
                        if (exercise.targetSets != null) append("Target: ${exercise.targetSets} sets")
                        if (exercise.targetReps != null) {
                            if (isNotEmpty()) append(" × ")
                            append("${exercise.targetReps} reps")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Previous sets
            if (sets.isNotEmpty()) {
                Text(
                    text = "Completed Sets",
                    style = MaterialTheme.typography.titleSmall
                )
                
                sets.forEach { set ->
                    SetSummaryRow(set = set)
                }
                
                HorizontalDivider()
            }
            
            // Add new set
            Text(
                text = "Add Set ${sets.size + 1}",
                style = MaterialTheme.typography.titleSmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Weight input
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                
                // Reps input
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // For cardio exercises, show duration/distance
            if (exercise.category == "Cardio") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        suffix = { Text("sec") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("Distance") },
                        suffix = { Text("m") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onAddSet(
                            weight.toDoubleOrNull(),
                            reps.toIntOrNull(),
                            duration.toIntOrNull(),
                            distance.toDoubleOrNull()
                        )
                        // Clear inputs
                        weight = ""
                        reps = ""
                        duration = ""
                        distance = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Set")
                }
                
                if (isResting) {
                    OutlinedButton(
                        onClick = onStopRest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop Rest")
                    }
                } else {
                    OutlinedButton(
                        onClick = { onStartRest(90) }, // 90 second default rest
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rest 90s")
                    }
                }
            }
        }
    }
}

@Composable
private fun SetSummaryRow(set: Set) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${set.setNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = buildString {
                set.weight?.let { append("${it}kg") }
                set.reps?.let { 
                    if (isNotEmpty()) append(" × ")
                    append("${it} reps") 
                }
                set.duration?.let { 
                    if (isNotEmpty()) append(" • ")
                    append("${it}s") 
                }
                set.distance?.let { 
                    if (isNotEmpty()) append(" • ")
                    append("${it}m") 
                }
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ExerciseNavigationCard(
    currentIndex: Int,
    totalExercises: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    exercises: List<TrainingDao.ExerciseWithWorkoutInfo>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Exercise Navigation",
                style = MaterialTheme.typography.titleSmall
            )
            
            // Progress indicator
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalExercises },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }
                
                OutlinedButton(
                    onClick = onNext,
                    enabled = currentIndex < totalExercises - 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
            
            // Exercise list
            LazyColumn(
                modifier = Modifier.height(120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(exercises.size) { index ->
                    val exercise = exercises[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (index < currentIndex) Icons.Default.CheckCircle
                            else if (index == currentIndex) Icons.Default.PlayCircle
                            else Icons.Default.Circle,
                            contentDescription = null,
                            tint = when {
                                index < currentIndex -> MaterialTheme.colorScheme.primary
                                index == currentIndex -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (index == currentIndex) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
