package com.mydashboardapp.inventory.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.mydashboardapp.core.navigation.MainDestinations
import com.mydashboardapp.core.navigation.NavigationApi
import javax.inject.Inject

/**
 * Inventory feature navigation implementation
 */
class InventoryNavigationApi @Inject constructor() : NavigationApi {
    
    override fun registerGraph(
        navController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        navGraphBuilder.navigation(
            startDestination = MainDestinations.Inventory.destination,
            route = MainDestinations.Inventory.route
        ) {
            composable(MainDestinations.Inventory.destination) {
                InventoryScreen()
            }
        }
    }
}

/**
 * Main inventory screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InventoryScreen() {
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Inventory",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(
                        onClick = { 
                            viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST 
                        }
                    ) {
                        Icon(
                            if (viewMode == ViewMode.LIST) Icons.Default.ViewModule else Icons.Default.ViewList,
                            contentDescription = "Toggle view"
                        )
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add item */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary cards
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(
                            title = "Total Items",
                            value = "156",
                            icon = Icons.Default.Inventory,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Low Stock",
                            value = "12",
                            icon = Icons.Default.Warning,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(
                            title = "Categories",
                            value = "8",
                            icon = Icons.Default.Category,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Total Value",
                            value = "$4.2K",
                            icon = Icons.Default.AttachMoney,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Filter chips
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier.height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("All", "Electronics", "Furniture", "Supplies", "Books", "Clothing")
                        items(filters.size) { index ->
                            val filter = filters[index]
                            FilterChip(
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                selected = selectedFilter == filter
                            )
                        }
                    }
                }
                
                // Items section
                item {
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
                                    "Items ($selectedFilter)",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                TextButton(onClick = { /* View all */ }) {
                                    Text("View All")
                                }
                            }
                            
                            if (viewMode == ViewMode.LIST) {
                                InventoryListView()
                            } else {
                                InventoryGridView()
                            }
                        }
                    }
                }
                
                // Recent activity
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Recent Activity",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            ActivityItem("Laptop Pro 13\" added", "2 hours ago", Icons.Default.Add)
                            ActivityItem("Office Chair stock updated", "4 hours ago", Icons.Default.Update)
                            ActivityItem("Bluetooth Headphones removed", "1 day ago", Icons.Default.Remove)
                        }
                    }
                }
            }
        }
    }
}

enum class ViewMode { LIST, GRID }

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InventoryListView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            InventoryListItem(
                name = "Sample Item ${index + 1}",
                quantity = "${10 + index * 5}",
                location = "Warehouse A${index + 1}",
                status = if (index == 0) "Low Stock" else "In Stock"
            )
        }
    }
}

@Composable
private fun InventoryGridView() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(4) { index ->
            InventoryGridItem(
                name = "Item ${index + 1}",
                quantity = "${10 + index * 5}",
                status = if (index == 0) "Low" else "OK"
            )
        }
    }
}

@Composable
private fun InventoryListItem(
    name: String,
    quantity: String,
    location: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$location • Qty: $quantity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Badge(
                containerColor = if (status == "Low Stock") 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = status,
                    color = if (status == "Low Stock") 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun InventoryGridItem(
    name: String,
    quantity: String,
    status: String
) {
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Qty: $quantity",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Badge(
                containerColor = if (status == "Low") 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = status,
                    color = if (status == "Low") 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(
    description: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
