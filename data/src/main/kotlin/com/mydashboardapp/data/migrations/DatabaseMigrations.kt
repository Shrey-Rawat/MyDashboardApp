package com.mydashboardapp.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration definitions for BestProductivityDatabase
 * 
 * Each migration should:
 * 1. Have a descriptive comment explaining what changed
 * 2. Include SQL statements to modify the schema
 * 3. Handle data preservation when necessary
 */
object DatabaseMigrations {
    
    /**
     * Migration from version 1 to 2
     * Example: Add new column to existing table
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example migration - add new columns to existing tables
            
            // Add new columns to foods table
            database.execSQL("""
                ALTER TABLE foods ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0
            """.trimIndent())
            
            // Add new columns to exercises table
            database.execSQL("""
                ALTER TABLE exercises ADD COLUMN youtubeVideoId TEXT
            """.trimIndent())
            
            // Add index for performance
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_foods_isVerified ON foods(isVerified)
            """.trimIndent())
        }
    }
    
    /**
     * Migration from version 2 to 3
     * Example: Add new table
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add new table for user preferences
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS user_preferences (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    key TEXT NOT NULL UNIQUE,
                    value TEXT NOT NULL,
                    dataType TEXT NOT NULL DEFAULT 'STRING',
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            database.execSQL("""
                CREATE UNIQUE INDEX IF NOT EXISTS index_user_preferences_key ON user_preferences(key)
            """.trimIndent())
        }
    }
    
    /**
     * Migration from version 3 to 4
     * Example: Modify existing table structure
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Change column type or constraints
            
            // Create new table with updated schema
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS tasks_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    isCompleted INTEGER NOT NULL DEFAULT 0,
                    priority TEXT,
                    status TEXT DEFAULT 'Not Started',
                    dueDate INTEGER,
                    completedAt INTEGER,
                    estimatedTimeMinutes INTEGER,
                    actualTimeMinutes INTEGER,
                    goalId INTEGER,
                    parentTaskId INTEGER,
                    category TEXT,
                    tags TEXT,
                    notes TEXT,
                    reminderAt INTEGER,
                    recurrenceRule TEXT,
                    sortOrder INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(goalId) REFERENCES goals(id) ON DELETE SET NULL,
                    FOREIGN KEY(parentTaskId) REFERENCES tasks(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Copy data from old table to new table
            database.execSQL("""
                INSERT INTO tasks_new (
                    id, title, description, isCompleted, priority, status,
                    dueDate, completedAt, estimatedTimeMinutes, actualTimeMinutes,
                    goalId, parentTaskId, category, tags, notes, reminderAt,
                    recurrenceRule, createdAt, updatedAt
                )
                SELECT 
                    id, title, description, isCompleted, priority, status,
                    dueDate, completedAt, estimatedTimeMinutes, actualTimeMinutes,
                    goalId, parentTaskId, category, tags, notes, reminderAt,
                    recurrenceRule, createdAt, updatedAt
                FROM tasks
            """.trimIndent())
            
            // Drop old table
            database.execSQL("DROP TABLE tasks")
            
            // Rename new table
            database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            
            // Recreate indexes
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_tasks_goalId ON tasks(goalId)
            """.trimIndent())
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_tasks_parentTaskId ON tasks(parentTaskId)
            """.trimIndent())
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_tasks_sortOrder ON tasks(sortOrder)
            """.trimIndent())
        }
    }
    
    /**
     * Migration from version 4 to 5
     * Example: Add new entity with relationships
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new table for notifications
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    type TEXT NOT NULL,
                    entityType TEXT,
                    entityId INTEGER,
                    isRead INTEGER NOT NULL DEFAULT 0,
                    isActioned INTEGER NOT NULL DEFAULT 0,
                    scheduledFor INTEGER,
                    sentAt INTEGER,
                    actionUrl TEXT,
                    priority INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_notifications_type ON notifications(type)
            """.trimIndent())
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_notifications_isRead ON notifications(isRead)
            """.trimIndent())
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_notifications_scheduledFor ON notifications(scheduledFor)
            """.trimIndent())
        }
    }
    
    /**
     * Get all migrations as an array
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5
        )
    }
}

/**
 * Helper class for complex migration operations
 */
object MigrationUtils {
    
    /**
     * Create a temporary table name
     */
    fun getTempTableName(tableName: String): String {
        return "${tableName}_temp_${System.currentTimeMillis()}"
    }
    
    /**
     * Get table info query
     */
    fun getTableInfo(tableName: String): String {
        return "PRAGMA table_info($tableName)"
    }
    
    /**
     * Get foreign key list query
     */
    fun getForeignKeyList(tableName: String): String {
        return "PRAGMA foreign_key_list($tableName)"
    }
    
    /**
     * Get index list query
     */
    fun getIndexList(tableName: String): String {
        return "PRAGMA index_list($tableName)"
    }
    
    /**
     * Enable foreign keys
     */
    fun enableForeignKeys(database: SupportSQLiteDatabase) {
        database.execSQL("PRAGMA foreign_keys = ON")
    }
    
    /**
     * Disable foreign keys (useful during migrations)
     */
    fun disableForeignKeys(database: SupportSQLiteDatabase) {
        database.execSQL("PRAGMA foreign_keys = OFF")
    }
    
    /**
     * Check if table exists
     */
    fun tableExists(database: SupportSQLiteDatabase, tableName: String): Boolean {
        val cursor = database.query("""
            SELECT COUNT(*) FROM sqlite_master 
            WHERE type='table' AND name='$tableName'
        """.trimIndent())
        
        return try {
            cursor.moveToFirst()
            cursor.getInt(0) > 0
        } catch (e: Exception) {
            false
        } finally {
            cursor.close()
        }
    }
    
    /**
     * Check if column exists in table
     */
    fun columnExists(database: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
        val cursor = database.query("PRAGMA table_info($tableName)")
        
        return try {
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                if (name == columnName) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        } finally {
            cursor.close()
        }
    }
}
