package com.msa.chatlab.core.data.repository

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.mapper.toEntity
import com.msa.chatlab.core.data.mapper.toUi
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.featurechat.model.ChatMessageUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMessageRepository(
    private val dao: MessageDao,
    private val activeProfileStore: ActiveProfileStore
) : MessageRepository {

    override fun observeMessages(profileId: String): Flow<List<ChatMessageUi>> {
        return dao.observeByProfile(profileId).map { entities -> entities.map { it.toUi() } }
    }

    override suspend fun upsert(message: ChatMessageUi) {
        val profile = activeProfileStore.getActiveNow() ?: return
        dao.insert(message.toEntity(profile.id.value))
    }

    override suspend fun upsertAll(messages: List<ChatMessageUi>) {
        val profile = activeProfileStore.getActiveNow() ?: return
        val entities = messages.map { it.toEntity(profile.id.value) }
        dao.insertAll(entities)
    }

    override suspend fun clear(profileId: String) {
        dao.deleteByProfile(profileId)
    }
}
