package com.mydashboardapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mydashboardapp.core.navigation.*

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            // Only show bottom navigation when not on welcome screen
            if (currentDestination?.route != "welcome") {
                MainBottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Welcome/Home screen
            composable("welcome") {
                WelcomeScreen(
                    onNavigateToFeature = { destination ->
                        navController.navigateToFeature(destination)
                    }
                )
            }
            
            // Feature screens - these would be registered by feature modules
            // For now, we'll add placeholder screens
            composable(MainDestinations.Nutrition.route) {
                PlaceholderFeatureScreen("Nutrition")
            }
            
            composable(MainDestinations.Training.route) {
                PlaceholderFeatureScreen("Training")
            }
            
            composable(MainDestinations.Productivity.route) {
                PlaceholderFeatureScreen("Productivity")
            }
            
            composable(MainDestinations.Finance.route) {
                PlaceholderFeatureScreen("Finance")
            }
            
            composable(MainDestinations.Inventory.route) {
                PlaceholderFeatureScreen("Inventory")
            }
            
            composable(MainDestinations.AI.route) {
                PlaceholderFeatureScreen("AI Assistant")
            }
        }
    }
}

@Composable
private fun WelcomeScreen(
    onNavigateToFeature: (MainDestinations) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Best Productivity App",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to the Best Productivity App!",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This app helps you manage your daily tasks, track your nutrition, monitor your finances, and improve your productivity with AI-powered insights.",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Available Features:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                FeatureItem("• Smart Task Management", "Organize and prioritize your daily tasks")
                FeatureItem("• Nutrition Tracking", "Monitor your dietary intake and health goals")
                FeatureItem("• Training Programs", "Custom workouts and fitness tracking")
                FeatureItem("• Financial Management", "Budget tracking and expense monitoring")
                FeatureItem("• Inventory Management", "Track your belongings and assets")
                FeatureItem("• AI-Powered Insights", "Get intelligent recommendations and analysis")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick access buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onNavigateToFeature(MainDestinations.Productivity) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Tasks")
            }
            
            Button(
                onClick = { onNavigateToFeature(MainDestinations.AI) },
                modifier = Modifier.weight(1f)
            ) {
                Text("AI Assistant")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { onNavigateToFeature(MainDestinations.Finance) },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Explore All Features")
        }
    }
}

@Composable
private fun FeatureItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun PlaceholderFeatureScreen(featureName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$featureName Feature",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This feature is coming soon!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Use the navigation bar below to explore other features.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
