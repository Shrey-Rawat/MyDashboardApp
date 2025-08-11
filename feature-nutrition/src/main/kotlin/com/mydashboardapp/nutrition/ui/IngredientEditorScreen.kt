package com.mydashboardapp.nutrition.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mydashboardapp.data.entities.Ingredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditorScreen(
    modifier: Modifier = Modifier,
    ingredientId: Long? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: IngredientEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Load ingredient for editing if ID is provided
    LaunchedEffect(ingredientId) {
        ingredientId?.let { id ->
            if (id > 0) {
                viewModel.loadIngredient(id)
            }
        }
    }
    
    // Handle successful save
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (ingredientId != null && ingredientId > 0) "Edit Ingredient" else "Create Ingredient",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Basic Information Card
        IngredientBasicInfoCard(
            name = uiState.name,
            category = uiState.category,
            onNameChange = viewModel::updateName,
            onCategoryChange = viewModel::updateCategory,
            categories = uiState.availableCategories
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nutrition Information Card
        IngredientNutritionCard(
            caloriesPerGram = uiState.caloriesPerGram,
            proteinPerGram = uiState.proteinPerGram,
            carbsPerGram = uiState.carbsPerGram,
            fatPerGram = uiState.fatPerGram,
            fiberPerGram = uiState.fiberPerGram,
            sugarPerGram = uiState.sugarPerGram,
            sodiumPerGram = uiState.sodiumPerGram,
            onCaloriesChange = viewModel::updateCaloriesPerGram,
            onProteinChange = viewModel::updateProteinPerGram,
            onCarbsChange = viewModel::updateCarbsPerGram,
            onFatChange = viewModel::updateFatPerGram,
            onFiberChange = viewModel::updateFiberPerGram,
            onSugarChange = viewModel::updateSugarPerGram,
            onSodiumChange = viewModel::updateSodiumPerGram
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Allergen Information Card
        IngredientAllergenCard(
            isAllergen = uiState.isAllergen,
            allergenInfo = uiState.allergenInfo,
            onIsAllergenChange = viewModel::updateIsAllergen,
            onAllergenInfoChange = viewModel::updateAllergenInfo
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = viewModel::saveIngredient,
                modifier = Modifier.weight(1f),
                enabled = uiState.isValid && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save Ingredient")
            }
        }
        
        // Error message
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
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
        
        // Add some bottom padding
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientBasicInfoCard(
    name: String,
    category: String,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    categories: List<String>
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ingredient Name *") },
                placeholder = { Text("e.g., Organic Chicken Breast") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null
                    )
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = onCategoryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Category") },
                    placeholder = { Text("Select or type category") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                    },
                    singleLine = true
                )
                
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    categories.forEach { categoryOption ->
                        DropdownMenuItem(
                            text = { Text(categoryOption) },
                            onClick = {
                                onCategoryChange(categoryOption)
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientNutritionCard(
    caloriesPerGram: String,
    proteinPerGram: String,
    carbsPerGram: String,
    fatPerGram: String,
    fiberPerGram: String,
    sugarPerGram: String,
    sodiumPerGram: String,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onFiberChange: (String) -> Unit,
    onSugarChange: (String) -> Unit,
    onSodiumChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nutrition Information (per gram)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter nutritional values per 1 gram of this ingredient",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Macronutrients
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = caloriesPerGram,
                    onValueChange = onCaloriesChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Calories *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = proteinPerGram,
                    onValueChange = onProteinChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Protein (g) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = carbsPerGram,
                    onValueChange = onCarbsChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Carbs (g) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = fatPerGram,
                    onValueChange = onFatChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Fat (g) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Optional Nutrients",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Optional nutrients
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fiberPerGram,
                    onValueChange = onFiberChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Fiber (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = sugarPerGram,
                    onValueChange = onSugarChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Sugar (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = sodiumPerGram,
                onValueChange = onSodiumChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Sodium (mg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientAllergenCard(
    isAllergen: Boolean,
    allergenInfo: String,
    onIsAllergenChange: (Boolean) -> Unit,
    onAllergenInfoChange: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Allergen Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isAllergen,
                    onCheckedChange = onIsAllergenChange
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "This ingredient contains allergens",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (isAllergen) {
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = allergenInfo,
                    onValueChange = onAllergenInfoChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Allergen Information") },
                    placeholder = { Text("e.g., Contains gluten, dairy, nuts") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    maxLines = 3
                )
            }
        }
    }
}
