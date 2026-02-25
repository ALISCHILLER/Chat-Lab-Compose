package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.msa.chatlab.core.domain.model.OutboxStatus
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OutboxItemEntity)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND message_id = :messageId")
    suspend fun delete(profileId: String, messageId: String)

    @Query("SELECT * FROM outbox WHERE profile_id = :profileId AND status = :status ORDER BY created_at ASC")
    fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItemEntity>>

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = :status")
    suspend fun countByStatus(profileId: String, status: OutboxStatus): Int

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = :status")
    fun observeCountByStatus(profileId: String, status: OutboxStatus): Flow<Int>

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND status = :status")
    suspend fun deleteByStatus(profileId: String, status: OutboxStatus)

    @Query("UPDATE outbox SET status = :toStatus, updated_at = :now WHERE profile_id = :profileId AND status = :fromStatus")
    suspend fun updateStatus(profileId: String, fromStatus: OutboxStatus, toStatus: OutboxStatus, now: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE outbox
        SET attempt = :attempt,
            status = :status,
            last_error = :error,
            last_attempt_at = :now,
            updated_at = :now
        WHERE profile_id = :profileId AND message_id = :messageId
        """
    )
    suspend fun updateAttempt(
        profileId: String,
        messageId: String,
        attempt: Int,
        status: OutboxStatus,
        error: String?,
        now: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE outbox
        SET status = 'PENDING',
            updated_at = :now
        WHERE profile_id = :profileId
          AND status = 'IN_FLIGHT'
          AND (last_attempt_at IS NULL OR last_attempt_at < :expireBefore)
        """
    )
    suspend fun requeueStaleInflightTx(profileId: String, expireBefore: Long, now: Long = System.currentTimeMillis()): Int

    @Query(
        """
        SELECT * FROM outbox
        WHERE profile_id = :profileId
          AND status = 'PENDING'
        ORDER BY created_at ASC
        LIMIT :limit
        """
    )
    suspend fun getPendingBatch(profileId: String, limit: Int): List<OutboxItemEntity>

    @Transaction
    suspend fun claimPendingBatchTx(profileId: String, now: Long, limit: Int): List<OutboxItemEntity> {
        val batch = getPendingBatch(profileId, limit)
        batch.forEach {
            updateAttempt(
                profileId = it.profileId,
                messageId = it.messageId,
                attempt = it.attempt,
                status = OutboxStatus.IN_FLIGHT,
                error = it.lastError,
                now = now
            )
        }
        return batch
    }
}
