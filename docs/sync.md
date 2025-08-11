# Synchronization and Conflict Resolution

This document outlines the synchronization strategy and conflict resolution policies for the Best Productivity App.

## Overview

The app uses a bidirectional synchronization system that allows data to be synchronized between multiple devices and the cloud backend. The sync system is designed to handle conflicts gracefully while preserving user data integrity.

## Synchronization Architecture

### Components

1. **Local Database**: SQLite database storing local data
2. **Remote Data Source**: Abstract interface for cloud synchronization
3. **Sync Manager**: Coordinates synchronization operations
4. **Conflict Resolver**: Handles data conflicts during sync

### Sync Flow

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Device A  │    │   Backend   │    │   Device B  │
│             │    │             │    │             │
│ 1. Push     │───▶│ 2. Store    │    │             │
│    Changes  │    │    Changes  │    │             │
│             │    │             │    │ 3. Pull     │
│ 6. Pull     │◀───│ 5. Retrieve │◀───│    Updates  │
│    Updates  │    │    Updates  │    │             │
│             │    │ 4. Process  │    │             │
│ 7. Resolve  │    │    Conflicts│    │             │
│    Conflicts│    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘
```

## Conflict Types

### 1. Both Modified (BOTH_MODIFIED)
**Scenario**: The same entity was modified on both local and remote sides since last sync.

**Resolution Strategy**:
- **Default**: Use timestamp-based resolution (most recent wins)
- **User Data**: Prompt user to choose or merge
- **System Data**: Use predefined merge strategies

**Example**:
```kotlin
// Local: Food(id=1, name="Apple", calories=95, lastModified=1000)
// Remote: Food(id=1, name="Green Apple", calories=92, lastModified=1100)
// Resolution: Use remote (more recent timestamp)
```

### 2. Local Deleted, Remote Modified (LOCAL_DELETED_REMOTE_MODIFIED)
**Scenario**: Local entity was deleted but remote entity was modified.

**Resolution Strategy**:
- **Default**: Restore entity with remote modifications
- **Critical Data**: Prompt user for confirmation
- **Temporary Data**: Use remote version

### 3. Local Modified, Remote Deleted (LOCAL_MODIFIED_REMOTE_DELETED)
**Scenario**: Local entity was modified but remote entity was deleted.

**Resolution Strategy**:
- **Default**: Keep local modifications and sync to remote
- **Shared Data**: Prompt user for confirmation
- **Personal Data**: Preserve local changes

### 4. Duplicate Creation (DUPLICATE_CREATION)
**Scenario**: Same entity created on both sides with different IDs.

**Resolution Strategy**:
- **Default**: Merge entities using content-based deduplication
- **Unique Constraints**: Keep both if they differ significantly
- **Identical Content**: Remove duplicate, keep one

## Resolution Policies by Entity Type

### Food Items
- **Conflict Type**: `BOTH_MODIFIED`
- **Policy**: User choice with smart merge suggestions
- **Merge Strategy**: Combine nutritional data, prefer user-provided names

```kotlin
data class FoodMergeStrategy(
    val namePreference: ConflictResolution = CONFLICT_RESOLUTION_MANUAL,
    val nutritionalDataPreference: ConflictResolution = CONFLICT_RESOLUTION_MERGE,
    val metadataPreference: ConflictResolution = CONFLICT_RESOLUTION_USE_REMOTE
)
```

### Meals
- **Conflict Type**: `BOTH_MODIFIED`
- **Policy**: Timestamp-based with meal component merge
- **Merge Strategy**: Combine foods from both versions if no conflicts

### Workouts
- **Conflict Type**: `LOCAL_MODIFIED_REMOTE_DELETED`
- **Policy**: Always preserve local modifications
- **Rationale**: User's workout data is personal and should not be lost

### Training Plans
- **Conflict Type**: `DUPLICATE_CREATION`
- **Policy**: Merge if similar, keep both if different
- **Merge Strategy**: Compare exercise lists and goals

## Conflict Resolution Flow

### Automatic Resolution
1. **Check Entity Type**: Apply type-specific policies
2. **Evaluate Timestamps**: Use recency for system-generated data
3. **Content Analysis**: Detect meaningful differences
4. **Apply Resolution**: Execute chosen strategy

### Manual Resolution
1. **Present Conflict**: Show both versions to user
2. **Provide Options**: 
   - Keep Local
   - Use Remote
   - Smart Merge
   - Manual Edit
3. **Save Decision**: Store resolution preference for similar conflicts
4. **Apply Globally**: Option to apply resolution to similar pending conflicts

## Implementation Guidelines

### RemoteDataSource Methods

```kotlin
abstract class RemoteDataSource<T, ID> {
    open suspend fun pushChanges(changes: List<PendingChange<T>>): PushResult<T>
    open suspend fun pullUpdates(lastSyncTimestamp: Long): PullResult<T>
}
```

### Default No-op Implementation
The base `RemoteDataSource` provides no-op implementations for `pushChanges()` and `pullUpdates()`:

```kotlin
open suspend fun pushChanges(changes: List<PendingChange<T>>): PushResult<T> {
    return PushResult(
        successful = emptyList(),
        failed = emptyList(),
        conflicts = emptyList()
    )
}
```

This allows offline-only data sources to work without implementing sync functionality.

### Conflict Storage
Unresolved conflicts are stored locally and presented to users during app usage:

```kotlin
data class StoredConflict(
    val conflictId: String,
    val entityId: String,
    val entityType: String,
    val localData: ByteArray,
    val remoteData: ByteArray,
    val timestamp: Long,
    val status: ConflictStatus = ConflictStatus.PENDING
)
```

## Performance Considerations

### Batch Operations
- Group related changes in single sync operations
- Use pagination for large datasets
- Implement incremental sync for efficiency

### Conflict Minimization
- Frequent sync intervals to reduce conflict likelihood
- Optimistic locking with version numbers
- Field-level conflict detection where applicable

### Network Optimization
- Compress sync payloads
- Use delta sync for large entities
- Implement offline-first architecture

## Error Handling

### Network Failures
- Store pending changes locally
- Retry with exponential backoff
- Provide offline-first user experience

### Sync Failures
- Log detailed error information
- Preserve user data integrity
- Provide recovery mechanisms

### Data Corruption
- Validate data before applying changes
- Maintain data backups
- Implement rollback capabilities

## Testing Strategy

### Unit Tests
- Test each conflict resolution policy
- Verify no-op implementation behavior
- Test edge cases and error conditions

### Integration Tests
- End-to-end sync scenarios
- Multi-device conflict resolution
- Network failure recovery

### User Acceptance Tests
- Conflict resolution user experience
- Data integrity validation
- Performance under load

## Future Enhancements

### Advanced Conflict Resolution
- Machine learning-based conflict prediction
- Semantic merge for text fields
- User behavior-based resolution preferences

### Real-time Sync
- WebSocket-based real-time updates
- Operational transformation for concurrent edits
- Live collaboration features

### Cross-Platform Sync
- Web application integration
- Third-party service synchronization
- Import/export functionality

## Configuration

The sync system can be configured through application settings:

```kotlin
data class SyncConfiguration(
    val syncIntervalMinutes: Int = 15,
    val batchSize: Int = 100,
    val retryAttempts: Int = 3,
    val conflictResolutionStrategy: GlobalConflictStrategy = GlobalConflictStrategy.TIMESTAMP_BASED,
    val enableRealtimeSync: Boolean = false,
    val enableBackgroundSync: Boolean = true
)
```

## Monitoring and Analytics

### Sync Metrics
- Sync success/failure rates
- Conflict frequency by entity type
- Resolution strategy effectiveness
- Network usage and performance

### User Behavior
- Conflict resolution choices
- Sync frequency preferences
- Feature usage patterns

### System Health
- Backend performance metrics
- Error rates and types
- Data consistency validation
