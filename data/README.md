# MyDashboardApp Database Schema

This document describes the comprehensive Room database schema for the Best Productivity App, designed with six top-level entity groups representing different aspects of personal productivity tracking.

## Database Overview

The database is implemented using Android Room with the following characteristics:
- **Database Name**: `best_productivity_database`
- **Current Version**: 1
- **Architecture**: Normalized schema with foreign key relationships
- **Migration Support**: Full migration framework with utilities
- **Prepopulation**: Automatic seeding with starter data from assets

## Entity Groups

### 1. Nutrition 🥗

Tracks food intake, nutritional information, and meal planning.

#### Entities:
- **Food**: Individual food items with nutritional data per serving
- **Ingredient**: Base ingredients with nutritional data per gram
- **Meal**: Recorded meals with calculated nutritional totals
- **MealFoodCrossRef**: Many-to-many relationship between meals and foods

#### Key Features:
- Barcode scanning support
- Serving size calculations
- Nutritional macro tracking (calories, protein, carbs, fat, fiber, sugar, sodium)
- Brand and custom food support
- Allergen information

### 2. Training 💪

Manages workout routines, exercises, and fitness tracking.

#### Entities:
- **Exercise**: Exercise library with instructions and metadata
- **Workout**: Individual workout sessions
- **Set**: Individual sets within exercises during workouts  
- **WorkoutExerciseCrossRef**: Links exercises to workouts with targets

#### Key Features:
- Comprehensive exercise library
- Muscle group categorization
- Equipment tracking
- Set/rep/weight progression
- Workout templates
- Calorie burn estimation
- RPE (Rate of Perceived Exertion) tracking

### 3. Productivity 📋

Handles task management, goal tracking, and time logging.

#### Entities:
- **Task**: Individual tasks with priorities and due dates
- **Goal**: High-level objectives with progress tracking
- **TimeLog**: Time tracking sessions with productivity metrics

#### Key Features:
- Hierarchical task structure (subtasks)
- Goal-task relationships
- Time tracking with Pomodoro support
- Priority and status management
- Recurring task support
- Productivity analytics

### 4. Finance 💰

Manages financial accounts, transactions, and investments.

#### Entities:
- **Account**: Bank accounts, credit cards, investment accounts
- **Transaction**: Financial transactions with categorization
- **Investment**: Stock/fund holdings with performance tracking
- **PriceSnapshot**: Historical price data for investments

#### Key Features:
- Multi-currency support
- Transaction categorization
- Investment portfolio tracking
- Price history and alerts
- Financial analytics and reporting
- Account balance tracking

### 5. Inventory 📦

Tracks personal items, locations, and stock movements.

#### Entities:
- **Item**: Physical items with metadata and valuation
- **Location**: Hierarchical storage locations
- **StockMovement**: Item movements between locations
- **AffiliateLink**: Purchase links with affiliate tracking

#### Key Features:
- Hierarchical location system
- Stock level monitoring
- Purchase price tracking
- Barcode scanning
- Affiliate link management
- Low stock alerts

### 6. AI 🤖

Stores AI interaction history and usage analytics.

#### Entities:
- **PromptHistory**: AI conversation history with metadata

#### Key Features:
- Multi-provider support (OpenAI, Anthropic, etc.)
- Token and cost tracking
- Quality rating system
- Conversation threading
- Usage analytics
- Bookmark and favorite system

## Database Architecture

### Relationships

The schema uses foreign keys to maintain referential integrity:

```
Goals (1) ←→ (N) Tasks
Tasks (1) ←→ (N) TimeLogs  
Tasks (1) ←→ (N) Tasks (parent-child)

Meals (N) ←→ (N) Foods (via MealFoodCrossRef)

Workouts (N) ←→ (N) Exercises (via WorkoutExerciseCrossRef)
WorkoutExerciseCrossRef (1) ←→ (N) Sets

Accounts (1) ←→ (N) Transactions
Accounts (1) ←→ (N) Investments
Investments (1) ←→ (N) PriceSnapshots

Locations (1) ←→ (N) Locations (parent-child)
Items (1) ←→ (N) StockMovements
Items (1) ←→ (N) AffiliateLinks
Locations (1) ←→ (N) StockMovements
```

