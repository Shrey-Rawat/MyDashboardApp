package com.mydashboardapp.nutrition.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mydashboardapp.data.entities.Food
import com.mydashboardapp.data.entities.Meal
import com.mydashboardapp.data.entities.MealFoodCrossRef
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLoggingScreen(
    modifier: Modifier = Modifier,
    viewModel: MealLoggingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with current meal info
        MealHeader(
            currentMeal = uiState.currentMeal,
            totalMacros = uiState.calculatedMacros,
            onMealTypeClick = viewModel::showMealTypeSelector
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add food section
        AddFoodSection(
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            selectedFoods = uiState.selectedFoods,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onFoodSelected = viewModel::addFoodToMeal,
            onSearchFoods = viewModel::searchFoods,
            isLoading = uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selected foods list
        SelectedFoodsList(
            selectedFoods = uiState.selectedFoods,
            onQuantityChange = viewModel::updateFoodQuantity,
            onRemoveFood = viewModel::removeFoodFromMeal
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save meal button
        Button(
            onClick = viewModel::saveMeal,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedFoods.isNotEmpty() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save Meal")
        }
        
        // Error message
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
    
    // Meal type selector dialog
    AnimatedVisibility(visible = uiState.showMealTypeSelector) {
        MealTypeDialog(
            selectedType = uiState.currentMeal?.mealType ?: "Breakfast",
            onTypeSelected = viewModel::selectMealType,
            onDismiss = viewModel::hideMealTypeSelector
        )
    }
}

@Composable
private fun MealHeader(
    currentMeal: Meal?,
    totalMacros: CalculatedMacros,
    onMealTypeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentMeal?.mealType ?: "New Meal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(currentMeal?.dateConsumed ?: System.currentTimeMillis())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(onClick = onMealTypeClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change meal type"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Macro summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem(
                    label = "Calories",
                    value = totalMacros.totalCalories.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                MacroItem(
                    label = "Protein",
                    value = "${totalMacros.totalProtein.toInt()}g",
                    color = MaterialTheme.colorScheme.secondary
                )
                MacroItem(
                    label = "Carbs",
                    value = "${totalMacros.totalCarbs.toInt()}g",
                    color = MaterialTheme.colorScheme.tertiary
                )
                MacroItem(
                    label = "Fat",
                    value = "${totalMacros.totalFat.toInt()}g",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun MacroItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFoodSection(
    searchQuery: String,
    searchResults: List<Food>,
    selectedFoods: List<SelectedFoodItem>,
    onSearchQueryChange: (String) -> Unit,
    onFoodSelected: (Food) -> Unit,
    onSearchFoods: () -> Unit,
    isLoading: Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Add Foods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search foods...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search results
            if (searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(searchResults) { food ->
                        FoodSearchResultItem(
                            food = food,
                            onSelect = { onFoodSelected(food) },
                            isSelected = selectedFoods.any { it.food.id == food.id }
                        )
                    }
                }
            } else if (searchQuery.isNotEmpty() && !isLoading) {
                Text(
                    text = "No foods found",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (isLoading && searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun FoodSearchResultItem(
    food: Food,
    onSelect: () -> Unit,
    isSelected: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                food.brand?.let { brand ->
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Text(
                text = "${food.caloriesPerServing} cal",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedFoodsList(
    selectedFoods: List<SelectedFoodItem>,
    onQuantityChange: (Food, Double) -> Unit,
    onRemoveFood: (Food) -> Unit
) {
    if (selectedFoods.isEmpty()) return
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Selected Foods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            selectedFoods.forEach { selectedFood ->
                SelectedFoodItem(
                    selectedFood = selectedFood,
                    onQuantityChange = { quantity -> onQuantityChange(selectedFood.food, quantity) },
                    onRemove = { onRemoveFood(selectedFood.food) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedFoodItem(
    selectedFood: SelectedFoodItem,
    onQuantityChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var quantityText by remember { mutableStateOf(selectedFood.quantity.toString()) }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedFood.food.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    selectedFood.food.brand?.let { brand ->
                        Text(
                            text = brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove food",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { newValue ->
                        quantityText = newValue
                        newValue.toDoubleOrNull()?.let { quantity ->
                            if (quantity > 0) {
                                onQuantityChange(quantity)
                            }
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    label = { Text("Servings", fontSize = 12.sp) },
                    singleLine = true
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${selectedFood.totalCalories} cal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "P: ${selectedFood.totalProtein.toInt()}g | C: ${selectedFood.totalCarbs.toInt()}g | F: ${selectedFood.totalFat.toInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MealTypeDialog(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Meal Type") },
        text = {
            LazyColumn {
                items(mealTypes) { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == selectedType,
                            onClick = { onTypeSelected(type) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// Data classes for UI state
data class SelectedFoodItem(
    val food: Food,
    val quantity: Double = 1.0
) {
    val totalCalories: Int get() = (food.caloriesPerServing * quantity).toInt()
    val totalProtein: Double get() = food.proteinPerServing * quantity
    val totalCarbs: Double get() = food.carbsPerServing * quantity
    val totalFat: Double get() = food.fatPerServing * quantity
    val totalFiber: Double? get() = food.fiberPerServing?.let { it * quantity }
    val totalSugar: Double? get() = food.sugarPerServing?.let { it * quantity }
    val totalSodium: Double? get() = food.sodiumPerServing?.let { it * quantity }
}

data class CalculatedMacros(
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalSugar: Double = 0.0,
    val totalSodium: Double = 0.0
)
