package com.mydashboardapp.productivity.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mydashboardapp.service.TimeTrackerService
import com.mydashboardapp.service.TimeTrackingState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimeTrackerScreen() {
    val context = LocalContext.current
    var isServiceRunning by remember { mutableStateOf(false) }
    var currentTrackingState by remember { mutableStateOf(TimeTrackingState()) }
    var selectedActivity by remember { mutableStateOf("Work Session") }
    var selectedCategory by remember { mutableStateOf("Work") }
    
    // Mock data - in real implementation, this would come from ViewModel
    val recentSessions = remember {
        listOf(
            TimeLogDisplay("Coding", "Work", "2h 30m", "2 hours ago", 8),
            TimeLogDisplay("Meeting", "Work", "1h 15m", "4 hours ago", 6),
            TimeLogDisplay("Reading", "Learning", "45m", "Yesterday", 9),
            TimeLogDisplay("Exercise", "Health", "30m", "Yesterday", 7)
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Timer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = if (currentTrackingState.isTracking) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            } else {
                CardDefaults.cardColors()
            }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentTrackingState.isTracking) {
                    ActiveTimerDisplay(
                        state = currentTrackingState,
                        onPause = { 
                            // Pause timer
                        },
                        onStop = { 
                            // Stop timer
                            currentTrackingState = TimeTrackingState()
                            isServiceRunning = false
                        }
                    )
                } else {
                    TimerSetup(
                        selectedActivity = selectedActivity,
                        selectedCategory = selectedCategory,
                        onActivityChange = { selectedActivity = it },
                        onCategoryChange = { selectedCategory = it },
                        onStart = {
                            // Start time tracking
                            TimeTrackerService.startService(context)
                            currentTrackingState = TimeTrackingState(
                                isTracking = true,
                                startTime = System.currentTimeMillis(),
                                activity = selectedActivity
                            )
                            isServiceRunning = true
                        }
                    )
                }
            }
        }
        
        // Today's Summary
        TodaysSummaryCard()
        
        // Recent Sessions
        RecentSessionsCard(sessions = recentSessions)
    }
}

@Composable
private fun ActiveTimerDisplay(
    state: TimeTrackingState,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Activity name
        Text(
            text = state.activity,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        // Timer display
        val hours = state.elapsedSeconds / 3600
        val minutes = (state.elapsedSeconds % 3600) / 60
        val seconds = state.elapsedSeconds % 60
        
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (state.isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (state.isPaused) "Paused" else "Recording...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (state.isPaused) "Resume" else "Pause"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isPaused) "Resume" else "Pause")
            }
            
            OutlinedButton(
                onClick = onStop
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerSetup(
    selectedActivity: String,
    selectedCategory: String,
    onActivityChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onStart: () -> Unit
) {
    var showActivityDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Ready to track time?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Activity selection
        OutlinedTextField(
            value = selectedActivity,
            onValueChange = onActivityChange,
            label = { Text("Activity") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showActivityDialog = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Activity")
                }
            }
        )
        
        // Category selection
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Work", "Learning", "Health", "Personal", "Break").forEach { category ->
                FilterChip(
                    onClick = { onCategoryChange(category) },
                    label = { Text(category) },
                    selected = selectedCategory == category,
                    leadingIcon = {
                        Icon(
                            when (category) {
                                "Work" -> Icons.Default.Work
                                "Learning" -> Icons.Default.School
                                "Health" -> Icons.Default.FitnessCenter
                                "Personal" -> Icons.Default.Person
                                else -> Icons.Default.Coffee
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
        
        // Start button
        Button(
            onClick = onStart,
            modifier = Modifier.size(80.dp),
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Start",
                modifier = Modifier.size(32.dp)
            )
        }
    }
    
    if (showActivityDialog) {
        ActivitySelectionDialog(
            onDismiss = { showActivityDialog = false },
            onSelect = { activity ->
                onActivityChange(activity)
                showActivityDialog = false
            }
        )
    }
}

@Composable
private fun TodaysSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Total Time",
                    value = "6h 45m",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SummaryItem(
                    label = "Sessions",
                    value = "4",
                    icon = Icons.Default.PlayCircle,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                SummaryItem(
                    label = "Avg Focus",
                    value = "1h 41m",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentSessionsCard(sessions: List<TimeLogDisplay>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = { /* Show all sessions */ }) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            sessions.forEach { session ->
                SessionItem(session = session)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SessionItem(session: TimeLogDisplay) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Activity icon
        Icon(
            when (session.category) {
                "Work" -> Icons.Default.Work
                "Learning" -> Icons.Default.School
                "Health" -> Icons.Default.FitnessCenter
                "Personal" -> Icons.Default.Person
                else -> Icons.Default.Circle
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Activity details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = session.activity,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${session.category} • ${session.timeAgo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Duration and productivity
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = session.duration,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            if (session.productivity != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${session.productivity}/10",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivitySelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val commonActivities = listOf(
        "Coding", "Meeting", "Reading", "Writing", "Research",
        "Planning", "Email", "Design", "Testing", "Learning"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Activity") },
        text = {
            LazyColumn {
                items(commonActivities) { activity ->
                    TextButton(
                        onClick = { onSelect(activity) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activity,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class TimeLogDisplay(
    val activity: String,
    val category: String,
    val duration: String,
    val timeAgo: String,
    val productivity: Int?
)
