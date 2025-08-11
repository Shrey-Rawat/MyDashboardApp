package com.mydashboardapp.inventory.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.mydashboardapp.data.dao.InventoryDao
// import com.mydashboardapp.MainActivity // MainActivity reference needs to be handled differently
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing stock level alerts and notifications
 */
@Singleton
class StockAlertService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inventoryDao: InventoryDao
) {
    
    companion object {
        private const val CHANNEL_ID = "stock_alerts"
        private const val CHANNEL_NAME = "Stock Alerts"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "stock_alert_check"
    }
    
    data class AlertSettings(
        val isEnabled: Boolean = true,
        val checkIntervalMinutes: Long = 60, // Check every hour by default
        val notificationEnabled: Boolean = true,
        val soundEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val minAlertThreshold: Int = 1 // Minimum number of low stock items to trigger alert
    )
    
    data class StockAlert(
        val itemId: Long,
        val itemName: String,
        val category: String?,
        val brand: String?,
        val currentStock: Int,
        val minimumStock: Int,
        val severity: AlertSeverity,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    enum class AlertSeverity {
        LOW,      // Stock is below minimum but not critical
        CRITICAL, // Stock is very low (less than 50% of minimum)
        OUT       // Stock is zero or negative
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Create notification channel for stock alerts
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for low stock items"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Start periodic stock alert checking
     */
    fun startPeriodicAlertCheck(settings: AlertSettings) {
        if (!settings.isEnabled) {
            stopPeriodicAlertCheck()
            return
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val alertCheckRequest = PeriodicWorkRequestBuilder<StockAlertWorker>(
            settings.checkIntervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            alertCheckRequest
        )
    }
    
    /**
     * Stop periodic stock alert checking
     */
    fun stopPeriodicAlertCheck() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME)
    }
    
    /**
     * Check for low stock items and generate alerts
     */
    suspend fun checkStockAlerts(settings: AlertSettings): List<StockAlert> {
        val lowStockItems = inventoryDao.getLowStockItems()
        val alerts = mutableListOf<StockAlert>()
        
        for (item in lowStockItems) {
            val severity = calculateSeverity(item.currentStock, item.minimumStock ?: 0)
            
            alerts.add(
                StockAlert(
                    itemId = item.id,
                    itemName = item.name,
                    category = item.category,
                    brand = item.brand,
                    currentStock = item.currentStock,
                    minimumStock = item.minimumStock ?: 0,
                    severity = severity
                )
            )
        }
        
        // Send notification if threshold is met
        if (settings.notificationEnabled && alerts.size >= settings.minAlertThreshold) {
            sendStockAlertNotification(alerts, settings)
        }
        
        return alerts
    }
    
    /**
     * Calculate alert severity based on stock levels
     */
    private fun calculateSeverity(currentStock: Int, minimumStock: Int): AlertSeverity {
        return when {
            currentStock <= 0 -> AlertSeverity.OUT
            currentStock < minimumStock * 0.5 -> AlertSeverity.CRITICAL
            else -> AlertSeverity.LOW
        }
    }
    
    /**
     * Send notification for stock alerts
     */
    private fun sendStockAlertNotification(alerts: List<StockAlert>, settings: AlertSettings) {
        val criticalCount = alerts.count { it.severity == AlertSeverity.CRITICAL }
        val outCount = alerts.count { it.severity == AlertSeverity.OUT }
        val lowCount = alerts.count { it.severity == AlertSeverity.LOW }
        
        val title = when {
            outCount > 0 -> "⚠️ $outCount items out of stock"
            criticalCount > 0 -> "⚠️ $criticalCount critically low items"
            else -> "⚠️ $lowCount items low in stock"
        }
        
        val content = buildString {
            if (outCount > 0) append("$outCount out of stock")
            if (criticalCount > 0) {
                if (outCount > 0) append(", ")
                append("$criticalCount critically low")
            }
            if (lowCount > 0 && (outCount > 0 || criticalCount > 0)) {
                append(", $lowCount low")
            } else if (lowCount > 0) {
                append("$lowCount items need restocking")
            }
        }
        
        // Create a generic intent to launch the app main activity
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "inventory/alerts")
        } ?: Intent()
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Add sound and vibration based on settings
        if (!settings.soundEnabled) {
            builder.setSilent(true)
        }
        
        if (!settings.vibrationEnabled) {
            builder.setVibrate(null)
        }
        
        // Add expandable content for multiple items
        if (alerts.size > 1) {
            val bigTextStyle = NotificationCompat.BigTextStyle()
            val expandedText = alerts.take(10).joinToString("\n") { alert ->
                val statusIcon = when (alert.severity) {
                    AlertSeverity.OUT -> "❌"
                    AlertSeverity.CRITICAL -> "⚠️"
                    AlertSeverity.LOW -> "🔶"
                }
                "$statusIcon ${alert.itemName}: ${alert.currentStock}/${alert.minimumStock}"
            }
            bigTextStyle.bigText(expandedText)
            builder.setStyle(bigTextStyle)
        }
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Handle case where notification permissions are not granted
        }
    }
    
    /**
     * Manual stock alert check (for immediate use)
     */
    suspend fun performImmediateStockCheck(settings: AlertSettings): List<StockAlert> {
        return checkStockAlerts(settings)
    }
    
    /**
     * Get alert history or summary
     */
    suspend fun getAlertSummary(): AlertSummary {
        val lowStockItems = inventoryDao.getLowStockItems()
        
        val outOfStockCount = lowStockItems.count { it.currentStock <= 0 }
        val criticalCount = lowStockItems.count { 
            it.currentStock > 0 && it.currentStock < (it.minimumStock ?: 0) * 0.5 
        }
        val lowStockCount = lowStockItems.count { 
            it.currentStock >= (it.minimumStock ?: 0) * 0.5 && 
            it.currentStock < (it.minimumStock ?: 0)
        }
        
        return AlertSummary(
            totalAlertsCount = lowStockItems.size,
            outOfStockCount = outOfStockCount,
            criticalCount = criticalCount,
            lowStockCount = lowStockCount,
            lastCheckTime = System.currentTimeMillis()
        )
    }
    
    data class AlertSummary(
        val totalAlertsCount: Int,
        val outOfStockCount: Int,
        val criticalCount: Int,
        val lowStockCount: Int,
        val lastCheckTime: Long
    )
}

/**
 * Background worker for periodic stock alert checking
 */
class StockAlertWorker(
    context: Context,
    params: WorkerParameters
) : androidx.work.CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // This would be injected in a real implementation
            // For now, we'll return success
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
