package com.mydashboardapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule
import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentationTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.mydashboardapp.free.debug", appContext.packageName)
    }

    @Test
    fun testMockKAndroidIntegration() {
        // Verify MockK Android works in instrumentation tests
        val mockObject = mockk<TestInterface>(relaxed = true)
        assertNotNull(mockObject)
    }

    // Example Compose UI test (uncomment when MainActivity exists)
    /*
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMainActivityCompose() {
        composeTestRule.onNodeWithText("Hello Android!")
            .assertIsDisplayed()
    }
    */

    interface TestInterface {
        fun doSomething()
    }
}
