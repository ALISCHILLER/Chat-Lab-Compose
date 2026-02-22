package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.DedupEntity

@Dao
interface DedupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dedup: DedupEntity)

    @Query("SELECT * FROM dedup WHERE id = :id")
    suspend fun getById(id: String): DedupEntity?

    @Query("DELETE FROM dedup WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
