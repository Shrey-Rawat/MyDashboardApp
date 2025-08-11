package com.mydashboardapp.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Base ViewModel class that provides standardized state management using Kotlin Flows.
 * All ViewModels should extend this class to ensure consistent state handling patterns.
 *
 * @param S the type of UiState this ViewModel manages
 * @property initialState the initial state for the UiState
 */
abstract class BaseViewModel<S : UiState>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /**
     * Current state value for convenient access
     */
    protected val currentState: S
        get() = _uiState.value

    /**
     * Updates the UI state
     */
    protected fun updateState(newState: S) {
        _uiState.value = newState
    }

    /**
     * Updates the UI state using a reducer function
     */
    protected fun updateState(reducer: (S) -> S) {
        _uiState.value = reducer(currentState)
    }

    /**
     * Exception handler for coroutines that automatically handles errors
     */
    protected open val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Launches a coroutine in the viewModelScope with automatic error handling
     */
    protected fun launchWithErrorHandling(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    /**
     * Handles errors by updating the state. Override this method to customize error handling.
     */
    protected open fun handleError(throwable: Throwable) {
        val errorMessage = throwable.message ?: "An unknown error occurred"
        // If the current state supports error messages, update it
        when (val state = currentState) {
            is StandardUiState<*> -> {
                @Suppress("UNCHECKED_CAST")
                updateState(
                    state.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    ) as S
                )
            }
            is LoadingUiState -> {
                @Suppress("UNCHECKED_CAST")
                updateState(
                    state.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    ) as S
                )
            }
        }
    }

    /**
     * Clears any error messages from the current state
     */
    protected fun clearError() {
        when (val state = currentState) {
            is StandardUiState<*> -> {
                @Suppress("UNCHECKED_CAST")
                updateState(state.copy(errorMessage = null) as S)
            }
            is LoadingUiState -> {
                @Suppress("UNCHECKED_CAST")
                updateState(state.copy(errorMessage = null) as S)
            }
        }
    }

    /**
     * Sets loading state
     */
    protected fun setLoading(isLoading: Boolean) {
        when (val state = currentState) {
            is StandardUiState<*> -> {
                @Suppress("UNCHECKED_CAST")
                updateState(state.copy(isLoading = isLoading) as S)
            }
            is LoadingUiState -> {
                @Suppress("UNCHECKED_CAST")
                updateState(state.copy(isLoading = isLoading) as S)
            }
        }
    }
}

/**
 * Simple non-generic base ViewModel for ViewModels that manage their own state
 * without using the UiState pattern. Provides common functionality like loading
 * and error handling.
 */
abstract class SimpleBaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Exception handler for coroutines that automatically handles errors
     */
    protected open val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        setError(throwable.message ?: "An unknown error occurred")
    }

    /**
     * Launches a coroutine in the viewModelScope with automatic error handling
     */
    protected fun launchWithErrorHandling(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    /**
     * Sets loading state
     */
    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    /**
     * Sets error message
     */
    protected fun setError(message: String) {
        _errorMessage.value = message
        _isLoading.value = false
    }

    /**
     * Clears error message
     */
    protected fun clearError() {
        _errorMessage.value = null
    }
}
