package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OutboxItemEntity)

    @Query("SELECT * FROM outbox ORDER BY createdAt ASC LIMIT 1")
    suspend fun getOldestPending(): OutboxItemEntity?

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE outbox SET attempt = attempt + 1, lastAttemptAt = :timestamp, lastError = :error WHERE id = :id")
    suspend fun incrementAttempt(id: String, timestamp: Long, error: String)

    @Query("UPDATE outbox SET status = :status, lastError = :error WHERE id = :id")
    suspend fun updateStatus(id: String, status: OutboxStatus, error: String)

    @Query("SELECT * FROM outbox ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OutboxItemEntity>>
}
