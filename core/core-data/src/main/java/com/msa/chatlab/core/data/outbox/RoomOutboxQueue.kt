package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class RoomOutboxQueue(
    private val dao: OutboxDao,
    private val activeProfileStore: ActiveProfileStore
) : OutboxQueue {

    override fun observe(profileId: String): Flow<List<OutboxItem>> =
        dao.observeByProfile(profileId).map { it.map { e -> e.toDomain() } }

    override suspend fun peekFirst(profileId: String): OutboxItem? =
        dao.peekFirst(profileId)?.toDomain()

    override suspend fun enqueue(item: OutboxItem) {
        dao.enqueue(item.toEntity())
    }

    override suspend fun incrementAttempt(profileId: String, messageId: MessageId) {
        dao.incrementAttempt(profileId, messageId.value)
    }

    override suspend fun remove(profileId: String, messageId: MessageId) {
        dao.remove(profileId, messageId.value)
    }

    override suspend fun clear(profileId: String) {
        dao.clearByProfile(profileId)
    }

    override fun contains(messageId: MessageId): Boolean {
        // برای سادگی، در این فاز از observe() برای بررسی وجود استفاده می‌کنیم
        // در پروژه واقعی بهتر است یک کوئری مستقیم اضافه شود
        val profile = activeProfileStore.getActiveNow() ?: return false
        return runBlocking {
            dao.observeByProfile(profile.id.value).firstOrNull()?.any { it.messageId == messageId.value } ?: false
        }
    }

    private fun OutboxItem.toEntity(): OutboxItemEntity {
        val profileId = activeProfileStore.getActiveNow()?.id?.value ?: ""
        return OutboxItemEntity(
            profileId = profileId,
            messageId = messageId.value,
            text = text,
            attempt = attempt,
            createdAt = createdAtMs
        )
    }
}
