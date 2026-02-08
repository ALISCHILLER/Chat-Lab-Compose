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
    suspend fun upsert(entity: RunEntity)

    @Query("SELECT * FROM runs ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE profileId = :profileId ORDER BY startedAt DESC")
    fun observeByProfile(profileId: String): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :runId LIMIT 1")
    suspend fun getById(runId: String): RunEntity?

    @Query("DELETE FROM runs WHERE id = :runId")
    suspend fun deleteById(runId: String)

    @Query("DELETE FROM runs")
    suspend fun deleteAll()
}
