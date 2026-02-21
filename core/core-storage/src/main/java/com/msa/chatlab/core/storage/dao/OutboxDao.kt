package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = :status")
    fun observeCountByStatus(profileId: String, status: OutboxStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = :status")
    suspend fun countByStatus(profileId: String, status: OutboxStatus): Int

    @Query("""
        SELECT * FROM outbox
        WHERE profile_id = :profileId AND status = :status
        ORDER BY created_at ASC
        LIMIT 1
    """)
    suspend fun getOldest(profileId: String, status: OutboxStatus = OutboxStatus.PENDING): OutboxItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OutboxItemEntity)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND message_id = :messageId")
    suspend fun delete(profileId: String, messageId: String)

    @Query("""
        UPDATE outbox
        SET attempt = :attempt,
            last_attempt_at = :lastAttemptAt,
            last_error = :error,
            status = :status,
            updated_at = :updatedAt
        WHERE profile_id = :profileId AND message_id = :messageId
    """)
    suspend fun updateAttempt(
        profileId: String,
        messageId: String,
        attempt: Int,
        status: OutboxStatus,
        error: String?,
        lastAttemptAt: Long?,
        updatedAt: Long
    )

    @Query("""
        SELECT * FROM outbox
        WHERE profile_id = :profileId AND status = :status
        ORDER BY created_at DESC
    """)
    fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItemEntity>>

    @Query("""
        UPDATE outbox
        SET status = :toStatus,
            updated_at = :updatedAt
        WHERE profile_id = :profileId AND status = :fromStatus
    """)
    suspend fun updateStatusForAll(profileId: String, fromStatus: OutboxStatus, toStatus: OutboxStatus, updatedAt: Long)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND status = :status")
    suspend fun deleteByStatus(profileId: String, status: OutboxStatus)
}
