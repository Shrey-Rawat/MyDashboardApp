package com.mydashboardapp.sync

import com.mydashboardapp.core.data.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Example implementation of RemoteDataSource using gRPC for synchronization.
 * This demonstrates how the abstract RemoteDataSource can be extended for actual sync implementations.
 *
 * @param T the entity type
 * @param ID the ID type
 */
@Singleton
class GrpcRemoteDataSource<T, ID> @Inject constructor(
    private val channel: ManagedChannel,
    private val serializer: EntitySerializer<T>
) : RemoteDataSource<T, ID>() {

    // Legacy CRUD operations (implemented for backward compatibility)
    override suspend fun getAll(): List<T> = withContext(Dispatchers.IO) {
        // Implementation would call gRPC service to get all entities
        // This is a placeholder implementation
        emptyList()
    }

    override suspend fun getById(id: ID): T? = withContext(Dispatchers.IO) {
        // Implementation would call gRPC service to get entity by ID
        // This is a placeholder implementation
        null
    }

    override suspend fun create(entity: T): T = withContext(Dispatchers.IO) {
        // Implementation would call gRPC service to create entity
        // This is a placeholder implementation
        entity
    }

    override suspend fun update(id: ID, entity: T): T = withContext(Dispatchers.IO) {
        // Implementation would call gRPC service to update entity
        // This is a placeholder implementation
        entity
    }

    override suspend fun delete(id: ID): Boolean = withContext(Dispatchers.IO) {
        // Implementation would call gRPC service to delete entity
        // This is a placeholder implementation
        true
    }

    override suspend fun sync(lastSyncTimestamp: Long): SyncResult<T> = withContext(Dispatchers.IO) {
        // Legacy sync implementation
        // This would be replaced by the new push/pull methods
        SyncResult()
    }

    // New sync methods with actual gRPC implementation
    override suspend fun pushChanges(changes: List<PendingChange<T>>): PushResult<T> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Convert to protobuf messages
            val protobufChanges = changes.map { change ->
                PendingChangeProto.newBuilder()
                    .setEntityId(change.localId ?: "")
                    .setEntityType(determineEntityType(change.entity))
                    .setOperation(convertToProtoOperation(change.operation))
                    .setEntityData(serializer.serialize(change.entity))
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(change.timestamp / 1000)
                        .setNanos((change.timestamp % 1000 * 1000000).toInt())
                        .build())
                    .build()
            }

            // Create gRPC request
            val request = PushChangesRequest.newBuilder()
                .setUserId(getCurrentUserId())
                .setDeviceId(getCurrentDeviceId())
                .addAllChanges(protobufChanges)
                .setLastSyncTimestamp(com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(System.currentTimeMillis() / 1000)
                    .build())
                .build()

            // Call gRPC service
            val stub = SyncServiceGrpcKt.SyncServiceCoroutineStub(channel)
            val response = stub.pushChanges(request)

            // Convert response back to Kotlin data classes
            PushResult(
                successful = response.successfulList.map { convertFromProto(it) },
                failed = response.failedList.map { convertFromProto(it) },
                conflicts = response.conflictsList.map { convertConflictFromProto(it) }
            )
        } catch (e: Exception) {
            // Handle gRPC errors
            PushResult(
                successful = emptyList(),
                failed = changes, // Mark all as failed
                conflicts = emptyList()
            )
        }
    }

    override suspend fun pullUpdates(lastSyncTimestamp: Long): PullResult<T> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Create gRPC request
            val request = PullUpdatesRequest.newBuilder()
                .setUserId(getCurrentUserId())
                .setDeviceId(getCurrentDeviceId())
                .setLastSyncTimestamp(com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(lastSyncTimestamp / 1000)
                    .setNanos((lastSyncTimestamp % 1000 * 1000000).toInt())
                    .build())
                .build()

            // Call gRPC service
            val stub = SyncServiceGrpcKt.SyncServiceCoroutineStub(channel)
            val response = stub.pullUpdates(request)

            // Convert response back to Kotlin data classes
            PullResult(
                updates = response.updatesList.map { update ->
                    serializer.deserialize(update.entityData.toByteArray())
                },
                conflicts = response.conflictsList.map { convertConflictFromProto(it) },
                newSyncTimestamp = response.newSyncTimestamp.seconds * 1000 + 
                    response.newSyncTimestamp.nanos / 1000000
            )
        } catch (e: Exception) {
            // Handle gRPC errors
            PullResult(
                updates = emptyList(),
                conflicts = emptyList(),
                newSyncTimestamp = System.currentTimeMillis()
            )
        }
    }

    // Helper methods
    private fun determineEntityType(entity: T): EntityType {
        // This would be implemented based on the actual entity type
        return EntityType.ENTITY_TYPE_UNSPECIFIED
    }

    private fun convertToProtoOperation(operation: ChangeOperation): ChangeOperationProto {
        return when (operation) {
            ChangeOperation.CREATE -> ChangeOperationProto.CHANGE_OPERATION_CREATE
            ChangeOperation.UPDATE -> ChangeOperationProto.CHANGE_OPERATION_UPDATE
            ChangeOperation.DELETE -> ChangeOperationProto.CHANGE_OPERATION_DELETE
        }
    }

    private fun convertFromProto(proto: PendingChangeProto): PendingChange<T> {
        return PendingChange(
            entity = serializer.deserialize(proto.entityData.toByteArray()),
            operation = convertFromProtoOperation(proto.operation),
            timestamp = proto.timestamp.seconds * 1000 + proto.timestamp.nanos / 1000000,
            localId = proto.localId,
            remoteId = proto.remoteId
        )
    }

    private fun convertFromProtoOperation(operation: ChangeOperationProto): ChangeOperation {
        return when (operation) {
            ChangeOperationProto.CHANGE_OPERATION_CREATE -> ChangeOperation.CREATE
            ChangeOperationProto.CHANGE_OPERATION_UPDATE -> ChangeOperation.UPDATE
            ChangeOperationProto.CHANGE_OPERATION_DELETE -> ChangeOperation.DELETE
            else -> ChangeOperation.CREATE
        }
    }

    private fun convertConflictFromProto(proto: SyncConflictProto): SyncConflict<T> {
        return SyncConflict(
            localEntity = if (proto.localEntityData.isEmpty) null 
                else serializer.deserialize(proto.localEntityData.toByteArray()),
            remoteEntity = if (proto.remoteEntityData.isEmpty) null 
                else serializer.deserialize(proto.remoteEntityData.toByteArray()),
            conflictType = convertConflictTypeFromProto(proto.conflictType),
            timestamp = proto.timestamp.seconds * 1000 + proto.timestamp.nanos / 1000000,
            entityId = proto.entityId
        )
    }

    private fun convertConflictTypeFromProto(type: ConflictTypeProto): ConflictType {
        return when (type) {
            ConflictTypeProto.CONFLICT_TYPE_BOTH_MODIFIED -> ConflictType.BOTH_MODIFIED
            ConflictTypeProto.CONFLICT_TYPE_LOCAL_DELETED_REMOTE_MODIFIED -> ConflictType.LOCAL_DELETED_REMOTE_MODIFIED
            ConflictTypeProto.CONFLICT_TYPE_LOCAL_MODIFIED_REMOTE_DELETED -> ConflictType.LOCAL_MODIFIED_REMOTE_DELETED
            ConflictTypeProto.CONFLICT_TYPE_DUPLICATE_CREATION -> ConflictType.DUPLICATE_CREATION
            else -> ConflictType.BOTH_MODIFIED
        }
    }

    private fun getCurrentUserId(): String {
        // This would be obtained from authentication service
        return "user_123"
    }

    private fun getCurrentDeviceId(): String {
        // This would be obtained from device identification service
        return "device_456"
    }
}

/**
 * Interface for serializing entities to byte arrays for gRPC transmission
 */
interface EntitySerializer<T> {
    fun serialize(entity: T): ByteArray
    fun deserialize(data: ByteArray): T
}

/**
 * No-op implementation for offline-only scenarios
 */
class NoOpRemoteDataSource<T, ID> : RemoteDataSource<T, ID>() {
    override suspend fun getAll(): List<T> = emptyList()
    override suspend fun getById(id: ID): T? = null
    override suspend fun create(entity: T): T = entity
    override suspend fun update(id: ID, entity: T): T = entity
    override suspend fun delete(id: ID): Boolean = true
    override suspend fun sync(lastSyncTimestamp: Long): SyncResult<T> = SyncResult()
    
    // pushChanges and pullUpdates use the default no-op implementations
    // from the abstract class, so no need to override them
}
