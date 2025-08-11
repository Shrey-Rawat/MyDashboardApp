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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mydashboardapp.data.entities.Workout
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingHomeScreen(
    onNavigateToPlanner: () -> Unit = {},
    onNavigateToLiveSession: (Long) -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsState()
    val workoutTemplates by viewModel.workoutTemplates.collectAsState()
    val todayWorkouts by viewModel.todayWorkouts.collectAsState()
    val weeklyWorkouts by viewModel.weeklyWorkouts.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Training",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.Analytics, contentDescription = "Analytics")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToPlanner,
                icon = { Icon(Icons.Default.Add, contentDescription = "Create workout") },
                text = { Text("Plan Workout") }
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
            // Quick stats cards
            item {
                QuickStatsSection(
                    todayWorkouts = todayWorkouts,
                    weeklyWorkouts = weeklyWorkouts
                )
            }
            
            // Quick actions
            item {
                QuickActionsSection(
                    onStartQuickWorkout = { viewModel.startWorkout() },
                    onNavigateToPlanner = onNavigateToPlanner,
                    onNavigateToAnalytics = onNavigateToAnalytics
                )
            }
            
            // Workout templates
            if (workoutTemplates.isNotEmpty()) {
                item {
                    WorkoutTemplatesSection(
                        templates = workoutTemplates,
                        onStartFromTemplate = { templateId ->
                            viewModel.startWorkout(templateId)
                            // Navigation would be handled by viewModel callback
                        }
                    )
                }
            }
            
            // Recent workouts
            item {
                RecentWorkoutsSection(
                    workouts = workouts.take(5),
                    onContinueWorkout = onNavigateToLiveSession,
                    onViewAllWorkouts = { /* Navigate to workout history */ }
                )
            }
        }
    }
}

@Composable
private fun QuickStatsSection(
    todayWorkouts: List<Workout>,
    weeklyWorkouts: List<Workout>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Today",
                    value = todayWorkouts.size.toString(),
                    icon = Icons.Default.Today,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "This Week",
                    value = weeklyWorkouts.size.toString(),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Time",
                    value = "${todayWorkouts.sumOf { it.duration ?: 0 }}m",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onStartQuickWorkout: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStartQuickWorkout,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Quick Start")
                    }
                }
                
                OutlinedButton(
                    onClick = onNavigateToPlanner,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Plan Workout")
                    }
                }
                
                OutlinedButton(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Progress")
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutTemplatesSection(
    templates: List<Workout>,
    onStartFromTemplate: (Long) -> Unit
) {
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
                    text = "Workout Templates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = "Templates",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onStartFromTemplate = { onStartFromTemplate(template.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: Workout,
    onStartFromTemplate: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.width(160.dp),
        onClick = onStartFromTemplate
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = template.name,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            template.workoutType?.let { type ->
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentWorkoutsSection(
    workouts: List<Workout>,
    onContinueWorkout: (Long) -> Unit,
    onViewAllWorkouts: () -> Unit
) {
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
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onViewAllWorkouts) {
                    Text("View All")
                }
            }
            
            if (workouts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "No workouts",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No workouts yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start your fitness journey by creating your first workout!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                workouts.forEach { workout ->
                    WorkoutItem(
                        workout = workout,
                        onContinueWorkout = { onContinueWorkout(workout.id) }
                    )
                    if (workout != workouts.last()) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutItem(
    workout: Workout,
    onContinueWorkout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(workout.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                workout.duration?.let { duration ->
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${duration}min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                workout.workoutType?.let { type ->
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Show continue button for incomplete workouts
        if (workout.endTime == null) {
            FilledTonalButton(
                onClick = onContinueWorkout
            ) {
                Text("Continue")
            }
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                style = MaterialTheme.typography.titleLarge,
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
}
