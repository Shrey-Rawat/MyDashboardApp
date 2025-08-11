package com.mydashboardapp.productivity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mydashboardapp.data.entities.PomodoroType

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pomodoro Timer Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Session Type Selection
                if (uiState.currentSession == null) {
                    PomodoroTypeSelector(
                        selectedType = uiState.selectedType,
                        onTypeSelected = viewModel::onSessionTypeChanged
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Duration Selector
                    DurationSelector(
                        duration = uiState.customDuration,
                        onDurationChange = viewModel::onDurationChanged,
                        sessionType = uiState.selectedType
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Timer Display
                if (uiState.currentSession != null) {
                    PomodoroTimer(
                        session = uiState.currentSession,
                        onToggleTimer = viewModel::toggleTimer,
                        onSessionCancel = viewModel::cancelSession
                    )
                } else {
                    // Start Button
                    Button(
                        onClick = viewModel::startSession,
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "START",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Pomodoro Statistics
        PomodoroStatsCard(
            stats = uiState.todayStats
        )
        
        // Recent Sessions
        RecentSessionsCard(
            sessions = uiState.recentSessions
        )
    }
}

@Composable
private fun PomodoroTypeSelector(
    selectedType: PomodoroType,
    onTypeSelected: (PomodoroType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PomodoroType.values().forEach { type ->
            FilterChip(
                onClick = { onTypeSelected(type) },
                label = { 
                    Text(
                        text = when (type) {
                            PomodoroType.WORK -> "Work"
                            PomodoroType.SHORT_BREAK -> "Short Break"
                            PomodoroType.LONG_BREAK -> "Long Break"
                        }
                    )
                },
                selected = selectedType == type,
                leadingIcon = {
                    Icon(
                        imageVector = when (type) {
                            PomodoroType.WORK -> Icons.Default.Work
                            PomodoroType.SHORT_BREAK -> Icons.Default.Coffee
                            PomodoroType.LONG_BREAK -> Icons.Default.Weekend
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun DurationSelector(
    duration: Int,
    onDurationChange: (Int) -> Unit,
    sessionType: PomodoroType
) {
    val defaultDuration = when (sessionType) {
        PomodoroType.WORK -> 25
        PomodoroType.SHORT_BREAK -> 5
        PomodoroType.LONG_BREAK -> 15
    }
    
    LaunchedEffect(sessionType) {
        onDurationChange(defaultDuration)
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Duration: $duration minutes",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Slider(
            value = duration.toFloat(),
            onValueChange = { onDurationChange(it.toInt()) },
            valueRange = 1f..60f,
            steps = 59,
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
private fun PomodoroTimer(
    session: PomodoroSessionState,
    onToggleTimer: () -> Unit,
    onSessionCancel: () -> Unit
) {
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Timer
        Box(
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                val progress = 1f - (session.remainingSeconds.toFloat() / (session.totalDurationMinutes * 60))
                val strokeWidth = 12.dp.toPx()
                
                // Background circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.3f),
                    style = Stroke(width = strokeWidth)
                )
                
                // Progress arc
                drawArc(
                    color = when (session.type) {
                        PomodoroType.WORK -> Color.Red
                        PomodoroType.SHORT_BREAK -> Color.Green
                        PomodoroType.LONG_BREAK -> Color.Blue
                    },
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // Time Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val minutes = session.remainingSeconds / 60
                val seconds = session.remainingSeconds % 60
                
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = when (session.type) {
                        PomodoroType.WORK -> "Work Time"
                        PomodoroType.SHORT_BREAK -> "Short Break"
                        PomodoroType.LONG_BREAK -> "Long Break"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onToggleTimer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (session.isRunning) MaterialTheme.colorScheme.secondary 
                                   else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (session.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (session.isRunning) "Pause" else "Start"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (session.isRunning) "Pause" else "Start")
            }
            
            OutlinedButton(
                onClick = onSessionCancel
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Cancel")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun PomodoroStatsCard(
    stats: PomodoroStats
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today's Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Completed",
                    value = "4",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    label = "Focus Time",
                    value = "1h 40m",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                StatItem(
                    label = "Streak",
                    value = "7 days",
                    icon = Icons.Default.LocalFire,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentSessionsCard() {
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sample sessions
            repeat(3) { index ->
                SessionItem(
                    type = if (index % 2 == 0) PomodoroType.WORK else PomodoroType.SHORT_BREAK,
                    duration = if (index % 2 == 0) "25 min" else "5 min",
                    timeAgo = "${index + 1}h ago",
                    completed = index != 2
                )
                
                if (index < 2) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionItem(
    type: PomodoroType,
    duration: String,
    timeAgo: String,
    completed: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (type) {
                PomodoroType.WORK -> Icons.Default.Work
                PomodoroType.SHORT_BREAK -> Icons.Default.Coffee
                PomodoroType.LONG_BREAK -> Icons.Default.Weekend
            },
            contentDescription = null,
            tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = when (type) {
                    PomodoroType.WORK -> "Work Session"
                    PomodoroType.SHORT_BREAK -> "Short Break"
                    PomodoroType.LONG_BREAK -> "Long Break"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$duration • $timeAgo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (completed) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                Icons.Default.Cancel,
                contentDescription = "Cancelled",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

data class PomodoroSessionState(
    val type: PomodoroType,
    val totalDurationMinutes: Int,
    val remainingSeconds: Int
)
