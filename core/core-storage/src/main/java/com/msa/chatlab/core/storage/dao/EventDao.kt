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
    suspend fun insertAll(items: List<EventEntity>)

    @Query("SELECT * FROM events WHERE runId = :runId ORDER BY timestamp ASC")
    fun observeByRun(runId: String): Flow<List<EventEntity>>
}
