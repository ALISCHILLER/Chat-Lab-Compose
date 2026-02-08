package com.msa.chatlab.core.data.repository

import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.storage.dao.ProfileDao
import com.msa.chatlab.core.storage.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomProfileRepository(
    private val dao: ProfileDao,
    private val codec: ProfileJsonCodec,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ProfileRepository {

    override fun observeAll(): Flow<List<Profile>> {
        return dao.observeAll().map { list ->
            list.mapNotNull { entity -> decodeEntityOrNull(entity) }
        }
    }

    override suspend fun getAll(): List<Profile> {
        return dao.getAll().mapNotNull { decodeEntityOrNull(it) }
    }

    override suspend fun getById(id: String): Profile? {
        val entity = dao.getById(id) ?: return null
        return decodeEntityOrNull(entity)
    }

    override suspend fun upsert(profile: Profile): Profile {
        val now = clock()
        val json = codec.encode(profile)

        val entity = ProfileEntity(
            id = profile.id.value,
            name = profile.name,
            description = profile.description,
            tagsCsv = profile.tags.joinToString(","),
            protocolType = profile.protocolType.name,
            profileJson = json,
            createdAt = now,     // فاز ۱: ساده
            updatedAt = now
        )
        dao.upsert(entity)
        return profile
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    override suspend fun search(query: String): List<Profile> {
        return dao.search(query).mapNotNull { decodeEntityOrNull(it) }
    }

    private fun decodeEntityOrNull(entity: ProfileEntity): Profile? {
        return runCatching { codec.decode(entity.profileJson) }.getOrNull()
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}
