package com.mydashboardapp.training.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mydashboardapp.core.ui.components.*
import com.mydashboardapp.data.repository.PersonalRecord
import com.mydashboardapp.data.repository.OneRepMaxData
import com.mydashboardapp.data.repository.VolumeData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressAnalyticsScreen(
    onBackClick: () -> Unit = {},
    viewModel: ProgressAnalyticsViewModel = hiltViewModel()
) {
    val workoutSummary by viewModel.workoutSummary.collectAsState()
    val muscleGroupData by viewModel.muscleGroupData.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()
    val oneRepMaxHistory by viewModel.oneRepMaxHistory.collectAsState()
    val volumeHistory by viewModel.volumeHistory.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val selectedExerciseId by viewModel.selectedExerciseId.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time range selector
            item {
                TimeRangeSelector(
                    selectedTimeRange = selectedTimeRange,
                    onTimeRangeSelected = viewModel::setTimeRange
                )
            }
            
            // Workout summary stats
            item {
                WorkoutSummaryCard(workoutSummary = workoutSummary)
            }
            
            // Muscle group frequency chart
            item {
                MuscleGroupFrequencyCard(muscleGroupData = muscleGroupData)
            }
            
            // Personal records
            item {
                PersonalRecordsCard(personalRecords = personalRecords)
            }
            
            // Exercise-specific charts (if exercise is selected)
            if (selectedExerciseId != null && (oneRepMaxHistory.isNotEmpty() || volumeHistory.isNotEmpty())) {
                item {
                    ExerciseProgressCard(
                        oneRepMaxHistory = oneRepMaxHistory,
                        volumeHistory = volumeHistory
                    )
                }
            }
            
            // Exercise selector for detailed analysis
            item {
                ExerciseSelectorCard(
                    selectedExerciseId = selectedExerciseId,
                    onExerciseSelected = viewModel::setSelectedExercise
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedTimeRange: ProgressAnalyticsViewModel.TimeRange,
    onTimeRangeSelected: (ProgressAnalyticsViewModel.TimeRange) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Time Range",
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProgressAnalyticsViewModel.TimeRange.values()) { timeRange ->
                    FilterChip(
                        onClick = { onTimeRangeSelected(timeRange) },
                        label = { Text(timeRange.name.replace("_", " ")) },
                        selected = selectedTimeRange == timeRange
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutSummaryCard(workoutSummary: com.mydashboardapp.data.dao.TrainingDao.WorkoutSummary?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Workout Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (workoutSummary != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatItem(
                        title = "Workouts",
                        value = workoutSummary.totalWorkouts.toString(),
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                    
                    SummaryStatItem(
                        title = "Minutes",
                        value = workoutSummary.totalMinutes.toString(),
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    SummaryStatItem(
                        title = "Calories",
                        value = workoutSummary.totalCalories.toString(),
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = "No workout data available for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MuscleGroupFrequencyCard(muscleGroupData: List<com.mydashboardapp.data.dao.TrainingDao.MuscleGroupFrequency>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Muscle Group Frequency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (muscleGroupData.isNotEmpty()) {
                val chartData = muscleGroupData.map { muscleGroup ->
                    ChartData(
                        label = muscleGroup.muscleGroup,
                        value = muscleGroup.exerciseCount.toFloat(),
                        color = getColorForMuscleGroup(muscleGroup.muscleGroup)
                    )
                }
                
                BarChart(
                    data = chartData,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "No muscle group data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PersonalRecordsCard(personalRecords: List<PersonalRecord>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Personal Records",
                    tint = Color(0xFFFFD700) // Gold color
                )
            }
            
            if (personalRecords.isNotEmpty()) {
                personalRecords.take(5).forEach { record ->
                    PersonalRecordItem(record = record)
                    if (record != personalRecords.last()) {
                        HorizontalDivider()
                    }
                }
                
                if (personalRecords.size > 5) {
                    Text(
                        text = "... and ${personalRecords.size - 5} more records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No personal records yet. Keep training to set your first PR!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PersonalRecordItem(record: PersonalRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = record.exerciseName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = record.recordType,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${record.weight}kg × ${record.reps}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(record.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExerciseProgressCard(
    oneRepMaxHistory: List<OneRepMaxData>,
    volumeHistory: List<VolumeData>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Exercise Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (oneRepMaxHistory.isNotEmpty()) {
                Text(
                    text = "1 Rep Max Progress",
                    style = MaterialTheme.typography.titleSmall
                )
                
                LineChart(
                    data = oneRepMaxHistory.map { it.oneRepMax.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    lineColor = MaterialTheme.colorScheme.primary
                )
            }
            
            if (volumeHistory.isNotEmpty()) {
                Text(
                    text = "Volume Progress",
                    style = MaterialTheme.typography.titleSmall
                )
                
                LineChart(
                    data = volumeHistory.map { it.volume.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    lineColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ExerciseSelectorCard(
    selectedExerciseId: Long?,
    onExerciseSelected: (Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Exercise Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Select an exercise to view detailed progress charts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // This would typically be a dropdown or search field
            // For now, just show placeholder buttons for common exercises
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Bench Press", "Squat", "Deadlift", "Overhead Press")) { exercise ->
                    FilterChip(
                        onClick = { 
                            // In a real implementation, you'd map exercise names to IDs
                            onExerciseSelected(exercise.hashCode().toLong())
                        },
                        label = { Text(exercise) },
                        selected = selectedExerciseId == exercise.hashCode().toLong()
                    )
                }
            }
            
            if (selectedExerciseId != null) {
                OutlinedButton(
                    onClick = { onExerciseSelected(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Selection")
                }
            }
        }
    }
}

private fun getColorForMuscleGroup(muscleGroup: String): Color {
    return when (muscleGroup.lowercase()) {
        "chest" -> Color(0xFF2196F3)
        "back" -> Color(0xFF4CAF50)
        "legs" -> Color(0xFFFF9800)
        "arms" -> Color(0xFF9C27B0)
        "shoulders" -> Color(0xFFF44336)
        "core" -> Color(0xFF607D8B)
        else -> Color(0xFF795548)
    }
}
