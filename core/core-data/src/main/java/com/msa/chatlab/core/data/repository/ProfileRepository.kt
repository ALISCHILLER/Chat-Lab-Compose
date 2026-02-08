package com.msa.chatlab.core.data.repository

import com.msa.chatlab.core.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeAll(): Flow<List<Profile>>
    suspend fun getAll(): List<Profile>
    suspend fun getById(id: String): Profile?
    suspend fun upsert(profile: Profile): Profile
    suspend fun deleteById(id: String)
    suspend fun search(query: String): List<Profile>
}
