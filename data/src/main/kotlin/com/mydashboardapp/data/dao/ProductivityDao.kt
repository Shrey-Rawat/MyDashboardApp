package com.mydashboardapp.data.dao

import androidx.room.*
import com.mydashboardapp.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductivityDao {
    
    // Goal operations
    @Query("SELECT * FROM goals WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveGoals(): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): Goal?
    
    @Query("SELECT * FROM goals WHERE category = :category AND isArchived = 0")
    suspend fun getGoalsByCategory(category: String): List<Goal>
    
    @Query("SELECT * FROM goals WHERE isCompleted = 0 AND isArchived = 0")
    suspend fun getIncompleteGoals(): List<Goal>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long
    
    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Delete
    suspend fun deleteGoal(goal: Goal)
    
    // Task operations
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    fun getActiveTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE goalId = :goalId")
    suspend fun getTasksByGoalId(goalId: Long): List<Task>
    
    @Query("SELECT * FROM tasks WHERE parentTaskId = :parentTaskId")
    suspend fun getSubtasks(parentTaskId: Long): List<Task>
    
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate")
    suspend fun getTasksByDueDate(startDate: Long, endDate: Long): List<Task>
    
    @Query("SELECT * FROM tasks WHERE category = :category")
    suspend fun getTasksByCategory(category: String): List<Task>
    
    @Query("SELECT * FROM tasks WHERE priority = :priority AND isCompleted = 0")
    suspend fun getTasksByPriority(priority: String): List<Task>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    // TimeLog operations
    @Query("SELECT * FROM time_logs ORDER BY startTime DESC")
    fun getAllTimeLogs(): Flow<List<TimeLog>>
    
    @Query("SELECT * FROM time_logs WHERE id = :id")
    suspend fun getTimeLogById(id: Long): TimeLog?
    
    @Query("SELECT * FROM time_logs WHERE taskId = :taskId")
    suspend fun getTimeLogsByTaskId(taskId: Long): List<TimeLog>
    
    @Query("SELECT * FROM time_logs WHERE goalId = :goalId")
    suspend fun getTimeLogsByGoalId(goalId: Long): List<TimeLog>
    
    @Query("SELECT * FROM time_logs WHERE startTime BETWEEN :startDate AND :endDate")
    suspend fun getTimeLogsByDateRange(startDate: Long, endDate: Long): List<TimeLog>
    
    @Query("SELECT * FROM time_logs WHERE category = :category")
    suspend fun getTimeLogsByCategory(category: String): List<TimeLog>
    
    @Query("SELECT * FROM time_logs WHERE endTime IS NULL")
    suspend fun getActiveTimeLogs(): List<TimeLog>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeLog(timeLog: TimeLog): Long
    
    @Update
    suspend fun updateTimeLog(timeLog: TimeLog)
    
    @Delete
    suspend fun deleteTimeLog(timeLog: TimeLog)
    
    // Analytics queries
    @Query("""
        SELECT COUNT(*) as totalTasks,
               SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as completedTasks,
               AVG(CASE WHEN isCompleted = 1 AND actualTimeMinutes IS NOT NULL THEN actualTimeMinutes END) as avgTimeMinutes
        FROM tasks
        WHERE createdAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getTaskSummary(startDate: Long, endDate: Long): TaskSummary
    
    @Query("""
        SELECT SUM(duration) as totalDuration,
               COUNT(*) as totalSessions,
               AVG(productivity) as avgProductivity
        FROM time_logs
        WHERE startTime BETWEEN :startDate AND :endDate
        AND duration IS NOT NULL
        AND isBreak = 0
    """)
    suspend fun getTimeTrackingSummary(startDate: Long, endDate: Long): TimeTrackingSummary
    
    @Query("""
        SELECT category, SUM(duration) as totalTime
        FROM time_logs
        WHERE startTime BETWEEN :startDate AND :endDate
        AND duration IS NOT NULL
        AND category IS NOT NULL
        GROUP BY category
        ORDER BY totalTime DESC
    """)
    suspend fun getTimeByCategory(startDate: Long, endDate: Long): List<CategoryTime>
    
    @Query("""
        SELECT g.*, 
               COUNT(t.id) as taskCount,
               SUM(CASE WHEN t.isCompleted = 1 THEN 1 ELSE 0 END) as completedTaskCount
        FROM goals g
        LEFT JOIN tasks t ON g.id = t.goalId
        WHERE g.isArchived = 0
        GROUP BY g.id
    """)
    suspend fun getGoalsWithTaskCounts(): List<GoalWithTaskCount>
    
    data class TaskSummary(
        val totalTasks: Int,
        val completedTasks: Int,
        val avgTimeMinutes: Double?
    )
    
    data class TimeTrackingSummary(
        val totalDuration: Long,
        val totalSessions: Int,
        val avgProductivity: Double?
    )
    
    data class CategoryTime(
        val category: String,
        val totalTime: Long
    )
    
    data class GoalWithTaskCount(
        val id: Long,
        val title: String,
        val description: String?,
        val category: String?,
        val targetDate: Long?,
        val isCompleted: Boolean,
        val progress: Float,
        val priority: String?,
        val taskCount: Int,
        val completedTaskCount: Int
    )
    
    // Pomodoro operations
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE id = :id")
    suspend fun getPomodoroSessionById(id: Long): PomodoroSession?
    
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId")
    suspend fun getPomodoroSessionsByTaskId(taskId: Long): List<PomodoroSession>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE startTime BETWEEN :startDate AND :endDate")
    suspend fun getPomodoroSessionsByDateRange(startDate: Long, endDate: Long): List<PomodoroSession>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE endTime IS NULL")
    suspend fun getActivePomodoroSession(): PomodoroSession?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroSession): Long
    
    @Update
    suspend fun updatePomodoroSession(session: PomodoroSession)
    
    @Delete
    suspend fun deletePomodoroSession(session: PomodoroSession)
    
    // Streak operations
    @Query("SELECT * FROM streaks WHERE isActive = 1 ORDER BY priority DESC, name ASC")
    fun getActiveStreaks(): Flow<List<Streak>>
    
    @Query("SELECT * FROM streaks WHERE id = :id")
    suspend fun getStreakById(id: Long): Streak?
    
    @Query("SELECT * FROM streaks WHERE goalId = :goalId")
    suspend fun getStreaksByGoalId(goalId: Long): List<Streak>
    
    @Query("SELECT * FROM streaks WHERE category = :category AND isActive = 1")
    suspend fun getStreaksByCategory(category: String): List<Streak>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: Streak): Long
    
    @Update
    suspend fun updateStreak(streak: Streak)
    
    @Delete
    suspend fun deleteStreak(streak: Streak)
    
    // Streak log operations
    @Query("SELECT * FROM streak_logs WHERE streakId = :streakId ORDER BY date DESC")
    suspend fun getStreakLogs(streakId: Long): List<StreakLog>
    
    @Query("SELECT * FROM streak_logs WHERE streakId = :streakId AND date BETWEEN :startDate AND :endDate")
    suspend fun getStreakLogsByDateRange(streakId: Long, startDate: Long, endDate: Long): List<StreakLog>
    
    @Query("SELECT * FROM streak_logs WHERE date = :date")
    suspend fun getStreakLogsForDate(date: Long): List<StreakLog>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakLog(log: StreakLog): Long
    
    @Update
    suspend fun updateStreakLog(log: StreakLog)
    
    @Delete
    suspend fun deleteStreakLog(log: StreakLog)
    
    // Analytics for Pomodoro
    @Query("""
        SELECT COUNT(*) as totalSessions,
               SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as completedSessions,
               AVG(productivity) as avgProductivity
        FROM pomodoro_sessions
        WHERE startTime BETWEEN :startDate AND :endDate
        AND type = 'WORK'
    """)
    suspend fun getPomodoroSummary(startDate: Long, endDate: Long): PomodoroSummary
    
    // Analytics for Streaks
    @Query("""
        SELECT s.*, 
               COALESCE(MAX(sl.date), 0) as lastLogDate,
               COUNT(sl.id) as totalLogs
        FROM streaks s
        LEFT JOIN streak_logs sl ON s.id = sl.streakId
        WHERE s.isActive = 1
        GROUP BY s.id
        ORDER BY s.currentStreak DESC
    """)
    suspend fun getStreaksWithStats(): List<StreakWithStats>
    
    data class PomodoroSummary(
        val totalSessions: Int,
        val completedSessions: Int,
        val avgProductivity: Double?
    )
    
    data class StreakWithStats(
        val id: Long,
        val name: String,
        val description: String?,
        val type: StreakType,
        val currentStreak: Int,
        val longestStreak: Int,
        val totalCount: Int,
        val lastLogDate: Long,
        val totalLogs: Int,
        val frequency: StreakFrequency,
        val targetValue: Int,
        val category: String?,
        val isActive: Boolean
    )
}
