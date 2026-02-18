package com.msa.chatlab.core.storage.dao

import androidx.room.*
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = 'PENDING'")
    fun observePendingCount(profileId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE profile_id = :profileId AND status = 'PENDING'")
    suspend fun pendingCount(profileId: String): Int

    @Query("""
        SELECT * FROM outbox 
        WHERE profile_id = :profileId AND status = 'PENDING'
        ORDER BY created_at ASC
        LIMIT 1
    """)
    suspend fun getOldestPending(profileId: String): OutboxItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OutboxItemEntity)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND message_id = :messageId")
    suspend fun remove(profileId: String, messageId: String)

    @Query("""
        UPDATE outbox 
        SET attempt = :attempt,
            status = :status,
            last_error = :error,
            updated_at = :updatedAt
        WHERE profile_id = :profileId AND message_id = :messageId
    """)
    suspend fun updateAttempt(
        profileId: String,
        messageId: String,
        attempt: Int,
        status: String,
        error: String?,
        updatedAt: Long
    )
}
