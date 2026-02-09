package com.msa.chatlab.core.storage.dao

import androidx.room.*
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE profile_id = :profileId ORDER BY created_at ASC")
    fun observeByProfile(profileId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE profile_id = :profileId")
    suspend fun deleteByProfile(profileId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}
