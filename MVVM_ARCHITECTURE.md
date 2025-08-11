# MVVM Architecture with Kotlin Flows Implementation

This document describes the implementation of MVVM (Model-View-ViewModel) architecture with Kotlin Flows, Hilt dependency injection, and DataStore for the Best Productivity App.

## Architecture Overview

The app follows a clean MVVM architecture pattern with the following key components:

### Core Components

#### 1. BaseViewModel
- **Location**: `core/src/main/kotlin/com/mydashboardapp/core/ui/BaseViewModel.kt`
- **Purpose**: Abstract base class for all ViewModels with standardized state management
- **Features**:
  - Automatic error handling with `CoroutineExceptionHandler`
  - Standardized state updates using `StateFlow`
  - Built-in loading and error state management
  - Extension points for custom error handling

```kotlin
// Example usage
class MyViewModel : BaseViewModel<MyUiState>(
    initialState = MyUiState()
) {
    fun doSomething() {
        launchWithErrorHandling {
            setLoading(true)
            // Perform operations
            setLoading(false)
        }
    }
}
```

#### 2. UiState Interface and Implementations
- **Location**: `core/src/main/kotlin/com/mydashboardapp/core/ui/UiState.kt`
- **Purpose**: Standardized UI state representation
- **Types**:
  - `UiState`: Base interface for all UI states
  - `StandardUiState<T>`: Generic state with data, loading, and error handling
  - `LoadingUiState`: Simple state for loading operations

```kotlin
data class MyScreenUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
```

### Repository Pattern

#### 3. BaseRepository
- **Location**: `core/src/main/kotlin/com/mydashboardapp/core/data/BaseRepository.kt`
- **Purpose**: Standardized repository interface with CRUD operations
- **Features**:
  - Generic CRUD operations
  - Support for remote data sources
  - Automatic sync capabilities
  - Flow-based reactive data access

```kotlin
interface MyRepository : BaseRepository<MyEntity, Long> {
    // Domain-specific methods
    suspend fun getByCategory(category: String): List<MyEntity>
}
```

#### 4. Repository Implementation Example
- **Location**: `data/src/main/kotlin/com/mydashboardapp/data/repository/NutritionRepository.kt`
- **Purpose**: Shows how to implement repositories with both local and remote data sources
- **Features**:
  - Extends `BaseRepositoryImpl` for common functionality
  - Implements domain-specific operations
  - Handles remote sync with conflict resolution
  - Proper Hilt dependency injection setup

### User Preferences and Feature Flags

#### 5. DataStore Integration
- **Location**: `core/src/main/kotlin/com/mydashboardapp/core/data/UserPreferencesRepository.kt`
- **Purpose**: Reactive user preferences management
- **Features**:
  - Type-safe preferences with data classes
  - Flow-based reactive updates
  - Feature flags for free vs pro functionality
  - Automatic persistence with DataStore

```kotlin
// Observing preferences
userPreferencesRepository.userPreferences
    .onEach { preferences ->
        updateUI(preferences)
    }
    .launchIn(viewModelScope)
```

#### 6. Feature Flags System
- **Location**: `core/src/main/kotlin/com/mydashboardapp/core/data/UserPreferences.kt`
- **Purpose**: Dynamic feature enabling based on user tier (free vs pro)
- **Features**:
  - Build-time and runtime feature toggles
  - Limit enforcement for free users
  - Upgrade prompts and messaging

```kotlin
val featureFlags = FeatureFlags(
    maxNutritionEntries = if (isPro) -1 else 10,
    advancedAnalyticsEnabled = isPro,
    cloudSyncEnabled = isPro
)
```

### Dependency Injection with Hilt

#### 7. Core Module
- **Location**: `core/src/main/kotlin/com/mydashboardapp/core/di/CoreModule.kt`
- **Purpose**: Hilt module for core components
- **Provides**:
  - DataStore instance
  - UserPreferencesRepository binding

#### 8. Database Module
- **Location**: `data/src/main/kotlin/com/mydashboardapp/data/di/DatabaseModule.kt`
- **Purpose**: Hilt module for data layer components
- **Provides**:
  - Room database instance
  - DAO instances
  - Repository implementations

### ViewModel Implementations

#### 9. NutritionViewModel Example
- **Location**: `feature-nutrition/src/main/kotlin/com/mydashboardapp/nutrition/ui/NutritionViewModel.kt`
- **Purpose**: Demonstrates complete MVVM implementation
- **Features**:
  - Reactive data loading with `combine`
  - Feature flag enforcement
  - Error handling and user feedback
  - Pro feature gating with upgrade prompts

#### 10. SettingsViewModel Example
- **Location**: `core/src/main/kotlin/com/mydashboardapp/core/ui/SettingsViewModel.kt`
- **Purpose**: Demonstrates preferences management
- **Features**:
  - User preference updates
  - Feature availability checking
  - Pro upgrade handling
  - Success/error message management

## Key Patterns and Best Practices

### 1. Reactive Data Flow
```kotlin
// Combine multiple data sources
combine(
    repository.getData(),
    userPreferencesRepository.featureFlags
) { data, flags ->
    // Transform data based on feature flags
    transformData(data, flags)
}.launchIn(viewModelScope)
```

### 2. Error Handling
```kotlin
// Automatic error handling in BaseViewModel
launchWithErrorHandling {
    // Operations that might throw exceptions
    performOperation()
}
```

### 3. Feature Flag Usage
```kotlin
fun performProFeature() {
    if (!currentState.featureFlags.proFeatureEnabled) {
        showUpgradeDialog("This is a Pro feature")
        return
    }
    // Perform pro feature logic
}
```

### 4. State Management
```kotlin
// Update state immutably
updateState { currentState ->
    currentState.copy(
        data = newData,
        isLoading = false,
        errorMessage = null
    )
}
```

## Benefits of This Architecture

1. **Separation of Concerns**: Clear separation between UI, business logic, and data layers
2. **Testability**: Easy to unit test ViewModels and repositories
3. **Reactive UI**: Automatic UI updates when data changes
4. **Error Handling**: Consistent error handling across the app
5. **Feature Flags**: Easy to enable/disable features based on user tier
6. **Type Safety**: Strongly typed preferences and state management
7. **Scalability**: Easy to add new features following established patterns

## Usage Examples

### Creating a New Feature
1. Define UI state data class extending `UiState`
2. Create ViewModel extending `BaseViewModel<YourUiState>`
3. Create repository interface extending `BaseRepository<Entity, ID>`
4. Implement repository extending `BaseRepositoryImpl`
5. Add Hilt bindings in appropriate modules
6. Use feature flags for pro functionality

### Adding User Preferences
1. Add properties to `UserPreferences` data class
2. Add preference keys to `UserPreferencesRepository`
3. Add methods to update preferences
4. Use in ViewModels with reactive flows

### Implementing Remote Sync
1. Create remote data source implementing `BaseRemoteDataSource`
2. Add sync logic to repository implementation
3. Handle sync conflicts and errors
4. Update UI to show sync status

This architecture provides a solid foundation for building scalable, maintainable Android applications with proper separation of concerns and reactive data flow.
