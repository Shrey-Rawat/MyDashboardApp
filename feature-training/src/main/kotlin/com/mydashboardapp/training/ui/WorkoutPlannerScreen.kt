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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mydashboardapp.core.ui.components.ExpandableCard
import com.mydashboardapp.core.ui.components.SearchBar
import com.mydashboardapp.data.entities.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlannerScreen(
    onBackClick: () -> Unit = {},
    viewModel: WorkoutPlannerViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val selectedExercises by viewModel.selectedExercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val workoutName by viewModel.workoutName.collectAsState()
    val isTemplate by viewModel.isTemplate.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Planner") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveWorkout() }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Workout details section
            WorkoutDetailsSection(
                workoutName = workoutName,
                onWorkoutNameChange = viewModel::setWorkoutName,
                isTemplate = isTemplate,
                onIsTemplateChange = viewModel::setIsTemplate
            )
            
            // Selected exercises section
            if (selectedExercises.isNotEmpty()) {
                SelectedExercisesSection(
                    selectedExercises = selectedExercises,
                    onRemoveExercise = viewModel::removeExercise
                )
            }
            
            // Exercise selection section
            ExerciseSelectionSection(
                exercises = exercises,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::searchExercises,
                onAddExercise = viewModel::addExercise,
                onFilterByCategory = viewModel::filterByCategory
            )
        }
    }
}

@Composable
private fun WorkoutDetailsSection(
    workoutName: String,
    onWorkoutNameChange: (String) -> Unit,
    isTemplate: Boolean,
    onIsTemplateChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Workout Details",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = workoutName,
                onValueChange = onWorkoutNameChange,
                label = { Text("Workout Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isTemplate,
                    onCheckedChange = onIsTemplateChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save as template",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SelectedExercisesSection(
    selectedExercises: List<Exercise>,
    onRemoveExercise: (Exercise) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Selected Exercises (${selectedExercises.size})",
                style = MaterialTheme.typography.titleMedium
            )
            
            selectedExercises.forEach { exercise ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${exercise.category} • ${exercise.muscleGroup}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onRemoveExercise(exercise) }
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Remove exercise",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (exercise != selectedExercises.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ExerciseSelectionSection(
    exercises: List<Exercise>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddExercise: (Exercise) -> Unit,
    onFilterByCategory: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Exercises",
                style = MaterialTheme.typography.titleMedium
            )
            
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search exercises...",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Category filters
            CategoryFilterRow(onFilterByCategory = onFilterByCategory)
            
            // Exercise list
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onAddExercise = { onAddExercise(exercise) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    onFilterByCategory: (String?) -> Unit
) {
    val categories = listOf("All", "Strength", "Cardio", "Flexibility", "Balance")
    var selectedCategory by remember { mutableStateOf("All") }
    
    LazyColumn(
        modifier = Modifier.height(48.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        onClick = {
                            selectedCategory = category
                            onFilterByCategory(if (category == "All") null else category)
                        },
                        label = { Text(category) },
                        selected = selectedCategory == category
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    exercise: Exercise,
    onAddExercise: () -> Unit
) {
    ExpandableCard(
        title = exercise.name,
        subtitle = "${exercise.category} • ${exercise.muscleGroup}",
        modifier = Modifier.fillMaxWidth(),
        actions = {
            IconButton(onClick = onAddExercise) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add exercise",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            exercise.equipment?.let { equipment ->
                Text(
                    text = "Equipment: $equipment",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            exercise.difficulty?.let { difficulty ->
                Text(
                    text = "Difficulty: $difficulty",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            exercise.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
