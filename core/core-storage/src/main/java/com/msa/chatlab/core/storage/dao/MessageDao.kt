package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

data class ConversationRow(
    val destination: String,
    val lastAt: Long,
    val lastText: String?,
    val lastStatus: String?,
    val total: Int
)

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MessageEntity)

    @Query("SELECT * FROM messages WHERE profile_id = :profileId ORDER BY created_at ASC")
    fun observeByProfile(profileId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE profile_id = :profileId AND destination = :destination ORDER BY created_at ASC")
    fun observeConversation(profileId: String, destination: String): Flow<List<MessageEntity>>

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
}
