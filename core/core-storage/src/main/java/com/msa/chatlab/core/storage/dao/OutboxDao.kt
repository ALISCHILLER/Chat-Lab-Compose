package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.msa.chatlab.core.domain.model.OutboxStatus
import com.msa.chatlab.core.storage.entities.OutboxItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OutboxItemEntity)

    @Update
    suspend fun update(item: OutboxItemEntity)

    @Query("SELECT * FROM outbox")
    fun getAll(): Flow<List<OutboxItemEntity>>

    @Query("SELECT * FROM outbox WHERE id = :id")
    suspend fun getById(id: String): OutboxItemEntity?

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM outbox WHERE status = :status")
    suspend fun getByStatus(status: OutboxStatus): List<OutboxItemEntity>

    @Query("SELECT * FROM outbox WHERE session_id = :sessionId AND status != :status ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestBySessionId(sessionId: String, status: OutboxStatus): OutboxItemEntity?
}
