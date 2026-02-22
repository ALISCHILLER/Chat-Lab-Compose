package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.AckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ack: AckEntity)

    @Query("SELECT * FROM acks WHERE id = :id")
    fun getById(id: String): Flow<AckEntity?>

    @Query("DELETE FROM acks WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
