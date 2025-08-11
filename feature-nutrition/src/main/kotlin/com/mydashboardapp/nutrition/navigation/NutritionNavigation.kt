package com.mydashboardapp.nutrition.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mydashboardapp.core.navigation.MainDestinations
import com.mydashboardapp.core.navigation.NavigationApi
import com.mydashboardapp.nutrition.ui.NutritionViewModel
import javax.inject.Inject

/**
 * Nutrition feature navigation implementation
 */
class NutritionNavigationApi @Inject constructor() : NavigationApi {
    
    override fun registerGraph(
        navController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.navigation(
            startDestination = MainDestinations.Nutrition.destination,
            route = MainDestinations.Nutrition.route
        ) {
            composable(MainDestinations.Nutrition.destination) {
                NutritionScreen()
            }
            
            // Additional nutrition screens can be added here
            composable("nutrition_add") {
                NutritionAddScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Main nutrition screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NutritionScreen(
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Nutrition",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState.canAddMoreEntries) {
                FloatingActionButton(
                    onClick = { /* Navigate to add screen */ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add nutrition entry")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.isEmpty -> {
                    EmptyNutritionState(
                        modifier = Modifier.align(Alignment.Center),
                        canAddEntries = uiState.canAddMoreEntries
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Foods section
                        if (uiState.foods.isNotEmpty()) {
                            item {
                                Text(
                                    "Foods",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(uiState.foods) { food ->
                                NutritionItemCard(
                                    title = food.name,
                                    subtitle = "${food.calories} calories",
                                    details = "Protein: ${food.protein}g, Carbs: ${food.carbohydrates}g, Fat: ${food.fat}g"
                                )
                            }
                        }
                        
                        // Meals section
                        if (uiState.meals.isNotEmpty()) {
                            item {
                                Text(
                                    "Meals",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(uiState.meals) { meal ->
                                NutritionItemCard(
                                    title = meal.name,
                                    subtitle = "${meal.totalCalories} total calories",
                                    details = "Items: ${meal.foods.size}"
                                )
                            }
                        }
                    }
                }
            }
            
            // Error message
            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

/**
 * Nutrition item card component
 */
@Composable
private fun NutritionItemCard(
    title: String,
    subtitle: String,
    details: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state for nutrition screen
 */
@Composable
private fun EmptyNutritionState(
    canAddEntries: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No nutrition data yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (canAddEntries) {
                "Start tracking your nutrition by adding your first meal or food item."
            } else {
                "You've reached the maximum entries for the free version. Upgrade to Pro for unlimited tracking."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (canAddEntries) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Navigate to add screen */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Entry")
            }
        }
    }
}

/**
 * Add nutrition screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NutritionAddScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Nutrition Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Add nutrition entry screen - To be implemented",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
