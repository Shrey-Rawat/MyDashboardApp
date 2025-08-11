package com.mydashboardapp

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import app.cash.turbine.test

@DisplayName("Example JUnit 5 Tests")
class ExampleJUnit5Test {

    @Test
    @DisplayName("Should demonstrate basic JUnit 5 functionality")
    fun `basic junit5 test`() {
        // Given
        val expected = "Hello, World!"
        
        // When
        val actual = "Hello, World!"
        
        // Then
        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @DisplayName("Should test with parameters")
    @ValueSource(ints = [1, 2, 3, 4, 5])
    fun `parameterized test example`(value: Int) {
        assertTrue(value > 0)
        assertTrue(value <= 5)
    }

    @Test
    @DisplayName("Should demonstrate MockK integration")
    fun `mockk integration test`() {
        // Given
        val mockObject = mockk<TestInterface>(relaxed = true)
        
        // When
        mockObject.doSomething()
        
        // Then
        verify { mockObject.doSomething() }
    }

    @Test
    @DisplayName("Should demonstrate Coroutines testing")
    fun `coroutines test with runTest`() = runTest {
        // Given
        val testFlow = flowOf(1, 2, 3)
        
        // When & Then
        testFlow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }

    interface TestInterface {
        fun doSomething()
    }
}
