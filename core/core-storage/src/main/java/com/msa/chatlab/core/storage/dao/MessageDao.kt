package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.domain.model.ConversationRow
import com.msa.chatlab.core.domain.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MessageEntity)

    @Query("SELECT * FROM messages WHERE profile_id = :profileId ORDER BY created_at ASC")
    fun observeByProfile(profileId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE profile_id = :profileId AND destination = :destination ORDER BY created_at ASC")
    fun observeConversation(profileId: String, destination: String): Flow<List<MessageEntity>>

    @Query(
        """
        UPDATE messages 
        SET queued = :queued, attempt = :attempt, status = :status, last_error = :lastError, updated_at = :updatedAt
        WHERE profile_id = :profileId AND message_id = :messageId
        """
    )
    suspend fun updateDelivery(
        profileId: String,
        messageId: String,
        queued: Boolean,
        attempt: Int,
        status: String,
        lastError: String?,
        updatedAt: Long
    )

    @Query("DELETE FROM messages WHERE profile_id = :profileId")
    suspend fun deleteByProfile(profileId: String)

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun countAll(): Long

    @Query(
        """
    SELECT 
      m.destination AS destination,
      MAX(m.created_at) AS lastAt,
      (SELECT text FROM messages m2 
         WHERE m2.profile_id = m.profile_id AND m2.destination = m.destination
         ORDER BY m2.created_at DESC LIMIT 1) AS lastText,
      (SELECT status FROM messages m2 
         WHERE m2.profile_id = m.profile_id AND m2.destination = m.destination
         ORDER BY m2.created_at DESC LIMIT 1) AS lastStatus,
      COUNT(*) AS total
    FROM messages m
    WHERE m.profile_id = :profileId
    GROUP BY m.destination
    ORDER BY lastAt DESC
    """
    )
    fun observeConversations(profileId: String): Flow<List<ConversationRow>>

    @Query("""
        UPDATE messages
        SET status = :status,
            last_error = :lastError,
            updated_at = :updatedAt
        WHERE message_id = :messageId
    """)
    suspend fun updateStatusByMessageId(messageId: String, status: String, lastError: String?, updatedAt: Long)

    @Query("DELETE FROM messages WHERE message_id = :messageId")
    suspend fun deleteByMessageId(messageId: String)
}
