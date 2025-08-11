package com.mydashboardapp.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mydashboardapp.core.ui.theme.MyDashboardAppTheme
import java.time.LocalDate
import java.time.LocalTime

/**
 * Comprehensive showcase of all reusable UI components
 * Demonstrates Material 3 theming, accessibility features, and component interactions
 */
@Composable
fun ComponentsShowcase() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf("1", "3")) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(LocalTime.now()) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val sampleTags = listOf(
        Tag("1", "Productivity", Color.Blue, Icons.Default.Person),
        Tag("2", "Health", Color.Green, Icons.Default.LocalHospital),
        Tag("3", "Finance", Color.Red, Icons.Default.Business),
        Tag("4", "Education", Color.Yellow, Icons.Default.School),
        Tag("5", "Tech", Color.Magenta)
    )
    
    val sampleChartData = listOf(
        ChartData("Jan", 120f, Color.Blue),
        ChartData("Feb", 180f, Color.Green),
        ChartData("Mar", 90f, Color.Red),
        ChartData("Apr", 220f, Color.Yellow),
        ChartData("May", 160f, Color.Magenta)
    )
    
    val sampleSearchSuggestions = listOf(
        SearchSuggestion("Productivity Tips", "Improve your daily workflow"),
        SearchSuggestion("Time Management", "Master your schedule"),
        SearchSuggestion("Goal Setting", "Define and achieve objectives"),
        SearchSuggestion("Habit Tracking", "Build lasting routines")
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "UI Components Showcase",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Search Components
        item {
            ComponentSection("Search Components") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { /* Handle search */ },
                        placeholder = "Search productivity resources...",
                        suggestions = sampleSearchSuggestions,
                        recentSearches = listOf("habits", "goals", "time management")
                    )
                    
                    CompactSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { /* Handle search */ },
                        placeholder = "Quick search...",
                        isLoading = false
                    )
                }
            }
        }
        
        // Tag Components
        item {
            ComponentSection("Tag Components") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Selectable Tags:", style = MaterialTheme.typography.titleMedium)
                    TagChipGroup(
                        tags = sampleTags,
                        selectedTags = selectedTags,
                        onSelectionChanged = { selectedTags = it },
                        multiSelect = true
                    )
                    
                    Text("Flow Layout:", style = MaterialTheme.typography.titleMedium)
                    FlowTagChipGroup(
                        tags = sampleTags + sampleTags.map { it.copy(id = it.id + "_2") },
                        selectedTags = selectedTags,
                        onSelectionChanged = { selectedTags = it }
                    )
                }
            }
        }
        
        // Chart Components
        item {
            ComponentSection("Chart Components") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Bar Chart:", style = MaterialTheme.typography.titleMedium)
                    BarChart(
                        data = sampleChartData,
                        modifier = Modifier.height(200.dp)
                    )
                    
                    Text("Line Chart:", style = MaterialTheme.typography.titleMedium)
                    LineChart(
                        data = listOf(100f, 150f, 120f, 200f, 180f, 220f, 190f),
                        modifier = Modifier.height(150.dp)
                    )
                    
                    Text("Pie Chart:", style = MaterialTheme.typography.titleMedium)
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PieChart(
                            data = sampleChartData.take(4),
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
            }
        }
        
        // Expandable Components
        item {
            ComponentSection("Expandable Components") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpandableCard(
                        title = "Account Settings",
                        subtitle = "Manage your profile and preferences",
                        icon = Icons.Default.Settings
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Username: productivity_user")
                            Text("Email: user@example.com")
                            Text("Subscription: Premium")
                            Button(
                                onClick = { /* Handle click */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Edit Profile")
                            }
                        }
                    }
                    
                    AccordionExpandableCard(
                        title = "Advanced Features",
                        subtitle = "Pro features and integrations",
                        headerContent = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) { 
                                Text("PRO", color = MaterialTheme.colorScheme.onPrimary) 
                            }
                        }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("• Advanced Analytics")
                            Text("• API Integrations")
                            Text("• Custom Reports")
                            Text("• Priority Support")
                        }
                    }
                }
            }
        }
        
        // Date/Time Components
        item {
            ComponentSection("Date & Time Components") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Individual Pickers:", style = MaterialTheme.typography.titleMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DatePickerField(
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it },
                            label = "Event Date",
                            modifier = Modifier.weight(1f)
                        )
                        
                        TimePickerField(
                            selectedTime = selectedTime,
                            onTimeSelected = { selectedTime = it },
                            label = "Event Time",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text("Combined Picker:", style = MaterialTheme.typography.titleMedium)
                    DateTimePickerField(
                        selectedDate = selectedDate,
                        selectedTime = selectedTime,
                        onDateSelected = { selectedDate = it },
                        onTimeSelected = { selectedTime = it }
                    )
                    
                    Text("Date Range:", style = MaterialTheme.typography.titleMedium)
                    DateRangePicker(
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateSelected = { startDate = it },
                        onEndDateSelected = { endDate = it }
                    )
                }
            }
        }
        
        // Theme Information
        item {
            ComponentSection("Theme Features") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✅ Material 3 Dynamic Colors")
                    Text("✅ Light/Dark Theme Support")
                    Text("✅ Accessibility Features")
                    Text("✅ Content Descriptions")
                    Text("✅ TalkBack Support")
                    Text("✅ Font Scaling")
                    Text("✅ High Contrast Colors")
                    Text("✅ Semantic Properties")
                    Text("✅ Haptic Feedback")
                }
            }
        }
    }
}

@Composable
private fun ComponentSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ComponentsShowcasePreview() {
    MyDashboardAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ComponentsShowcase()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ComponentsShowcaseDarkPreview() {
    MyDashboardAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ComponentsShowcase()
        }
    }
}
