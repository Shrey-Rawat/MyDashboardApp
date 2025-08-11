# Adding New Feature Modules

This guide provides step-by-step instructions for adding new feature modules to the Best Productivity App. Feature modules allow you to add functionality while maintaining the project's modular architecture and build performance.

## Overview

Feature modules in this project follow a consistent structure and configuration using Gradle convention plugins. Each feature module:
- Has its own UI screens and ViewModels
- Depends on `core` and `data` modules
- Cannot depend on other feature modules directly
- Uses Hilt for dependency injection
- Follows MVVM architecture with Jetpack Compose

## Prerequisites

Before adding a new feature module, ensure you:
- Understand the project's [multi-module architecture](architecture-decisions/adr-0001-multi-module-architecture.md)
- Have reviewed existing feature modules for patterns
- Created a [feature design document](feature-designs/README.md) if this is a major feature

## Step-by-Step Guide

### Step 1: Create Module Directory

Create the module directory structure:
```bash
mkdir feature-[your-feature-name]
mkdir -p feature-[your-feature-name]/src/main/kotlin/com/mydashboardapp/[feature]/
mkdir -p feature-[your-feature-name]/src/test/kotlin/com/mydashboardapp/[feature]/
mkdir -p feature-[your-feature-name]/src/androidTest/kotlin/com/mydashboardapp/[feature]/
```

Example for a "habits" feature:
```bash
mkdir feature-habits
mkdir -p feature-habits/src/main/kotlin/com/mydashboardapp/habits/
mkdir -p feature-habits/src/test/kotlin/com/mydashboardapp/habits/
mkdir -p feature-habits/src/androidTest/kotlin/com/mydashboardapp/habits/
```

### Step 2: Create build.gradle.kts

Create `feature-[name]/build.gradle.kts` using the feature convention plugin:

```kotlin
plugins {
    id("mydashboardapp.android.feature")
    id("mydashboardapp.android.library.compose")
}

android {
    namespace = "com.mydashboardapp.[feature]"
    
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // Add feature-specific dependencies here
    // Common dependencies are included via convention plugin
}
```

### Step 3: Add to Project Settings

Add your module to `settings.gradle.kts`:
```kotlin
include(":feature-[your-feature-name]")
```

And to the main `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(projects.feature[YourFeatureName])
    // ... other dependencies
}
```

### Step 4: Create Package Structure

Create the standard package structure in your feature module:

```
feature-[name]/src/main/kotlin/com/mydashboardapp/[feature]/
├── di/                    # Dependency injection module
├── navigation/            # Navigation definitions  
├── ui/                   # UI components and screens
│   ├── components/       # Reusable UI components
│   ├── screen1/         # Individual screens
│   ├── screen2/
│   └── theme/           # Feature-specific theme extensions (if needed)
├── domain/              # Business logic (if complex)
│   ├── model/
│   ├── repository/
│   └── usecase/
└── data/                # Data layer (if needed, prefer using main data module)
```

### Step 5: Create Navigation Module

Create navigation definitions in `navigation/[Feature]Navigation.kt`:

```kotlin
package com.mydashboardapp.[feature].navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mydashboardapp.[feature].ui.[FeatureScreen]

const val [FEATURE]_ROUTE = "[feature]"

fun NavController.navigateTo[Feature]() {
    this.navigate([FEATURE]_ROUTE)
}

fun NavGraphBuilder.[feature]Screen(
    onNavigateUp: () -> Unit,
    onNavigateToOtherFeature: () -> Unit = {}
) {
    composable(route = [FEATURE]_ROUTE) {
        [FeatureScreen](
            onNavigateUp = onNavigateUp,
            onNavigateToOtherFeature = onNavigateToOtherFeature
        )
    }
}
```

### Step 6: Create Hilt Module

Create dependency injection module in `di/[Feature]Module.kt`:

```kotlin
package com.mydashboardapp.[feature].di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object [Feature]Module {
    
    // Add your feature-specific dependencies here
    @Provides
    fun provide[FeatureDependency](): [FeatureDependency] {
        return [FeatureDependency]Impl()
    }
}
```

### Step 7: Create UI Screens

Create your main screen composable in `ui/[Feature]Screen.kt`:

```kotlin
package com.mydashboardapp.[feature].ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun [Feature]Screen(
    onNavigateUp: () -> Unit,
    viewModel: [Feature]ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("[Feature Name]") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Your UI content here
            when (uiState) {
                is [Feature]UiState.Loading -> {
                    CircularProgressIndicator()
                }
                is [Feature]UiState.Success -> {
                    // Success UI
                }
                is [Feature]UiState.Error -> {
                    Text("Error: ${uiState.message}")
                }
            }
        }
    }
}
```

