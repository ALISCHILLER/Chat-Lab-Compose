package com.msa.chatlab.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.msa.chatlab.core.storage.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles")
    suspend fun getAll(): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM profiles WHERE name LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' OR tagsCsv LIKE '%' || :q || '%'")
    suspend fun search(q: String): List<ProfileEntity>
}
