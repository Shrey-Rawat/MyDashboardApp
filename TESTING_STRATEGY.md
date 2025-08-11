# Testing Strategy - Best Productivity App

## Overview

This document outlines the comprehensive testing strategy for the Best Productivity App, designed to achieve **90% code coverage** for repositories and ensure robust testing across all application layers.

## Testing Framework Stack

### Unit Testing
- **JUnit 5** - Modern testing framework with powerful features
- **MockK** - Mocking library for Kotlin
- **Kotlinx Coroutines Test** - Testing coroutines and flows
- **Turbine** - Testing Kotlin flows
- **Robolectric** - Android unit testing framework

### Instrumented Testing  
- **AndroidX Test** - Core Android testing framework
- **Room Testing** - Database migration testing
- **Compose Testing** - UI testing for Jetpack Compose
- **Espresso** - UI interaction testing
- **Hilt Testing** - Dependency injection testing

### End-to-End Testing
- **Google Play Billing Test Lab** - Real purchase flow testing
- **Firebase Test Lab** - Device testing at scale
- **Test APKs** - Dedicated test builds

## Test Categories

### 1. Unit Tests for Repositories (90% Coverage Target)

**Location**: `data/src/test/kotlin/`

#### Coverage Goals
- **InventoryRepository**: 95% line coverage
- **NutritionRepository**: 90% line coverage  
- **TrainingRepository**: 90% line coverage
- **FinanceRepository**: 90% line coverage
- **ProductivityRepository**: 90% line coverage

#### Key Test Areas
```kotlin
// Data operations
✅ CRUD operations (Create, Read, Update, Delete)
✅ Flow transformations and mapping
✅ Error handling and edge cases
✅ Business logic calculations
✅ Data validation and constraints

// Examples
- getAllActiveItems() → verifies DAO flow mapping
- searchItems() → tests query parameter handling
- recordStockMovementFromScan() → validates business rules
- generateAffiliateDeeplink() → tests URL building logic
- getLowStockItemsFlow() → verifies filtering and calculations
```

#### Mock Strategy
- **DAO Layer**: Fully mocked using MockK
- **External Dependencies**: Stubbed with realistic data
- **Flow Testing**: Using `flowOf()` and `first()` assertions
- **Coroutine Testing**: `runTest` for suspend functions

### 2. Instrumented Tests for Room Migrations

**Location**: `data/src/androidTest/kotlin/com/mydashboardapp/data/migrations/`

#### Migration Test Coverage
```kotlin
✅ MIGRATION_1_2: Food verification + Exercise YouTube links
✅ MIGRATION_2_3: User preferences table addition
✅ MIGRATION_3_4: Task schema restructuring
✅ MIGRATION_4_5: Notifications table addition
✅ Full migration chain (1→5)
✅ Data preservation during migrations
✅ Index creation verification
✅ Foreign key constraint testing
```

#### Test Patterns
```kotlin
@Test
fun migrate1To2_preservesExistingData() {
    // 1. Create database at version 1 with test data
    var db = helper.createDatabase(TEST_DB, 1).apply {
        execSQL("INSERT INTO foods ...")
        close()
    }
    
    // 2. Run migration
    db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    
    // 3. Verify data preserved and new schema applied
    val cursor = db.query("SELECT * FROM foods WHERE id = 1")
    assert(cursor.getString("name") == "Test Food")
    assert(cursor.getInt("isVerified") == 0) // New column
}
```

### 3. UI Tests with Compose Testing & Espresso

**Location**: `app/src/androidTest/java/com/mydashboardapp/ui/`

#### Critical Flow Testing
```kotlin
✅ AI Chat Screen
  - Welcome state when no provider configured
  - Message sending and streaming indicators
  - Template selection and navigation
  - Error states and loading states

✅ Transaction Entry Screen
  - Income/Expense toggle functionality
  - Form validation and required fields
  - Dropdown selections (accounts, categories)
  - State preservation across config changes

✅ Live Session Screen
  - Workout timer display and functionality
  - Set recording and rest timer management
  - Exercise navigation and progress tracking
  - Workout completion flow

✅ Premium Upgrade Screen
  - Product loading and display
  - Subscription selection and purchase flow
  - Trial management and feature gating
  - Error handling and retry mechanisms
```

