package com.mydashboardapp.productivity.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydashboardapp.core.ui.BaseViewModel
import com.mydashboardapp.data.entities.PomodoroType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    // private val pomodoroRepository: PomodoroRepository,
    // private val userPreferencesRepository: UserPreferencesRepository,
    // private val notificationManager: PomodoroNotificationManager
) : BaseViewModel<PomodoroUiState>(
    initialState = PomodoroUiState()
) {
    
    private var timerJob: Job? = null
    
    fun onSessionTypeChanged(type: PomodoroType) {
        updateState { currentState ->
            val defaultDuration = when (type) {
                PomodoroType.WORK -> 25
                PomodoroType.SHORT_BREAK -> 5
                PomodoroType.LONG_BREAK -> 15
            }
            currentState.copy(
                selectedType = type,
                customDuration = defaultDuration
            )
        }
    }
    
    fun onDurationChanged(minutes: Int) {
        updateState { currentState ->
            currentState.copy(customDuration = minutes)
        }
    }
    
    fun startSession() {
        val currentState = uiState.value
        if (currentState.currentSession != null) return
        
        val session = PomodoroSessionState(
            type = currentState.selectedType,
            totalDurationMinutes = currentState.customDuration,
            remainingSeconds = currentState.customDuration * 60,
            isRunning = false
        )
        
        updateState { it.copy(currentSession = session) }
    }
    
    fun toggleTimer() {
        val session = uiState.value.currentSession ?: return
        
        if (session.isRunning) {
            pauseTimer()
        } else {
            resumeTimer()
        }
    }
    
    private fun resumeTimer() {
        val session = uiState.value.currentSession ?: return
        
        updateState { currentState ->
            currentState.copy(
                currentSession = session.copy(isRunning = true)
            )
        }
        
        timerJob = viewModelScope.launch {
            while (uiState.value.currentSession?.isRunning == true && 
                   uiState.value.currentSession?.remainingSeconds ?: 0 > 0) {
                delay(1000L)
                
                val currentSession = uiState.value.currentSession
                if (currentSession != null && currentSession.remainingSeconds > 0) {
                    updateState { currentState ->
                        currentState.copy(
                            currentSession = currentSession.copy(
                                remainingSeconds = currentSession.remainingSeconds - 1
                            )
                        )
                    }
                } else {
                    // Session completed
                    onSessionComplete()
                    break
                }
            }
        }
    }
    
    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        
        updateState { currentState ->
            val session = currentState.currentSession
            currentState.copy(
                currentSession = session?.copy(isRunning = false)
            )
        }
    }
    
    fun cancelSession() {
        timerJob?.cancel()
        timerJob = null
        
        updateState { currentState ->
            currentState.copy(currentSession = null)
        }
    }
    
    private fun onSessionComplete() {
        val session = uiState.value.currentSession ?: return
        
        // Save completed session to repository
        viewModelScope.launch {
            // pomodoroRepository.saveCompletedSession(session.toPomodoroSession())
            loadTodayStats()
        }
        
        // Show completion notification
        // notificationManager.showSessionCompleteNotification(session.type)
        
        updateState { currentState ->
            currentState.copy(currentSession = null)
        }
    }
    
    private fun loadTodayStats() {
        viewModelScope.launch {
            // Load today's statistics from repository
            val stats = PomodoroStats(
                completedSessions = 4,
                focusTimeMinutes = 100,
                currentStreak = 7
            )
            
            updateState { currentState ->
                currentState.copy(todayStats = stats)
            }
        }
    }
    
    private fun loadRecentSessions() {
        viewModelScope.launch {
            // Load recent sessions from repository
            val recentSessions = listOf(
                // Sample data - replace with actual repository call
            )
            
            updateState { currentState ->
                currentState.copy(recentSessions = recentSessions)
            }
        }
    }
    
    init {
        loadTodayStats()
        loadRecentSessions()
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class PomodoroUiState(
    val selectedType: PomodoroType = PomodoroType.WORK,
    val customDuration: Int = 25,
    val currentSession: PomodoroSessionState? = null,
    val todayStats: PomodoroStats = PomodoroStats(),
    val recentSessions: List<PomodoroSession> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : com.mydashboardapp.core.ui.UiState

data class PomodoroSessionState(
    val type: PomodoroType,
    val totalDurationMinutes: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = false
)

data class PomodoroStats(
    val completedSessions: Int = 0,
    val focusTimeMinutes: Int = 0,
    val currentStreak: Int = 0
)

data class PomodoroSession(
    val id: Long = 0,
    val type: PomodoroType,
    val durationMinutes: Int,
    val completedAt: Long,
    val wasCompleted: Boolean
)
