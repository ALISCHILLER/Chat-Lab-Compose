package com.msa.chatlab.core.storage.dao

import androidx.room.*
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Query("SELECT * FROM outbox WHERE profile_id = :profileId ORDER BY created_at ASC")
    fun observe(profileId: String): Flow<List<OutboxItemEntity>>

    @Query("SELECT * FROM outbox WHERE profile_id = :profileId ORDER BY created_at ASC LIMIT 1")
    suspend fun peekFirst(profileId: String): OutboxItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(entity: OutboxItemEntity)

    @Query("UPDATE outbox SET attempt = attempt + 1 WHERE profile_id = :profileId AND message_id = :messageId")
    suspend fun incrementAttempt(profileId: String, messageId: String)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId AND message_id = :messageId")
    suspend fun remove(profileId: String, messageId: String)

    @Query("DELETE FROM outbox WHERE profile_id = :profileId")
    suspend fun clear(profileId: String)
}