### Step 8: Create ViewModel

Create the ViewModel in `ui/[Feature]ViewModel.kt`:

```kotlin
package com.mydashboardapp.[feature].ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class [Feature]ViewModel @Inject constructor(
    // Inject dependencies from data or core modules
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<[Feature]UiState>([Feature]UiState.Loading)
    val uiState: StateFlow<[Feature]UiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load your data
                _uiState.value = [Feature]UiState.Success(/* data */)
            } catch (e: Exception) {
                _uiState.value = [Feature]UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun onUserAction() {
        // Handle user actions
    }
}

sealed class [Feature]UiState {
    object Loading : [Feature]UiState()
    data class Success(val data: Any) : [Feature]UiState()
    data class Error(val message: String) : [Feature]UiState()
}
```

### Step 9: Register Navigation

Add your feature navigation to the main navigation in `app/src/main/kotlin/com/mydashboardapp/navigation/MainNavigation.kt`:

```kotlin
// Import your navigation functions
import com.mydashboardapp.[feature].navigation.*

// Add to the NavGraphBuilder
fun NavGraphBuilder.mainNavigation(/* parameters */) {
    // ... existing navigation
    
    [feature]Screen(
        onNavigateUp = { navController.popBackStack() },
        onNavigateToOtherFeature = { /* navigation logic */ }
    )
}
```

### Step 10: Add to Bottom Navigation (if needed)

If your feature should appear in the bottom navigation, add it to `core/src/main/kotlin/com/mydashboardapp/core/navigation/BottomNavigation.kt`:

```kotlin
// Add your destination
enum class BottomNavDestination(/* parameters */) {
    // ... existing destinations
    [FEATURE_NAME]("[feature]", Icons.Default.[Icon], "[Feature Display Name]"),
}
```

### Step 11: Write Tests

Create unit tests in `src/test/kotlin/`:

```kotlin
package com.mydashboardapp.[feature].ui

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class [Feature]ViewModelTest {
    
    @Test
    fun `initial state is loading`() = runTest {
        // Test your ViewModel
    }
}
```

Create UI tests in `src/androidTest/kotlin/`:

```kotlin
package com.mydashboardapp.[feature].ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class [Feature]ScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun featureScreen_displaysCorrectly() {
        // Test your composables
    }
}
```

### Step 12: Update Documentation

1. Add your feature to the README.md module list
2. Create feature design document in `docs/feature-designs/`
3. Update any relevant ADRs if architectural decisions were made
4. Add module-specific README.md if needed

## Best Practices

### Architecture
- Follow MVVM pattern with Compose
- Use Hilt for dependency injection
- Keep business logic in ViewModels or use cases
- Use StateFlow for UI state management

### Dependencies
- Depend only on `core` and `data` modules
- Avoid dependencies between feature modules
- Use interfaces for testability

### UI/UX
- Follow Material 3 design guidelines
- Use components from `core` module when possible
- Implement proper accessibility support
- Handle different screen sizes

### Testing
- Write unit tests for ViewModels
- Create UI tests for complex interactions
- Test navigation flows
- Mock dependencies appropriately

### Performance
- Use LazyColumn for lists
- Optimize Compose recompositions
- Handle configuration changes properly
- Consider memory usage

## Common Issues and Solutions

### Build Issues
- Ensure module is added to `settings.gradle.kts`
- Check namespace matches directory structure
- Verify all required dependencies are included

### Navigation Issues
- Ensure routes are unique across features
- Check navigation registration in main navigation
- Verify parameter passing between screens

### Dependency Injection Issues
- Ensure Hilt module is properly annotated
- Check that dependencies are available in correct scope
- Verify injection points use correct annotations

## Examples

For reference, examine existing feature modules:
- `feature-productivity` - Task management
- `feature-nutrition` - Meal tracking
- `feature-training` - Workout planning

## Getting Help

If you encounter issues:
1. Check existing feature modules for patterns
2. Review the [Architecture Decision Records](architecture-decisions/)
3. Open a discussion on GitHub
4. Ask questions in pull request reviews

## Validation Checklist

Before submitting your new feature module:

- [ ] Module builds successfully
- [ ] All tests pass
- [ ] Navigation works correctly
- [ ] UI follows Material 3 guidelines
- [ ] Code follows project conventions
- [ ] Documentation is updated
- [ ] No dependencies on other feature modules
- [ ] Hilt integration works
- [ ] Accessibility is implemented
- [ ] Performance is acceptable
