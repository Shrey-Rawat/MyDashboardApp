package com.mydashboardapp.data.repository

import com.mydashboardapp.data.dao.*
import com.mydashboardapp.data.entities.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive repository test suite covering all major repositories
 * Designed to achieve 90% code coverage across repository layer
 */
@DisplayName("Repository Test Suite - 90% Coverage Target")
class RepositoryTestSuite {

    private lateinit var nutritionDao: NutritionDao
    private lateinit var trainingDao: TrainingDao
    private lateinit var financeDao: FinanceDao
    private lateinit var productivityDao: ProductivityDao

    @BeforeEach
    fun setup() {
        nutritionDao = mockk()
        trainingDao = mockk()
        financeDao = mockk()
        productivityDao = mockk()
    }

    @Nested
    @DisplayName("NutritionRepository Tests")
    inner class NutritionRepositoryTests {

        private lateinit var nutritionRepository: NutritionRepositoryImpl

        @BeforeEach
        fun setup() {
            nutritionRepository = NutritionRepositoryImpl(nutritionDao, null)
        }

        @Test
        @DisplayName("Should get all foods from DAO")
        fun `getAllFoods returns flow from dao`() = runTest {
            // Given
            val expectedFoods = listOf(mockFood(name = "Apple"), mockFood(name = "Banana"))
            every { nutritionDao.getAllFoods() } returns flowOf(expectedFoods)

            // When
            val result = nutritionRepository.getAllFoods().first()

            // Then
            assertEquals(expectedFoods, result)
            verify { nutritionDao.getAllFoods() }
        }

        @Test
        @DisplayName("Should search foods by query")
        fun `searchFoods returns matching results`() = runTest {
            // Given
            val query = "apple"
            val expectedFoods = listOf(mockFood(name = "Apple"), mockFood(name = "Pineapple"))
            coEvery { nutritionDao.searchFoods("%$query%") } returns expectedFoods

            // When
            val result = nutritionRepository.searchFoods(query)

            // Then
            assertEquals(expectedFoods, result)
            coVerify { nutritionDao.searchFoods("%$query%") }
        }

        @Test
        @DisplayName("Should insert food and return ID")
        fun `insertFood returns generated ID`() = runTest {
            // Given
            val food = mockFood()
            val expectedId = 123L
            coEvery { nutritionDao.insertFood(food) } returns expectedId

            // When
            val result = nutritionRepository.insertFood(food)

            // Then
            assertEquals(expectedId, result)
            coVerify { nutritionDao.insertFood(food) }
        }

        @Test
        @DisplayName("Should get nutrition entries for date range")
        fun `getNutritionEntriesForDateRange returns entries`() = runTest {
            // Given
            val startDate = 1000L
            val endDate = 2000L
            val expectedEntries = listOf(mockNutritionEntry(), mockNutritionEntry())
            every { nutritionDao.getEntriesForDateRange(startDate, endDate) } returns flowOf(expectedEntries)

            // When
            val result = nutritionRepository.getNutritionEntriesForDateRange(startDate, endDate).first()

            // Then
            assertEquals(expectedEntries, result)
            verify { nutritionDao.getEntriesForDateRange(startDate, endDate) }
        }

        @Test
        @DisplayName("Should calculate daily nutrition totals")
        fun `getDailyNutritionTotals calculates correctly`() = runTest {
            // Given
            val date = 1500L
            val entries = listOf(
                mockNutritionEntry(calories = 300, protein = 20.0, carbs = 40.0, fat = 10.0),
                mockNutritionEntry(calories = 200, protein = 15.0, carbs = 30.0, fat = 8.0)
            )
            every { nutritionDao.getEntriesForDate(date) } returns flowOf(entries)

            // When
            val result = nutritionRepository.getDailyNutritionTotals(date).first()

            // Then
            assertEquals(500, result.totalCalories)
            assertEquals(35.0, result.totalProtein, 0.01)
            assertEquals(70.0, result.totalCarbs, 0.01)
            assertEquals(18.0, result.totalFat, 0.01)
        }

        @Test
        @DisplayName("Should handle empty nutrition entries")
        fun `getDailyNutritionTotals handles empty entries`() = runTest {
            // Given
            val date = 1500L
            every { nutritionDao.getEntriesForDate(date) } returns flowOf(emptyList())

            // When
            val result = nutritionRepository.getDailyNutritionTotals(date).first()

            // Then
            assertEquals(0, result.totalCalories)
            assertEquals(0.0, result.totalProtein)
            assertEquals(0.0, result.totalCarbs)
            assertEquals(0.0, result.totalFat)
        }
    }