### Indexing Strategy

Indexes are strategically placed on:
- Foreign key columns
- Frequently queried date/timestamp fields
- Search fields (names, categories)
- Status and active flags

### Data Integrity

- Foreign keys with appropriate cascade/set null behavior
- Non-null constraints on essential fields
- Default values for timestamps and boolean fields
- Check constraints where applicable

## Data Access Layer

### DAOs (Data Access Objects)

Each entity group has a dedicated DAO with:
- Basic CRUD operations
- Complex query methods with joins
- Analytics and reporting queries
- Flow-based reactive queries for UI

### Repositories

Repository pattern implementation provides:
- Clean abstraction over data sources
- Consistent API across entity groups
- Future-ready for remote data integration
- Dependency injection with Hilt

## Migration System

Comprehensive migration framework with:
- Version-controlled schema changes
- Data preservation utilities
- Rollback capability
- Migration validation tools

## Prepopulation System

Automatic database seeding with:
- Common foods and ingredients
- Exercise library
- Sample goals and categories
- Basic location hierarchy
- Configurable via JSON assets

## Usage Examples

### Basic Operations

```kotlin
// Inject repository
@Inject lateinit var nutritionRepository: NutritionRepository

// Insert a meal
val mealId = nutritionRepository.insertMeal(
    Meal(
        name = "Breakfast",
        mealType = "Breakfast", 
        dateConsumed = System.currentTimeMillis(),
        totalCalories = 350
    )
)

// Add foods to meal
nutritionRepository.addFoodToMeal(
    MealFoodCrossRef(mealId, foodId, quantity = 1.0)
)

// Query meals by date
val meals = nutritionRepository.getMealsByDateRange(startDate, endDate)
```

### Analytics Queries

```kotlin
// Nutrition summary
val summary = nutritionDao.getNutritionSummary(startDate, endDate)

// Workout statistics  
val workoutStats = trainingDao.getWorkoutSummary(startDate, endDate)

// Financial overview
val financials = financeDao.getFinancialSummary(startDate, endDate)
```

## File Structure

```
data/src/main/kotlin/com/mydashboardapp/data/
├── entities/           # Entity classes
│   ├── Food.kt
│   ├── Exercise.kt  
│   ├── Task.kt
│   └── ...
├── dao/               # Data Access Objects
│   ├── NutritionDao.kt
│   ├── TrainingDao.kt
│   └── ...  
├── repository/        # Repository implementations
│   └── NutritionRepository.kt
├── migrations/        # Database migrations
│   └── DatabaseMigrations.kt
├── di/               # Dependency injection
│   └── DatabaseModule.kt
└── BestProductivityDatabase.kt  # Main database class
```

## Assets Structure

```
app/src/main/assets/
├── nutrition_foods.json      # Sample food data
├── training_exercises.json   # Exercise library
├── inventory_locations.json  # Location hierarchy
└── ...
```

## Best Practices

1. **Always use repository pattern** - Never access DAOs directly from UI
2. **Handle migrations carefully** - Test thoroughly before releasing
3. **Use transactions** for multi-table operations
4. **Optimize queries** with proper indexing
5. **Validate data** before insertion
6. **Use Flow** for reactive UI updates
7. **Consider performance** for large datasets

## Future Enhancements

- [ ] Cloud synchronization layer
- [ ] Offline-first architecture
- [ ] Advanced analytics views
- [ ] Data export/import
- [ ] Backup and restore
- [ ] Multi-user support
- [ ] Real-time collaboration

This schema provides a solid foundation for a comprehensive productivity app while maintaining flexibility for future enhancements and features.
