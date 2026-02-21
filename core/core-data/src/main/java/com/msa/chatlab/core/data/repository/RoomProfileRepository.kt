package com.msa.chatlab.core.data.repository

import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.storage.dao.ProfileDao
import com.msa.chatlab.core.storage.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomProfileRepository(
    private val dao: ProfileDao,
    private val codec: ProfileJsonCodec
) : ProfileRepository {

    override fun observeAll(): Flow<List<Profile>> {
        return dao.observeAll().map { list -> list.map { codec.decode(it.profileJson) } }
    }

    override suspend fun getAll(): List<Profile> {
        return dao.getAll().map { codec.decode(it.profileJson) }
    }

    override suspend fun getById(id: String): Profile? {
        return dao.getById(id)?.let { codec.decode(it.profileJson) }
    }

    override suspend fun upsert(profile: Profile): Profile {
        val now = System.currentTimeMillis()
        val existing = dao.getById(profile.id.value)
        val createdAt = existing?.createdAt ?: now

        val json = codec.encode(profile)
        val entity = ProfileEntity(
            id = profile.id.value,
            name = profile.name,
            description = profile.description,
            tagsCsv = profile.tags.joinToString(","),
            protocolType = profile.protocolType.name,
            profileJson = json,
            createdAt = createdAt,
            updatedAt = now
        )

        dao.upsert(entity)
        return profile
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    override suspend fun search(query: String): List<Profile> {
        return dao.search(query).map { codec.decode(it.profileJson) }
    }
}