    @Nested
    @DisplayName("TrainingRepository Tests")
    inner class TrainingRepositoryTests {

        private lateinit var trainingRepository: TrainingRepositoryImpl

        @BeforeEach
        fun setup() {
            trainingRepository = TrainingRepositoryImpl(trainingDao)
        }

        @Test
        @DisplayName("Should get all exercises from DAO")
        fun `getAllExercises returns flow from dao`() = runTest {
            // Given
            val expectedExercises = listOf(mockExercise(name = "Push Up"), mockExercise(name = "Pull Up"))
            every { trainingDao.getAllExercises() } returns flowOf(expectedExercises)

            // When
            val result = trainingRepository.getAllExercises().first()

            // Then
            assertEquals(expectedExercises, result)
            verify { trainingDao.getAllExercises() }
        }

        @Test
        @DisplayName("Should get exercises by muscle group")
        fun `getExercisesByMuscleGroup filters correctly`() = runTest {
            // Given
            val muscleGroup = "Chest"
            val expectedExercises = listOf(mockExercise(muscleGroup = muscleGroup))
            every { trainingDao.getExercisesByMuscleGroup(muscleGroup) } returns flowOf(expectedExercises)

            // When
            val result = trainingRepository.getExercisesByMuscleGroup(muscleGroup).first()

            // Then
            assertEquals(expectedExercises, result)
            verify { trainingDao.getExercisesByMuscleGroup(muscleGroup) }
        }

        @Test
        @DisplayName("Should create workout with exercises")
        fun `createWorkoutWithExercises inserts workout and exercises`() = runTest {
            // Given
            val workout = mockWorkout()
            val exercises = listOf(mockWorkoutExercise(), mockWorkoutExercise())
            val workoutId = 456L
            
            coEvery { trainingDao.insertWorkout(workout) } returns workoutId
            coEvery { trainingDao.insertWorkoutExercises(any()) } just runs

            // When
            val result = trainingRepository.createWorkoutWithExercises(workout, exercises)

            // Then
            assertEquals(workoutId, result)
            coVerify { trainingDao.insertWorkout(workout) }
            coVerify { trainingDao.insertWorkoutExercises(any()) }
        }

        @Test
        @DisplayName("Should record workout session")
        fun `recordWorkoutSession creates session with sets`() = runTest {
            // Given
            val workoutId = 1L
            val exercises = listOf(mockWorkoutExercise())
            val sets = mapOf(1L to listOf(mockSet(), mockSet()))
            val sessionId = 789L

            val sessionSlot = slot<WorkoutSession>()
            coEvery { trainingDao.insertWorkoutSession(capture(sessionSlot)) } returns sessionId
            coEvery { trainingDao.insertSets(any()) } just runs

            // When
            val result = trainingRepository.recordWorkoutSession(workoutId, exercises, sets)

            // Then
            assertEquals(sessionId, result)
            
            val capturedSession = sessionSlot.captured
            assertEquals(workoutId, capturedSession.workoutId)
            assertNotNull(capturedSession.startTime)
            assertNotNull(capturedSession.endTime)
            
            coVerify { trainingDao.insertWorkoutSession(any()) }
            coVerify { trainingDao.insertSets(any()) }
        }

        @Test
        @DisplayName("Should calculate workout statistics")
        fun `getWorkoutStatistics calculates correctly`() = runTest {
            // Given
            val workoutId = 1L
            val sessions = listOf(
                mockWorkoutSession(workoutId = workoutId, duration = 3600), // 1 hour
                mockWorkoutSession(workoutId = workoutId, duration = 2400)  // 40 minutes
            )
            every { trainingDao.getWorkoutSessions(workoutId) } returns flowOf(sessions)

            // When
            val result = trainingRepository.getWorkoutStatistics(workoutId).first()

            // Then
            assertEquals(2, result.totalSessions)
            assertEquals(3000, result.averageDuration) // (3600 + 2400) / 2
            assertTrue(result.lastSessionDate > 0)
        }
    }

