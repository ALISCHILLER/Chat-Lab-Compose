package com.msa.chatlab.core.data.repository

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.value.ProfileId
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeAll(): Flow<List<Profile>>
    suspend fun getAll(): List<Profile>
    suspend fun getById(id: ProfileId): Profile?
    suspend fun upsert(profile: Profile): Profile
    suspend fun deleteById(id: ProfileId)
    suspend fun search(query: String): List<Profile>
}
