package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<EventEntity>)

    @Query("SELECT * FROM events WHERE runId = :runId ORDER BY timestamp ASC")
    fun observeRunEvents(runId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE runId = :runId ORDER BY timestamp ASC")
    suspend fun getRunEvents(runId: String): List<EventEntity>

    @Query("DELETE FROM events WHERE runId = :runId")
    suspend fun deleteByRunId(runId: String)

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
