package com.mydashboardapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation items configuration
 */
data class BottomNavItem(
    val destination: MainDestinations,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
)

/**
 * Bottom navigation items list
 */
val bottomNavItems = listOf(
    BottomNavItem(
        destination = MainDestinations.Nutrition,
        icon = Icons.Default.Fastfood,
        label = "Nutrition",
        contentDescription = "Navigate to Nutrition"
    ),
    BottomNavItem(
        destination = MainDestinations.Training,
        icon = Icons.Default.FitnessCenter,
        label = "Training", 
        contentDescription = "Navigate to Training"
    ),
    BottomNavItem(
        destination = MainDestinations.Productivity,
        icon = Icons.Default.CheckCircle,
        label = "Tasks",
        contentDescription = "Navigate to Productivity"
    ),
    BottomNavItem(
        destination = MainDestinations.Finance,
        icon = Icons.Default.AttachMoney,
        label = "Finance",
        contentDescription = "Navigate to Finance"
    ),
    BottomNavItem(
        destination = MainDestinations.Inventory,
        icon = Icons.Default.Inventory,
        label = "Inventory", 
        contentDescription = "Navigate to Inventory"
    ),
    BottomNavItem(
        destination = MainDestinations.AI,
        icon = Icons.Default.SmartToy,
        label = "AI",
        contentDescription = "Navigate to AI Assistant"
    )
)

/**
 * Main bottom navigation bar component
 */
@Composable
fun MainBottomNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { destination ->
                destination.route == item.destination.route
            } == true
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription
                    ) 
                },
                label = { 
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    ) 
                },
                selected = selected,
                onClick = {
                    navController.navigateToFeature(item.destination)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