    @Nested
    @DisplayName("FinanceRepository Tests")
    inner class FinanceRepositoryTests {

        private lateinit var financeRepository: FinanceRepositoryImpl

        @BeforeEach
        fun setup() {
            financeRepository = FinanceRepositoryImpl(financeDao)
        }

        @Test
        @DisplayName("Should get all accounts from DAO")
        fun `getAllAccounts returns flow from dao`() = runTest {
            // Given
            val expectedAccounts = listOf(mockAccount(name = "Checking"), mockAccount(name = "Savings"))
            every { financeDao.getAllAccounts() } returns flowOf(expectedAccounts)

            // When
            val result = financeRepository.getAllAccounts().first()

            // Then
            assertEquals(expectedAccounts, result)
            verify { financeDao.getAllAccounts() }
        }

        @Test
        @DisplayName("Should record expense transaction")
        fun `recordExpense creates transaction and updates balance`() = runTest {
            // Given
            val accountId = 1L
            val amount = 50.0
            val description = "Coffee"
            val category = "Food"
            val account = mockAccount(id = accountId, balance = 1000.0)
            val transactionId = 123L

            val transactionSlot = slot<Transaction>()
            coEvery { financeDao.getAccountById(accountId) } returns account
            coEvery { financeDao.insertTransaction(capture(transactionSlot)) } returns transactionId
            coEvery { financeDao.updateAccountBalance(accountId, 950.0) } just runs

            // When
            val result = financeRepository.recordExpense(accountId, amount, description, category)

            // Then
            assertEquals(transactionId, result)
            
            val capturedTransaction = transactionSlot.captured
            assertEquals(accountId, capturedTransaction.accountId)
            assertEquals(-amount, capturedTransaction.amount) // Negative for expense
            assertEquals(description, capturedTransaction.description)
            assertEquals(category, capturedTransaction.category)
            
            coVerify { financeDao.updateAccountBalance(accountId, 950.0) }
        }

        @Test
        @DisplayName("Should record income transaction")
        fun `recordIncome creates transaction and updates balance`() = runTest {
            // Given
            val accountId = 1L
            val amount = 2000.0
            val description = "Salary"
            val category = "Income"
            val account = mockAccount(id = accountId, balance = 1000.0)
            val transactionId = 124L

            val transactionSlot = slot<Transaction>()
            coEvery { financeDao.getAccountById(accountId) } returns account
            coEvery { financeDao.insertTransaction(capture(transactionSlot)) } returns transactionId
            coEvery { financeDao.updateAccountBalance(accountId, 3000.0) } just runs

            // When
            val result = financeRepository.recordIncome(accountId, amount, description, category)

            // Then
            assertEquals(transactionId, result)
            
            val capturedTransaction = transactionSlot.captured
            assertEquals(accountId, capturedTransaction.accountId)
            assertEquals(amount, capturedTransaction.amount) // Positive for income
            
            coVerify { financeDao.updateAccountBalance(accountId, 3000.0) }
        }

        @Test
        @DisplayName("Should calculate monthly budget analysis")
        fun `getMonthlyBudgetAnalysis calculates spending by category`() = runTest {
            // Given
            val year = 2024
            val month = 3
            val transactions = listOf(
                mockTransaction(category = "Food", amount = -100.0),
                mockTransaction(category = "Food", amount = -50.0),
                mockTransaction(category = "Transport", amount = -75.0)
            )
            every { financeDao.getTransactionsForMonth(year, month) } returns flowOf(transactions)

            // When
            val result = financeRepository.getMonthlyBudgetAnalysis(year, month).first()

            // Then
            assertEquals(2, result.categoryTotals.size)
            assertEquals(150.0, result.categoryTotals["Food"])
            assertEquals(75.0, result.categoryTotals["Transport"])
            assertEquals(225.0, result.totalExpenses)
        }

        @Test
        @DisplayName("Should handle investment portfolio calculations")
        fun `getPortfolioPerformance calculates gains and losses`() = runTest {
            // Given
            val investments = listOf(
                mockInvestment(symbol = "AAPL", quantity = 10.0, purchasePrice = 150.0, currentPrice = 180.0),
                mockInvestment(symbol = "GOOGL", quantity = 5.0, purchasePrice = 2000.0, currentPrice = 1800.0)
            )
            every { financeDao.getAllInvestments() } returns flowOf(investments)

            // When
            val result = financeRepository.getPortfolioPerformance().first()

            // Then
            assertEquals(11500.0, result.totalCost, 0.01) // (10*150) + (5*2000)
            assertEquals(10800.0, result.currentValue, 0.01) // (10*180) + (5*1800)
            assertEquals(-700.0, result.totalGainLoss, 0.01) // 10800 - 11500
        }
    }

