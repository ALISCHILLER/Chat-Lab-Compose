package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Query("SELECT * FROM outbox_items ORDER BY created_at ASC")
    fun observeAll(): Flow<List<OutboxItemEntity>>

    @Query("SELECT * FROM outbox_items ORDER BY created_at ASC LIMIT 1")
    suspend fun peekOldest(): OutboxItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OutboxItemEntity)

    @Query("DELETE FROM outbox_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM outbox_items")
    suspend fun count(): Int
}
