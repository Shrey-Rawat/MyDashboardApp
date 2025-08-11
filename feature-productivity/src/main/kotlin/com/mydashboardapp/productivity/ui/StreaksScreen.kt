package com.mydashboardapp.productivity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mydashboardapp.data.entities.StreakFrequency
import com.mydashboardapp.data.entities.StreakType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StreaksScreen() {
    var showAddStreakDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with stats
        StreakOverviewCard()
        
        // Active Streaks
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
                        text = "Active Streaks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { showAddStreakDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Streak")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sample streaks
                repeat(3) { index ->
                    StreakItem(
                        name = when (index) {
                            0 -> "Daily Exercise"
                            1 -> "Read for 30 minutes"
                            else -> "Drink 8 glasses of water"
                        },
                        currentStreak = listOf(23, 7, 45)[index],
                        longestStreak = listOf(45, 12, 67)[index],
                        frequency = when (index) {
                            0 -> StreakFrequency.DAILY
                            1 -> StreakFrequency.DAILY
                            else -> StreakFrequency.DAILY
                        },
                        completedToday = index != 2,
                        onMarkComplete = { /* Mark complete */ },
                        lastSevenDays = generateSampleWeekData(index)
                    )
                    
                    if (index < 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
        
        // Streak Categories
        StreakCategoriesCard()
    }
    
    if (showAddStreakDialog) {
        AddStreakDialog(
            onDismiss = { showAddStreakDialog = false },
            onConfirm = { name, type, frequency ->
                // Add new streak
                showAddStreakDialog = false
            }
        )
    }
}

@Composable
private fun StreakOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Streak Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Keep up the great work!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                Icon(
                    Icons.Default.LocalFire,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewStat(
                    label = "Active Streaks",
                    value = "3",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                OverviewStat(
                    label = "Longest Streak",
                    value = "67 days",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                OverviewStat(
                    label = "Today's Progress",
                    value = "2/3",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun OverviewStat(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun StreakItem(
    name: String,
    currentStreak: Int,
    longestStreak: Int,
    frequency: StreakFrequency,
    completedToday: Boolean,
    onMarkComplete: () -> Unit,
    lastSevenDays: List<Boolean>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak icon and info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Current: $currentStreak days • Best: $longestStreak days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Complete button
            IconButton(
                onClick = onMarkComplete,
                enabled = !completedToday
            ) {
                Icon(
                    if (completedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (completedToday) "Completed" else "Mark Complete",
                    tint = if (completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 7-day streak visualization
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last 7 days:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            lastSevenDays.forEach { completed ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (completed) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun StreakCategoriesCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    listOf(
                        "Health" to Icons.Default.FitnessCenter,
                        "Learning" to Icons.Default.School,
                        "Productivity" to Icons.Default.Work,
                        "Habits" to Icons.Default.Star,
                        "Social" to Icons.Default.People
                    )
                ) { (category, icon) ->
                    CategoryChip(
                        category = category,
                        icon = icon,
                        streakCount = (1..5).random(),
                        onClick = { /* Navigate to category */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    streakCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                text = "$streakCount streaks",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStreakDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, StreakType, StreakFrequency) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StreakType.HABIT) }
    var selectedFrequency by remember { mutableStateOf(StreakFrequency.DAILY) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Streak") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Streak Name") },
                    placeholder = { Text("e.g., Daily Exercise") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StreakType.values().forEach { type ->
                            FilterChip(
                                onClick = { selectedType = type },
                                label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                selected = selectedType == type
                            )
                        }
                    }
                }
                
                Column {
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StreakFrequency.values().forEach { frequency ->
                            FilterChip(
                                onClick = { selectedFrequency = frequency },
                                label = { Text(frequency.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                selected = selectedFrequency == frequency
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedType, selectedFrequency)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun generateSampleWeekData(index: Int): List<Boolean> {
    return when (index) {
        0 -> listOf(true, true, true, false, true, true, true)
        1 -> listOf(true, false, true, true, false, true, false)
        else -> listOf(false, true, true, true, true, false, false)
    }
}