    @Nested
    @DisplayName("ProductivityRepository Tests") 
    inner class ProductivityRepositoryTests {

        private lateinit var productivityRepository: ProductivityRepositoryImpl

        @BeforeEach
        fun setup() {
            productivityRepository = ProductivityRepositoryImpl(productivityDao)
        }

        @Test
        @DisplayName("Should get active tasks from DAO")
        fun `getActiveTasks returns flow from dao`() = runTest {
            // Given
            val expectedTasks = listOf(mockTask(title = "Task 1"), mockTask(title = "Task 2"))
            every { productivityDao.getActiveTasks() } returns flowOf(expectedTasks)

            // When
            val result = productivityRepository.getActiveTasks().first()

            // Then
            assertEquals(expectedTasks, result)
            verify { productivityDao.getActiveTasks() }
        }

        @Test
        @DisplayName("Should complete task and update timestamp")
        fun `completeTask updates task status and timestamp`() = runTest {
            // Given
            val taskId = 1L
            val task = mockTask(id = taskId, isCompleted = false)
            
            val taskSlot = slot<Task>()
            coEvery { productivityDao.getTaskById(taskId) } returns task
            coEvery { productivityDao.updateTask(capture(taskSlot)) } just runs

            // When
            productivityRepository.completeTask(taskId)

            // Then
            val updatedTask = taskSlot.captured
            assertTrue(updatedTask.isCompleted)
            assertNotNull(updatedTask.completedAt)
            assertTrue(updatedTask.completedAt!! > task.createdAt)
            
            coVerify { productivityDao.updateTask(any()) }
        }

        @Test
        @DisplayName("Should get tasks by goal")
        fun `getTasksByGoal filters by goal ID`() = runTest {
            // Given
            val goalId = 5L
            val expectedTasks = listOf(mockTask(goalId = goalId))
            every { productivityDao.getTasksByGoal(goalId) } returns flowOf(expectedTasks)

            // When
            val result = productivityRepository.getTasksByGoal(goalId).first()

            // Then
            assertEquals(expectedTasks, result)
            verify { productivityDao.getTasksByGoal(goalId) }
        }

        @Test
        @DisplayName("Should calculate productivity metrics")
        fun `getProductivityMetrics calculates completion rates`() = runTest {
            // Given
            val startDate = 1000L
            val endDate = 2000L
            val completedTasks = 15
            val totalTasks = 20
            
            coEvery { productivityDao.getCompletedTasksCount(startDate, endDate) } returns completedTasks
            coEvery { productivityDao.getTotalTasksCount(startDate, endDate) } returns totalTasks

            // When
            val result = productivityRepository.getProductivityMetrics(startDate, endDate)

            // Then
            assertEquals(completedTasks, result.completedTasks)
            assertEquals(totalTasks, result.totalTasks)
            assertEquals(75.0, result.completionRate, 0.01) // 15/20 * 100
            
            coVerify { productivityDao.getCompletedTasksCount(startDate, endDate) }
            coVerify { productivityDao.getTotalTasksCount(startDate, endDate) }
        }

        @Test
        @DisplayName("Should handle zero division in metrics")
        fun `getProductivityMetrics handles zero total tasks`() = runTest {
            // Given
            val startDate = 1000L
            val endDate = 2000L
            
            coEvery { productivityDao.getCompletedTasksCount(startDate, endDate) } returns 0
            coEvery { productivityDao.getTotalTasksCount(startDate, endDate) } returns 0

            // When
            val result = productivityRepository.getProductivityMetrics(startDate, endDate)

            // Then
            assertEquals(0, result.completedTasks)
            assertEquals(0, result.totalTasks)
            assertEquals(0.0, result.completionRate)
        }
    }

