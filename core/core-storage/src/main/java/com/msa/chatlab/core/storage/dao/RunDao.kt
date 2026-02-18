package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.RunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(run: RunEntity)

    @Query("SELECT * FROM runs ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :runId LIMIT 1")
    suspend fun getById(runId: String): RunEntity?
}
