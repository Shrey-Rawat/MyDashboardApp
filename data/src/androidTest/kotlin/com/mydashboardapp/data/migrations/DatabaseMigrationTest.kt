package com.mydashboardapp.data.migrations

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mydashboardapp.data.BestProductivityDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for Room database migrations
 * Tests that migrations preserve data and correctly transform schema
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BestProductivityDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_preservesExistingData() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data in version 1 schema
            execSQL("""
                INSERT INTO foods (id, name, calories, protein, carbs, fat, fiber, createdAt, updatedAt) 
                VALUES (1, 'Test Food', 100, 10, 20, 5, 3, 1234567890, 1234567890)
            """.trimIndent())

            execSQL("""
                INSERT INTO exercises (id, name, category, muscleGroup, instructions, createdAt, updatedAt) 
                VALUES (1, 'Push Up', 'Strength', 'Chest', 'Do push ups', 1234567890, 1234567890)
            """.trimIndent())
            close()
        }

        // Re-open the database with version 2 and provide MIGRATION_1_2 as the migration process
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, DatabaseMigrations.MIGRATION_1_2)

        // Verify that the data survived migration
        val foodsCursor = db.query("SELECT * FROM foods WHERE id = 1")
        assert(foodsCursor.moveToFirst())
        assert(foodsCursor.getString(foodsCursor.getColumnIndexOrThrow("name")) == "Test Food")
        
        // Verify new column was added with default value
        val isVerifiedIndex = foodsCursor.getColumnIndexOrThrow("isVerified")
        assert(foodsCursor.getInt(isVerifiedIndex) == 0) // Default false
        foodsCursor.close()

        // Verify exercises table got new column
        val exercisesCursor = db.query("SELECT * FROM exercises WHERE id = 1")
        assert(exercisesCursor.moveToFirst())
        
        val youtubeVideoIdIndex = exercisesCursor.getColumnIndex("youtubeVideoId")
        assert(youtubeVideoIdIndex != -1) // Column exists
        exercisesCursor.close()

        // Verify index was created
        val indexCursor = db.query("PRAGMA index_info(index_foods_isVerified)")
        assert(indexCursor.count > 0) // Index exists
        indexCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3_addsUserPreferencesTable() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            // Insert some data to ensure it's preserved
            execSQL("""
                INSERT INTO foods (id, name, calories, protein, carbs, fat, fiber, isVerified, createdAt, updatedAt) 
                VALUES (1, 'Test Food', 100, 10, 20, 5, 3, 1, 1234567890, 1234567890)
            """.trimIndent())
            close()
        }

        // Run migration from 2 to 3
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, DatabaseMigrations.MIGRATION_2_3)

        // Verify user_preferences table was created
        val tablesCursor = db.query("""
            SELECT name FROM sqlite_master WHERE type='table' AND name='user_preferences'
        """.trimIndent())
        assert(tablesCursor.moveToFirst())
        tablesCursor.close()

        // Verify table structure
        val columnsCursor = db.query("PRAGMA table_info(user_preferences)")
        val columns = mutableSetOf<String>()
        while (columnsCursor.moveToNext()) {
            columns.add(columnsCursor.getString(columnsCursor.getColumnIndexOrThrow("name")))
        }
        columnsCursor.close()

        assert(columns.contains("id"))
        assert(columns.contains("key"))
        assert(columns.contains("value"))
        assert(columns.contains("dataType"))
        assert(columns.contains("createdAt"))
        assert(columns.contains("updatedAt"))

        // Test inserting into new table
        db.execSQL("""
            INSERT INTO user_preferences (key, value, dataType, createdAt, updatedAt)
            VALUES ('test_key', 'test_value', 'STRING', 1234567890, 1234567890)
        """.trimIndent())

        val prefsCursor = db.query("SELECT * FROM user_preferences WHERE key = 'test_key'")
        assert(prefsCursor.moveToFirst())
        assert(prefsCursor.getString(prefsCursor.getColumnIndexOrThrow("value")) == "test_value")
        prefsCursor.close()

        // Verify existing data preserved
        val foodsCursor = db.query("SELECT * FROM foods WHERE id = 1")
        assert(foodsCursor.moveToFirst())
        assert(foodsCursor.getString(foodsCursor.getColumnIndexOrThrow("name")) == "Test Food")
        foodsCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate3To4_modifiesTasksTableStructure() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            // Create initial tasks table (simplified version)
            execSQL("""
                CREATE TABLE tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    isCompleted INTEGER NOT NULL DEFAULT 0,
                    priority TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())

            // Insert test data
            execSQL("""
                INSERT INTO tasks (id, title, description, isCompleted, priority, createdAt, updatedAt)
                VALUES (1, 'Test Task', 'Test Description', 0, 'HIGH', 1234567890, 1234567890)
            """.trimIndent())
            close()
        }

        // Run migration from 3 to 4
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, DatabaseMigrations.MIGRATION_3_4)

        // Verify data was preserved
        val tasksCursor = db.query("SELECT * FROM tasks WHERE id = 1")
        assert(tasksCursor.moveToFirst())
        assert(tasksCursor.getString(tasksCursor.getColumnIndexOrThrow("title")) == "Test Task")
        assert(tasksCursor.getString(tasksCursor.getColumnIndexOrThrow("description")) == "Test Description")
        tasksCursor.close()

        // Verify new columns exist
        val columnsCursor = db.query("PRAGMA table_info(tasks)")
        val columns = mutableSetOf<String>()
        while (columnsCursor.moveToNext()) {
            columns.add(columnsCursor.getString(columnsCursor.getColumnIndexOrThrow("name")))
        }
        columnsCursor.close()

        // Check for new columns added in migration
        assert(columns.contains("status"))
        assert(columns.contains("dueDate"))
        assert(columns.contains("completedAt"))
        assert(columns.contains("estimatedTimeMinutes"))
        assert(columns.contains("actualTimeMinutes"))
        assert(columns.contains("goalId"))
        assert(columns.contains("parentTaskId"))
        assert(columns.contains("category"))
        assert(columns.contains("tags"))
        assert(columns.contains("notes"))
        assert(columns.contains("reminderAt"))
        assert(columns.contains("recurrenceRule"))
        assert(columns.contains("sortOrder"))

        // Verify indexes were created
        val indexesCursor = db.query("PRAGMA index_list(tasks)")
        val indexes = mutableSetOf<String>()
        while (indexesCursor.moveToNext()) {
            indexes.add(indexesCursor.getString(indexesCursor.getColumnIndexOrThrow("name")))
        }
        indexesCursor.close()

        assert(indexes.contains("index_tasks_goalId"))
        assert(indexes.contains("index_tasks_parentTaskId"))
        assert(indexes.contains("index_tasks_sortOrder"))

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5_addsNotificationsTable() {
        var db = helper.createDatabase(TEST_DB, 4).apply {
            // Insert test data to ensure it's preserved
            execSQL("""
                INSERT INTO user_preferences (key, value, dataType, createdAt, updatedAt)
                VALUES ('test_pref', 'test_value', 'STRING', 1234567890, 1234567890)
            """.trimIndent())
            close()
        }

        // Run migration from 4 to 5
        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, DatabaseMigrations.MIGRATION_4_5)

        // Verify notifications table was created
        val tablesCursor = db.query("""
            SELECT name FROM sqlite_master WHERE type='table' AND name='notifications'
        """.trimIndent())
        assert(tablesCursor.moveToFirst())
        tablesCursor.close()

        // Test inserting into notifications table
        db.execSQL("""
            INSERT INTO notifications (title, message, type, isRead, isActioned, priority, createdAt, updatedAt)
            VALUES ('Test Notification', 'Test Message', 'INFO', 0, 0, 1, 1234567890, 1234567890)
        """.trimIndent())

        val notificationsCursor = db.query("SELECT * FROM notifications WHERE title = 'Test Notification'")
        assert(notificationsCursor.moveToFirst())
        assert(notificationsCursor.getString(notificationsCursor.getColumnIndexOrThrow("message")) == "Test Message")
        notificationsCursor.close()

        // Verify indexes were created for notifications
        val indexesCursor = db.query("PRAGMA index_list(notifications)")
        val indexes = mutableSetOf<String>()
        while (indexesCursor.moveToNext()) {
            indexes.add(indexesCursor.getString(indexesCursor.getColumnIndexOrThrow("name")))
        }
        indexesCursor.close()

        assert(indexes.contains("index_notifications_type"))
        assert(indexes.contains("index_notifications_isRead"))
        assert(indexes.contains("index_notifications_scheduledFor"))

        // Verify existing data preserved
        val prefsCursor = db.query("SELECT * FROM user_preferences WHERE key = 'test_pref'")
        assert(prefsCursor.moveToFirst())
        prefsCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll_fromVersion1ToLatest() {
        // Create database at version 1
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Insert comprehensive test data
            execSQL("""
                INSERT INTO foods (id, name, calories, protein, carbs, fat, fiber, createdAt, updatedAt) 
                VALUES (1, 'Migration Test Food', 150, 15, 25, 8, 5, 1234567890, 1234567890)
            """.trimIndent())

            execSQL("""
                INSERT INTO exercises (id, name, category, muscleGroup, instructions, createdAt, updatedAt) 
                VALUES (1, 'Migration Test Exercise', 'Cardio', 'Full Body', 'Test instructions', 1234567890, 1234567890)
            """.trimIndent())
            close()
        }

        // Run all migrations
        db = helper.runMigrationsAndValidate(
            TEST_DB, 
            5, // Latest version
            true, 
            DatabaseMigrations.MIGRATION_1_2,
            DatabaseMigrations.MIGRATION_2_3,
            DatabaseMigrations.MIGRATION_3_4,
            DatabaseMigrations.MIGRATION_4_5
        )

        // Verify all tables exist
        val tablesCursor = db.query("""
            SELECT name FROM sqlite_master WHERE type='table' 
            AND name IN ('foods', 'exercises', 'user_preferences', 'tasks', 'notifications')
        """.trimIndent())
        
        val tables = mutableSetOf<String>()
        while (tablesCursor.moveToNext()) {
            tables.add(tablesCursor.getString(0))
        }
        tablesCursor.close()

        assert(tables.contains("foods"))
        assert(tables.contains("exercises"))
        assert(tables.contains("user_preferences"))
        assert(tables.contains("tasks"))
        assert(tables.contains("notifications"))

        // Verify original data preserved
        val foodsCursor = db.query("SELECT * FROM foods WHERE id = 1")
        assert(foodsCursor.moveToFirst())
        assert(foodsCursor.getString(foodsCursor.getColumnIndexOrThrow("name")) == "Migration Test Food")
        assert(foodsCursor.getInt(foodsCursor.getColumnIndexOrThrow("isVerified")) == 0) // New column with default
        foodsCursor.close()

        val exercisesCursor = db.query("SELECT * FROM exercises WHERE id = 1")
        assert(exercisesCursor.moveToFirst())
        assert(exercisesCursor.getString(exercisesCursor.getColumnIndexOrThrow("name")) == "Migration Test Exercise")
        // youtubeVideoId should be null/empty (new column)
        exercisesCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testDatabaseOpen_withoutMigrations_shouldFail() {
        // Create database at version 1
        helper.createDatabase(TEST_DB, 1).close()

        // Try to open with current version without migrations - this should fail
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        try {
            val database = Room.databaseBuilder(
                context,
                BestProductivityDatabase::class.java,
                TEST_DB
            ).build()
            
            // This should fail because no migrations are provided
            database.nutritionDao().getAllFoods()
            
            assert(false) { "Should have thrown migration exception" }
        } catch (e: IllegalStateException) {
            // Expected - database version mismatch without migrations
            assert(e.message?.contains("Migration") == true)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testMigrationUtils_tableExists() {
        val db = helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        
        val reopenedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true, DatabaseMigrations.MIGRATION_1_2)
        
        // Test the MigrationUtils helper functions
        assert(MigrationUtils.tableExists(reopenedDb, "foods"))
        assert(MigrationUtils.tableExists(reopenedDb, "exercises"))
        assert(!MigrationUtils.tableExists(reopenedDb, "non_existent_table"))
        
        reopenedDb.close()
    }

    @Test
    @Throws(IOException::class)
    fun testMigrationUtils_columnExists() {
        val db = helper.createDatabase(TEST_DB, 1)
        
        // Before migration - isVerified column should not exist
        assert(!MigrationUtils.columnExists(db, "foods", "isVerified"))
        
        db.close()
        
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true, DatabaseMigrations.MIGRATION_1_2)
        
        // After migration - isVerified column should exist
        assert(MigrationUtils.columnExists(migratedDb, "foods", "isVerified"))
        assert(MigrationUtils.columnExists(migratedDb, "foods", "name")) // Original column
        assert(!MigrationUtils.columnExists(migratedDb, "foods", "non_existent_column"))
        
        migratedDb.close()
    }

    @Test
    @Throws(IOException::class)
    fun testForeignKeyConstraints_afterMigration() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            close()
        }

        // Migrate to version 4 which includes foreign key relationships
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, DatabaseMigrations.MIGRATION_3_4)

        // Enable foreign keys to test constraints
        MigrationUtils.enableForeignKeys(db)

        // Test that we can insert valid related data
        db.execSQL("""
            INSERT INTO goals (id, title, description, createdAt, updatedAt)
            VALUES (1, 'Test Goal', 'Test Description', 1234567890, 1234567890)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO tasks (title, goalId, createdAt, updatedAt)
            VALUES ('Test Task', 1, 1234567890, 1234567890)
        """.trimIndent())

        // Verify the data was inserted
        val tasksCursor = db.query("SELECT * FROM tasks WHERE title = 'Test Task'")
        assert(tasksCursor.moveToFirst())
        assert(tasksCursor.getLong(tasksCursor.getColumnIndexOrThrow("goalId")) == 1L)
        tasksCursor.close()

        db.close()
    }
}