    // Helper methods for creating mock objects
    private fun mockFood(
        id: Long = 1L,
        name: String = "Test Food",
        calories: Int = 100,
        protein: Double = 10.0,
        carbs: Double = 20.0,
        fat: Double = 5.0
    ): Food = Food(
        id = id,
        name = name,
        brandName = "Test Brand",
        servingSize = "1 cup",
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = 3.0,
        sugar = 10.0,
        sodium = 50.0,
        barcode = null,
        isVerified = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun mockNutritionEntry(
        id: Long = 1L,
        foodId: Long = 1L,
        calories: Int = 100,
        protein: Double = 10.0,
        carbs: Double = 20.0,
        fat: Double = 5.0
    ): NutritionEntry = NutritionEntry(
        id = id,
        foodId = foodId,
        servings = 1.0,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = 3.0,
        sugar = 10.0,
        sodium = 50.0,
        mealType = "Breakfast",
        consumedAt = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis()
    )

    private fun mockExercise(
        id: Long = 1L,
        name: String = "Test Exercise",
        category: String = "Strength",
        muscleGroup: String = "Chest"
    ): Exercise = Exercise(
        id = id,
        name = name,
        category = category,
        muscleGroup = muscleGroup,
        description = "Test exercise description",
        instructions = "Do the exercise",
        difficulty = "Beginner",
        equipmentNeeded = "None",
        youtubeVideoId = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun mockWorkout(id: Long = 1L, name: String = "Test Workout"): Workout = Workout(
        id = id,
        name = name,
        description = "Test workout description",
        estimatedDurationMinutes = 45,
        difficulty = "Intermediate",
        category = "Strength",
        isTemplate = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun mockWorkoutExercise(
        workoutId: Long = 1L,
        exerciseId: Long = 1L
    ): WorkoutExercise = WorkoutExercise(
        workoutId = workoutId,
        exerciseId = exerciseId,
        orderInWorkout = 1,
        targetSets = 3,
        targetReps = 10,
        targetWeight = 135.0,
        restTimeSeconds = 90,
        notes = null
    )

    private fun mockWorkoutSession(
        id: Long = 1L,
        workoutId: Long = 1L,
        duration: Int = 3600
    ): WorkoutSession = WorkoutSession(
        id = id,
        workoutId = workoutId,
        startTime = System.currentTimeMillis() - duration * 1000L,
        endTime = System.currentTimeMillis(),
        durationSeconds = duration,
        notes = null,
        createdAt = System.currentTimeMillis()
    )

    private fun mockSet(
        id: Long = 1L,
        sessionId: Long = 1L,
        exerciseId: Long = 1L
    ): Set = Set(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        setNumber = 1,
        reps = 10,
        weight = 135.0,
        duration = null,
        distance = null,
        restTimeSeconds = 90,
        rpe = 8,
        notes = null,
        completedAt = System.currentTimeMillis()
    )

    private fun mockAccount(
        id: Long = 1L,
        name: String = "Test Account",
        balance: Double = 1000.0
    ): Account = Account(
        id = id,
        name = name,
        type = "Checking",
        balance = balance,
        currency = "USD",
        isActive = true,
        notes = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun mockTransaction(
        id: Long = 1L,
        accountId: Long = 1L,
        amount: Double = -50.0,
        category: String = "Food"
    ): Transaction = Transaction(
        id = id,
        accountId = accountId,
        amount = amount,
        description = "Test transaction",
        category = category,
        subcategory = null,
        merchant = null,
        transactionDate = System.currentTimeMillis(),
        notes = null,
        isRecurring = false,
        tags = null,
        createdAt = System.currentTimeMillis()
    )

    private fun mockInvestment(
        id: Long = 1L,
        symbol: String = "AAPL",
        quantity: Double = 10.0,
        purchasePrice: Double = 150.0,
        currentPrice: Double = 180.0
    ): Investment = Investment(
        id = id,
        symbol = symbol,
        name = "Test Investment",
        type = "Stock",
        quantity = quantity,
        purchasePrice = purchasePrice,
        currentPrice = currentPrice,
        purchaseDate = System.currentTimeMillis(),
        notes = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun mockTask(
        id: Long = 1L,
        title: String = "Test Task",
        goalId: Long? = null,
        isCompleted: Boolean = false
    ): Task = Task(
        id = id,
        title = title,
        description = "Test task description",
        isCompleted = isCompleted,
        priority = "Medium",
        status = "In Progress",
        dueDate = null,
        completedAt = null,
        estimatedTimeMinutes = 30,
        actualTimeMinutes = null,
        goalId = goalId,
        parentTaskId = null,
        category = "Work",
        tags = null,
        notes = null,
        reminderAt = null,
        recurrenceRule = null,
        sortOrder = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    // Mock data classes for return values
    data class DailyNutritionTotals(
        val totalCalories: Int,
        val totalProtein: Double,
        val totalCarbs: Double,
        val totalFat: Double
    )

    data class WorkoutStatistics(
        val totalSessions: Int,
        val averageDuration: Int,
        val lastSessionDate: Long
    )

    data class MonthlyBudgetAnalysis(
        val categoryTotals: Map<String, Double>,
        val totalExpenses: Double
    )

    data class PortfolioPerformance(
        val totalCost: Double,
        val currentValue: Double,
        val totalGainLoss: Double
    )

    data class ProductivityMetrics(
        val completedTasks: Int,
        val totalTasks: Int,
        val completionRate: Double
    )
}
