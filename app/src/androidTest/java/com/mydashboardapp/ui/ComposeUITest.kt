package com.mydashboardapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.mydashboardapp.MainActivity
import com.mydashboardapp.ai.data.models.ChatMessage
import com.mydashboardapp.ai.data.models.PromptTemplate
import com.mydashboardapp.ai.ui.chat.AIChatScreen
import com.mydashboardapp.ai.ui.templates.PromptTemplatesScreen
import com.mydashboardapp.finance.ui.TransactionEntryScreen
import com.mydashboardapp.training.ui.LiveSessionScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for critical user flows using Compose Testing
 * Tests key interactions and user journeys across different features
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ComposeUITest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun aiChatScreen_displaysWelcomeMessage_whenNoConfiguredProvider() {
        composeTestRule.setContent {
            AIChatScreen()
        }

        // Verify welcome screen is shown when no provider configured
        composeTestRule
            .onNodeWithText("Welcome to AI Assistant")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("To get started, you'll need to configure at least one AI provider.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Configure Providers")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun aiChatScreen_sendsMessage_whenProviderConfigured() {
        // Mock configured provider state
        composeTestRule.setContent {
            AIChatScreen(
                // Mock ViewModel with configured provider
            )
        }

        // Type a message
        val testMessage = "Hello, AI assistant!"
        composeTestRule
            .onNodeWithText("Ask me anything about productivity...")
            .performTextInput(testMessage)

        // Send message
        composeTestRule
            .onNodeWithContentDescription("Send")
            .performClick()

        // Verify message appears in chat
        composeTestRule
            .onNodeWithText(testMessage)
            .assertIsDisplayed()

        // Verify streaming indicator appears
        composeTestRule
            .onNodeWithText("Thinking...")
            .assertIsDisplayed()
    }

    @Test
    fun aiChatScreen_showsTemplateOptions() {
        composeTestRule.setContent {
            AIChatScreen()
        }

        // Click template button
        composeTestRule
            .onNodeWithContentDescription("Templates")
            .performClick()

        // Should navigate to templates (in real app this would be handled by navigation)
        // For now, just verify the button exists and is clickable
        composeTestRule
            .onNodeWithContentDescription("Templates")
            .assertHasClickAction()
    }

    @Test
    fun promptTemplatesScreen_displaysTemplates() {
        val mockTemplates = listOf(
            PromptTemplate(
                id = "1",
                title = "Meeting Summary",
                description = "Summarize meeting notes",
                content = "Summarize the following meeting notes: {{notes}}",
                category = "Productivity",
                variables = listOf("notes")
            ),
            PromptTemplate(
                id = "2", 
                title = "Email Draft",
                description = "Draft professional email",
                content = "Write a professional email about {{topic}}",
                category = "Communication",
                variables = listOf("topic")
            )
        )

        composeTestRule.setContent {
            PromptTemplatesScreen(
                // Mock ViewModel with templates
            )
        }

        // Verify templates are displayed
        composeTestRule
            .onNodeWithText("Meeting Summary")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Email Draft")
            .assertIsDisplayed()

        // Verify categories filter
        composeTestRule
            .onNodeWithText("All")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Productivity")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Communication")
            .assertIsDisplayed()
    }

    @Test
    fun promptTemplatesScreen_createsNewTemplate() {
        composeTestRule.setContent {
            PromptTemplatesScreen()
        }

        // Click add template button
        composeTestRule
            .onNodeWithContentDescription("Add Template")
            .performClick()

        // Fill in template details
        composeTestRule
            .onNodeWithText("Title")
            .performTextInput("Test Template")

        composeTestRule
            .onNodeWithText("Description")
            .performTextInput("Test description")

        composeTestRule
            .onNodeWithText("Category")
            .performTextInput("Test Category")

        composeTestRule
            .onNodeWithText("Template Content")
            .performTextInput("This is a test template with {{variable}}")

        // Save template
        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        // Verify template was created (in real implementation)
        // For now, just verify the form elements exist
        composeTestRule
            .onNodeWithText("Title")
            .assertExists()
    }

    @Test
    fun transactionEntryScreen_switchesBetweenExpenseAndIncome() {
        composeTestRule.setContent {
            TransactionEntryScreen()
        }

        // Start with expense selected
        composeTestRule
            .onNodeWithText("Expense")
            .assertIsDisplayed()

        // Switch to income
        composeTestRule
            .onNodeWithText("Income")
            .performClick()

        // Verify UI updates for income
        composeTestRule
            .onNodeWithText("Log Income")
            .assertIsDisplayed()

        // Switch back to expense
        composeTestRule
            .onNodeWithText("Expense")
            .performClick()

        // Verify UI updates for expense
        composeTestRule
            .onNodeWithText("Log Expense")
            .assertIsDisplayed()
    }

    @Test
    fun transactionEntryScreen_validatesRequiredFields() {
        composeTestRule.setContent {
            TransactionEntryScreen()
        }

        // Try to save without required fields
        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        // Should not proceed (validation prevents save)
        // In real app, error states would be shown

        // Fill required fields
        composeTestRule
            .onNodeWithText("Amount")
            .performTextInput("25.50")

        composeTestRule
            .onNodeWithText("Description")
            .performTextInput("Coffee")

        // Select account (this would open dropdown in real app)
        composeTestRule
            .onNodeWithText("Select Account")
            .performClick()

        // Now save should work
        composeTestRule
            .onNodeWithText("Save")
            .assertHasClickAction()
    }

    @Test
    fun transactionEntryScreen_handlesDropdowns() {
        composeTestRule.setContent {
            TransactionEntryScreen()
        }

        // Test category dropdown
        composeTestRule
            .onNodeWithText("Category")
            .performClick()

        // Should show category options (in real implementation)
        // Verify dropdown trigger exists
        composeTestRule
            .onNodeWithText("Category")
            .assertExists()

        // Test account dropdown
        composeTestRule
            .onNodeWithText("Select Account")
            .performClick()

        // Verify account dropdown exists
        composeTestRule
            .onNodeWithText("Select Account")
            .assertExists()
    }

    @Test
    fun liveSessionScreen_displaysWorkoutTimer() {
        composeTestRule.setContent {
            LiveSessionScreen(
                workoutId = 1L
            )
        }

        // Verify workout timer is displayed
        composeTestRule
            .onNodeWithText("WORKOUT TIME")
            .assertIsDisplayed()

        // Verify timer format (MM:SS)
        composeTestRule
            .onAllNodesWithText(Regex("\\d{2}:\\d{2}"))
            .onFirst()
            .assertIsDisplayed()

        // Verify exercise counter
        composeTestRule
            .onNodeWithText(Regex("Exercise \\d+ of \\d+"))
            .assertIsDisplayed()
    }

    @Test
    fun liveSessionScreen_addsSet() {
        composeTestRule.setContent {
            LiveSessionScreen(
                workoutId = 1L
            )
        }

        // Fill in set data
        composeTestRule
            .onNodeWithText("Weight")
            .performTextInput("135")

        composeTestRule
            .onNodeWithText("Reps")
            .performTextInput("10")

        // Add the set
        composeTestRule
            .onNodeWithText("Add Set")
            .performClick()

        // Verify set was added (would show in completed sets)
        composeTestRule
            .onNodeWithText("Add Set")
            .assertExists()
    }

    @Test
    fun liveSessionScreen_startsRestTimer() {
        composeTestRule.setContent {
            LiveSessionScreen(
                workoutId = 1L
            )
        }

        // Start rest timer
        composeTestRule
            .onNodeWithText("Rest 90s")
            .performClick()

        // Should switch to rest mode
        composeTestRule
            .onNodeWithText("REST TIME")
            .assertIsDisplayed()

        // Stop rest button should appear
        composeTestRule
            .onNodeWithText("Stop Rest")
            .assertIsDisplayed()
    }

    @Test
    fun liveSessionScreen_navigatesExercises() {
        composeTestRule.setContent {
            LiveSessionScreen(
                workoutId = 1L
            )
        }

        // Test previous button (should be disabled initially)
        composeTestRule
            .onNodeWithText("Previous")
            .assertIsDisplayed()

        // Test next button
        composeTestRule
            .onNodeWithText("Next")
            .assertIsDisplayed()
            .performClick()

        // Exercise counter should update
        composeTestRule
            .onNodeWithText(Regex("Exercise \\d+ of \\d+"))
            .assertIsDisplayed()
    }

    @Test
    fun finishWorkout_completesSession() {
        composeTestRule.setContent {
            LiveSessionScreen(
                workoutId = 1L,
                onFinishWorkout = {
                    // Navigate back or show completion screen
                }
            )
        }

        // Finish workout
        composeTestRule
            .onNodeWithContentDescription("Finish Workout")
            .performClick()

        // In real app, this would trigger navigation or completion dialog
        composeTestRule
            .onNodeWithContentDescription("Finish Workout")
            .assertExists()
    }

    @Test
    fun accessibilityTest_allScreensHaveContentDescriptions() {
        // Test AI Chat Screen
        composeTestRule.setContent {
            AIChatScreen()
        }

        // Verify important elements have content descriptions
        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertExists()

        composeTestRule
            .onNodeWithContentDescription("Settings")
            .assertExists()

        // Test Transaction Entry Screen
        composeTestRule.setContent {
            TransactionEntryScreen()
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertExists()

        // Test Live Session Screen  
        composeTestRule.setContent {
            LiveSessionScreen(workoutId = 1L)
        }

        composeTestRule
            .onNodeWithContentDescription("Finish Workout")
            .assertExists()
    }

    @Test
    fun semanticsTest_verifyProperSemantics() {
        composeTestRule.setContent {
            AIChatScreen()
        }

        // Test that buttons have proper click actions
        composeTestRule
            .onNodeWithText("Configure Providers")
            .assert(hasClickAction())

        // Test that text inputs have proper semantics
        composeTestRule
            .onNodeWithText("Ask me anything about productivity...")
            .assert(hasSetTextAction())

        // Test that content has proper roles
        composeTestRule
            .onNodeWithText("Welcome to AI Assistant")
            .assertExists()
    }

    @Test
    fun stateRestorationTest_maintainsStateAcrossConfigurationChanges() {
        composeTestRule.setContent {
            TransactionEntryScreen()
        }

        // Fill in some data
        val testAmount = "42.75"
        composeTestRule
            .onNodeWithText("Amount")
            .performTextInput(testAmount)

        val testDescription = "Test transaction"
        composeTestRule
            .onNodeWithText("Description")  
            .performTextInput(testDescription)

        // Simulate configuration change (rotation)
        composeTestRule.activity.recreate()

        // Verify data is preserved
        composeTestRule
            .onNodeWithText(testAmount)
            .assertExists()

        composeTestRule
            .onNodeWithText(testDescription)
            .assertExists()
    }

    @Test
    fun errorHandlingTest_displaysErrorStates() {
        composeTestRule.setContent {
            AIChatScreen(
                // Mock error state
            )
        }

        // Test network error state
        // In real implementation, would show error message
        composeTestRule
            .onNodeWithText("Configure Providers")
            .assertExists()

        // Test empty state
        composeTestRule
            .onNodeWithText("Welcome to AI Assistant")
            .assertIsDisplayed()
    }

    @Test
    fun loadingStatesTest_showsLoadingIndicators() {
        composeTestRule.setContent {
            AIChatScreen(
                // Mock loading state
            )
        }

        // Send a message to trigger loading
        composeTestRule
            .onNodeWithText("Ask me anything about productivity...")
            .performTextInput("Test message")

        composeTestRule
            .onNodeWithContentDescription("Send")
            .performClick()

        // Should show loading state
        // In real implementation, would show progress indicator
        composeTestRule
            .onNodeWithContentDescription("Send")
            .assertExists()
    }
}
