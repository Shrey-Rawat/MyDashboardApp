package com.mydashboardapp.core.ui

/**
 * Base interface for all UI state representations in the app.
 * All screen states should implement this interface.
 */
interface UiState

/**
 * Standard implementation of UiState that handles loading states and error messages.
 *
 * @param T the type of data being managed
 * @property data the current data state
 * @property isLoading indicates if an async operation is in progress
 * @property errorMessage contains error message if an error occurred
 * @property isEmpty indicates if the data is considered empty
 */
data class StandardUiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
) : UiState

/**
 * Simple loading state for basic operations
 */
data class LoadingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState
