package com.mydashboardapp.core.data

import kotlinx.coroutines.flow.Flow

/**
 * Simple base repository class for temporary build fixes
 */
abstract class BaseRepository {
    // Placeholder implementation
}

/**
 * Base repository interface that defines common CRUD operations for entities.
 * All domain-specific repositories should extend this interface.
 *
 * @param T the entity type
 * @param ID the ID type (typically Long)
 */
interface BaseRepositoryInterface<T, ID> {
    fun getAll(): Flow<List<T>>
    suspend fun getById(id: ID): T?
    suspend fun insert(entity: T): ID
    suspend fun insertAll(entities: List<T>): List<ID>
    suspend fun update(entity: T)
    suspend fun updateAll(entities: List<T>)
    suspend fun delete(entity: T)
    suspend fun deleteById(id: ID)
    suspend fun deleteAll(entities: List<T>)
}

/**
 * Base remote data source interface for handling network operations.
 * Implementations should handle API calls and network error handling.
 *
 * @param T the entity type
 * @param ID the ID type
 */
abstract class RemoteDataSource<T, ID> {
    abstract suspend fun getAll(): List<T>
    abstract suspend fun getById(id: ID): T?
    abstract suspend fun create(entity: T): T
    abstract suspend fun update(id: ID, entity: T): T
    abstract suspend fun delete(id: ID): Boolean
    abstract suspend fun sync(lastSyncTimestamp: Long): SyncResult<T>
    
    /**
     * Push local changes to remote server.
     * Default implementation is no-op for offline-only data sources.
     *
     * @param changes The list of pending changes to push
     * @return PushResult indicating success/failure and any conflicts
     */
    open suspend fun pushChanges(changes: List<PendingChange<T>>): PushResult<T> {
        // Default no-op implementation
        return PushResult(
            successful = emptyList(),
            failed = emptyList(),
            conflicts = emptyList()
        )
    }
    
    /**
     * Pull updates from remote server since last sync.
     * Default implementation is no-op for offline-only data sources.
     *
     * @param lastSyncTimestamp Timestamp of last successful sync
     * @return PullResult containing new updates and conflicts
     */
    open suspend fun pullUpdates(lastSyncTimestamp: Long): PullResult<T> {
        // Default no-op implementation
        return PullResult(
            updates = emptyList(),
            conflicts = emptyList(),
            newSyncTimestamp = System.currentTimeMillis()
        )
    }
}

/**
 * Legacy interface for backward compatibility
 * @deprecated Use RemoteDataSource abstract class instead
 */
interface BaseRemoteDataSource<T, ID> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: ID): T?
    suspend fun create(entity: T): T
    suspend fun update(id: ID, entity: T): T
    suspend fun delete(id: ID): Boolean
    suspend fun sync(lastSyncTimestamp: Long): SyncResult<T>
}

/**
 * Result wrapper for sync operations
 */
data class SyncResult<T>(
    val created: List<T> = emptyList(),
    val updated: List<T> = emptyList(),
    val deleted: List<Long> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a pending change to be synchronized
 */
data class PendingChange<T>(
    val entity: T,
    val operation: ChangeOperation,
    val timestamp: Long,
    val localId: String? = null,
    val remoteId: String? = null
)

/**
 * Types of changes that can be synchronized
 */
enum class ChangeOperation {
    CREATE, UPDATE, DELETE
}

/**
 * Result of pushing changes to remote server
 */
data class PushResult<T>(
    val successful: List<PendingChange<T>>,
    val failed: List<PendingChange<T>>,
    val conflicts: List<SyncConflict<T>>
)

/**
 * Result of pulling updates from remote server
 */
data class PullResult<T>(
    val updates: List<T>,
    val conflicts: List<SyncConflict<T>>,
    val newSyncTimestamp: Long
)

/**
 * Represents a synchronization conflict
 */
data class SyncConflict<T>(
    val localEntity: T?,
    val remoteEntity: T?,
    val conflictType: ConflictType,
    val timestamp: Long,
    val entityId: String
)

/**
 * Types of synchronization conflicts
 */
enum class ConflictType {
    BOTH_MODIFIED,
    LOCAL_DELETED_REMOTE_MODIFIED,
    LOCAL_MODIFIED_REMOTE_DELETED,
    DUPLICATE_CREATION
}

/**
 * Base repository implementation that handles local/remote data coordination.
 * This class provides the foundation for repositories that need to work with both
 * local database (DAOs) and remote data sources.
 *
 * @param T the entity type
 * @param ID the ID type
 * @param localDataSource the local DAO
 * @param remoteDataSource optional remote data source for sync
 */
abstract class BaseRepositoryImpl<T, ID, LocalDao>(
    protected val localDataSource: LocalDao,
    protected val remoteDataSource: BaseRemoteDataSource<T, ID>? = null
) : BaseRepositoryInterface<T, ID> {

    /**
     * Syncs data from remote source to local database if remote data source is available
     */
    suspend fun syncWithRemote(lastSyncTimestamp: Long = 0L): SyncResult<T>? {
        return remoteDataSource?.let { remote ->
            try {
                val result = remote.sync(lastSyncTimestamp)
                
                // Handle created items
                if (result.created.isNotEmpty()) {
                    handleRemoteCreated(result.created)
                }
                
                // Handle updated items
                if (result.updated.isNotEmpty()) {
                    handleRemoteUpdated(result.updated)
                }
                
                // Handle deleted items
                if (result.deleted.isNotEmpty()) {
                    handleRemoteDeleted(result.deleted)
                }
                
                result
            } catch (e: Exception) {
                // Log error and return null to indicate sync failure
                null
            }
        }
    }

    /**
     * Handle items created on remote
     */
    protected abstract suspend fun handleRemoteCreated(items: List<T>)
    
    /**
     * Handle items updated on remote
     */
    protected abstract suspend fun handleRemoteUpdated(items: List<T>)
    
    /**
     * Handle items deleted on remote
     */
    protected abstract suspend fun handleRemoteDeleted(deletedIds: List<Long>)
}
