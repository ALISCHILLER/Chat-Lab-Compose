package com.msa.chatlab.core.storage.dao

import androidx.room.*
import com.msa.chatlab.core.storage.entity.PresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY name ASC")
    fun observeAll(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PresetEntity)

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun countAll(): Long
}
