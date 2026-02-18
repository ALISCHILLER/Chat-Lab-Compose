package com.msa.chatlab.core.storage.dao

import androidx.room.*
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MessageEntity)

    @Query("SELECT * FROM messages WHERE profile_id = :profileId ORDER BY created_at ASC")
    fun observeByProfile(profileId: String): Flow<List<MessageEntity>>

    /*
    @Query(
        """
        SELECT m.* FROM messages m
        JOIN messages_fts f 
          ON f.profile_id = m.profile_id AND f.message_id = m.message_id
        WHERE m.profile_id = :profileId
          AND messages_fts MATCH :query
        ORDER BY m.created_at DESC
        LIMIT 200
        """
    )
    suspend fun searchFts(profileId: String, query: String): List<MessageEntity>
    */

    @Query(
        """
        UPDATE messages 
        SET queued = :queued, attempt = :attempt, status = :status, last_error = :lastError 
        WHERE profile_id = :profileId AND message_id = :messageId
        """
    )
    suspend fun updateDelivery(
        profileId: String,
        messageId: String,
        queued: Boolean,
        attempt: Int,
        status: String,
        lastError: String?
    )

    @Query("DELETE FROM messages WHERE profile_id = :profileId AND message_id = :messageId")
    suspend fun delete(profileId: String, messageId: String)

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun countAll(): Long
}
