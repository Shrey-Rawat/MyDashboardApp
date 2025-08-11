package com.mydashboardapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * Navigation API interface for feature modules to provide their navigation graphs.
 * This interface enables decoupling between the main app module and feature modules.
 */
interface NavigationApi {
    /**
     * Register the navigation graph for this feature module
     * @param navController The NavHostController from the main app
     * @param navGraphBuilder The NavGraphBuilder to add routes to
     */
    fun registerGraph(
        navController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    )
}

/**
 * Navigation destination interface for consistent navigation handling
 */
interface NavigationDestination {
    val route: String
    val destination: String
}

/**
 * Main navigation destinations for bottom navigation
 */
sealed class MainDestinations(
    override val route: String,
    override val destination: String
) : NavigationDestination {
    object Nutrition : MainDestinations("nutrition", "nutrition_main")
    object Training : MainDestinations("training", "training_main") 
    object Productivity : MainDestinations("productivity", "productivity_main")
    object Finance : MainDestinations("finance", "finance_main")
    object Inventory : MainDestinations("inventory", "inventory_main")
    object AI : MainDestinations("ai", "ai_main")
}

/**
 * Navigation extensions for feature modules
 */
fun NavHostController.navigateToFeature(destination: NavigationDestination) {
    navigate(destination.route) {
        // Clear back stack to avoid building up a large stack
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navigation extensions for single destination navigation
 */
fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