#### Accessibility Testing
```kotlin
@Test
fun accessibilityTest_allScreensHaveContentDescriptions() {
    // Verify all interactive elements have content descriptions
    composeTestRule.onNodeWithContentDescription("Back").assertExists()
    composeTestRule.onNodeWithContentDescription("Settings").assertExists()
    composeTestRule.onNodeWithText("Submit").assert(hasClickAction())
}

@Test 
fun semanticsTest_verifyProperSemantics() {
    // Test semantic properties for screen readers
    composeTestRule.onNodeWithText("Configure Providers")
        .assert(hasClickAction())
    composeTestRule.onNodeWithText("Ask me anything...")
        .assert(hasSetTextAction())
}
```

### 4. End-to-End Premium Purchase Flow Tests

**Location**: `billing/src/androidTest/kotlin/com/mydashboardapp/billing/`

#### Google Play Billing Test Lab Integration

##### Test Products Configuration
```kotlin
// Configure in Google Play Console
TEST_PREMIUM_MONTHLY = "premium_monthly_test"
TEST_PREMIUM_YEARLY = "premium_yearly_test" 
TEST_PREMIUM_LIFETIME = "premium_lifetime_test"

// Test accounts for purchase flows
TEST_ACCOUNTS = ["test1@gmail.com", "test2@gmail.com"]
```

##### E2E Test Scenarios
```kotlin
✅ Billing Client Connection
  - Initialize billing client
  - Query test products
  - Handle connection errors

✅ Product Display and Selection
  - Load subscription options
  - Display pricing and features
  - Handle product selection

✅ Purchase Flow Initiation
  - Start Google Play purchase dialog
  - Handle purchase flow states
  - Process purchase results

✅ Trial Management
  - Check trial availability
  - Start trial period
  - Track trial expiration

✅ Purchase Validation
  - Server-side validation (mocked)
  - Receipt verification
  - Premium state updates

✅ Error Scenarios
  - Network failures
  - Payment method issues
  - Subscription cancellations
```

##### Firebase Test Lab Integration
```kotlin
@Test
fun testLabIntegration_realPurchaseFlow() {
    val isTestLab = isRunningInTestLab()
    if (!isTestLab) return // Skip in local environment
    
    // Real purchase flow testing with test accounts
    composeTestRule.onNodeWithText("Yearly").performClick()
    composeTestRule.onNodeWithText("Subscribe Now").performClick()
    
    // Wait for Google Play purchase dialog
    Thread.sleep(15000)
    
    // Verify post-purchase state
}

private fun isRunningInTestLab(): Boolean {
    return try {
        val testLabSetting = InstrumentationRegistry
            .getArguments()
            .getString("firebase.testLabSetting")
        testLabSetting != null
    } catch (e: Exception) {
        false
    }
}
```

## Test Execution Strategy

### Local Development
```bash
# Unit tests
./gradlew test
./gradlew testDebugUnitTest --continue

# Instrumented tests  
./gradlew connectedAndroidTest
./gradlew connectedDebugAndroidTest

# Specific test suites
./gradlew :data:testDebugUnitTest
./gradlew :billing:connectedDebugAndroidTest
```

### Continuous Integration
```yaml
# GitHub Actions / CI Pipeline
- name: Run Unit Tests
  run: ./gradlew test --continue

- name: Run Migration Tests
  run: ./gradlew :data:connectedDebugAndroidTest

- name: Run UI Tests
  run: ./gradlew :app:connectedDebugAndroidTest

- name: Generate Coverage Report
  run: ./gradlew jacocoTestReport

- name: Upload to Firebase Test Lab
  run: |
    gcloud firebase test android run \
      --type instrumentation \
      --app app-debug.apk \
      --test app-debug-androidTest.apk \
      --device model=Pixel2,version=28,locale=en,orientation=portrait
```

### Google Play Test Lab
```bash
# Build test APKs
./gradlew assembleDebug assembleDebugAndroidTest

# Upload to Test Lab
gcloud firebase test android run \
  --type instrumentation \
  --app app-debug.apk \
  --test app-debug-androidTest.apk \
  --device model=Pixel6,version=33 \
  --use-orchestrator \
  --environment-variables firebase.testLabSetting=true
```

## Coverage Targets and Metrics

### Repository Layer Coverage
- **Minimum Target**: 90%
- **Stretch Goal**: 95%
- **Exclusions**: Generated code, data classes
- **Measurement**: JaCoCo line coverage

### UI Test Coverage
- **Critical User Flows**: 100%
- **Accessibility**: All interactive elements
- **Error States**: Major error scenarios
- **Performance**: No ANRs or crashes

