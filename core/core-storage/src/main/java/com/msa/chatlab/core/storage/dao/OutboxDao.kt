package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = :status")
    fun observeCountByStatus(profileId: String, status: OutboxStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = :status")
    suspend fun countByStatus(profileId: String, status: OutboxStatus): Int

    @Query(
        """
        SELECT * FROM outbox
        WHERE profile_id = :profileId AND status = :status
        ORDER BY created_at ASC
        LIMIT 1
        """
    )
    suspend fun getOldest(profileId: String, status: OutboxStatus = OutboxStatus.PENDING): OutboxItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OutboxItemEntity)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND message_id = :messageId")
    suspend fun delete(profileId: String, messageId: String)

    @Query(
        """
        UPDATE outbox
        SET attempt = :attempt,
            last_attempt_at = :lastAttemptAt,
            last_error = :error,
            status = :status,
            updated_at = :updatedAt
        WHERE profile_id = :profileId AND message_id = :messageId
        """
    )
    suspend fun updateAttempt(
        profileId: String,
        messageId: String,
        attempt: Int,
        status: OutboxStatus,
        error: String?,
        lastAttemptAt: Long?,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE outbox
        SET status = :newStatus,
            last_attempt_at = :lastAttemptAt,
            updated_at = :updatedAt
        WHERE profile_id = :profileId
          AND message_id = :messageId
          AND status = :expectedStatus
        """
    )
    suspend fun compareAndSetStatus(
        profileId: String,
        messageId: String,
        expectedStatus: OutboxStatus,
        newStatus: OutboxStatus,
        lastAttemptAt: Long?,
        updatedAt: Long
    ): Int

    @Query(
        """
        SELECT * FROM outbox
        WHERE profile_id = :profileId AND status = :status
        ORDER BY created_at DESC
        """
    )
    fun observeByStatus(profileId: String, status: OutboxStatus): Flow<List<OutboxItemEntity>>

    @Query(
        """
        UPDATE outbox
        SET status = :toStatus,
            updated_at = :updatedAt
        WHERE profile_id = :profileId
          AND status = :fromStatus
          AND (last_attempt_at IS NULL OR last_attempt_at < :olderThan)
        """
    )
    suspend fun requeueExpired(
        profileId: String,
        fromStatus: OutboxStatus,
        toStatus: OutboxStatus,
        olderThan: Long,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE outbox
        SET status = :toStatus,
            updated_at = :updatedAt
        WHERE profile_id = :profileId AND status = :fromStatus
        """
    )
    suspend fun updateStatusForAll(profileId: String, fromStatus: OutboxStatus, toStatus: OutboxStatus, updatedAt: Long)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND status = :status")
    suspend fun deleteByStatus(profileId: String, status: OutboxStatus)

    /**
     * ✅ فاز 2.2: claim اتمیکِ batch در یک transaction
     */
    @Transaction
    suspend fun claimPendingBatchTx(
        profileId: String,
        now: Long,
        limit: Int,
        maxSpin: Int = 3
    ): List<OutboxItemEntity> {
        val result = ArrayList<OutboxItemEntity>(limit.coerceAtMost(256))
        val hardLimit = limit.coerceIn(1, 256)

        var taken = 0
        while (taken < hardLimit) {
            var spin = 0
            var claimed: OutboxItemEntity? = null

            while (spin < maxSpin) {
                val candidate = getOldest(profileId, OutboxStatus.PENDING) ?: break
                val updated = compareAndSetStatus(
                    profileId = profileId,
                    messageId = candidate.messageId,
                    expectedStatus = OutboxStatus.PENDING,
                    newStatus = OutboxStatus.IN_FLIGHT,
                    lastAttemptAt = now,
                    updatedAt = now
                )
                if (updated == 1) {
                    claimed = candidate.copy(status = OutboxStatus.IN_FLIGHT, lastAttemptAt = now, updatedAt = now)
                    break
                }
                spin++
            }

            if (claimed == null) break
            result.add(claimed)
            taken++
        }

        return result
    }
}