### E2E Test Coverage
- **Purchase Flows**: All subscription types
- **Trial Management**: Complete trial lifecycle  
- **Feature Gating**: Premium vs free validation
- **Error Recovery**: Network and payment failures

## Test Data Management

### Mock Data Strategy
```kotlin
// Centralized mock data factories
fun mockItem(id: Long = 1L, name: String = "Test Item"): Item
fun mockFood(calories: Int = 100, protein: Double = 10.0): Food
fun mockTransaction(amount: Double = -50.0, category: String): Transaction

// Realistic test scenarios
val LOW_STOCK_SCENARIO = listOf(
    mockItem(minimumStock = 10, currentStock = 5),
    mockItem(minimumStock = 20, currentStock = 15)
)
```

### Test Database Management
```kotlin
// Clean state for each test
@BeforeEach
fun setupDatabase() {
    database.clearAllTables()
    database.insertTestData()
}

// Migration test data
fun insertMigrationTestData(version: Int) {
    when (version) {
        1 -> insertV1TestData()
        2 -> insertV2TestData()
        // etc.
    }
}
```

## Performance Testing

### Response Time Targets
- **Repository Operations**: <100ms average
- **UI Interactions**: <50ms to first frame
- **Database Queries**: <25ms for simple queries
- **Migration Time**: <2s for any single migration

### Memory Testing
- **Repository Tests**: No memory leaks in long-running tests
- **UI Tests**: Proper lifecycle management
- **Database Tests**: Connection pooling validation

## Test Documentation

### Test Naming Convention
```kotlin
// Pattern: should_[expectedBehavior]_when_[stateUnderTest]
fun `should_return_low_stock_items_when_inventory_below_minimum`()
fun `should_update_click_count_when_affiliate_link_clicked`()
fun `should_preserve_data_when_migrating_from_v1_to_v2`()
```

### Test Structure
```kotlin
@Test
@DisplayName("Human readable test description")
fun `descriptive_test_name_with_underscores`() = runTest {
    // Given - Set up test conditions
    val input = mockData()
    every { mockDao.method() } returns expected
    
    // When - Execute the operation under test
    val result = repository.performOperation(input)
    
    // Then - Verify the results
    assertEquals(expected, result)
    verify { mockDao.method() }
}
```

## Debugging and Troubleshooting

### Common Test Issues

#### Repository Tests
```kotlin
// Issue: Flow never completes
// Solution: Use `.first()` instead of collecting
val result = repository.getData().first() // ✅
val result = repository.getData().collect {} // ❌

// Issue: Coroutine not completing
// Solution: Use runTest
fun test() = runTest { } // ✅
suspend fun test() { } // ❌
```

#### Migration Tests
```kotlin
// Issue: Migration not found
// Solution: Register all migrations
helper.runMigrationsAndValidate(
    DB_NAME, 
    version, 
    true,
    *DatabaseMigrations.getAllMigrations() // ✅
)
```

#### UI Tests
```kotlin
// Issue: Node not found
// Solution: Wait for content
composeTestRule.waitForIdle()
composeTestRule.onNodeWithText("Loading").assertDoesNotExist()
composeTestRule.onNodeWithText("Content").assertIsDisplayed()
```

## Maintenance and Updates

### Weekly Tasks
- [ ] Review test coverage reports
- [ ] Update mock data for new features
- [ ] Check for flaky tests
- [ ] Update Test Lab device configurations

### Monthly Tasks
- [ ] Analyze test performance metrics
- [ ] Update testing documentation
- [ ] Review and update test data scenarios
- [ ] Validate E2E test accounts and products

### Release Tasks
- [ ] Run full test suite including E2E
- [ ] Validate all migrations with production-like data
- [ ] Test purchase flows on all supported devices
- [ ] Update test product configurations

## Tools and Resources

### IDE Setup
- **Android Studio**: Latest stable with test runner configuration
- **Plugins**: JUnit 5, MockK, Coverage reports
- **Run Configurations**: Separate configs for unit, integration, and E2E tests

### External Tools
- **Firebase Test Lab**: Device testing at scale
- **Google Play Console**: Test product management
- **JaCoCo**: Coverage reporting
- **Gradle**: Build automation and test orchestration

This comprehensive testing strategy ensures robust, maintainable, and high-quality code across all application layers while achieving the target 90% coverage for repositories and providing thorough validation of critical user flows.
