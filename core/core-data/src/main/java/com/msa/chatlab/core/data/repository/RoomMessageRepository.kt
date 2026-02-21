package com.msa.chatlab.core.data.repository

import com.msa.chatlab.core.domain.model.ChatMessage
import com.msa.chatlab.core.domain.model.ConversationRow
import com.msa.chatlab.core.domain.model.MessageDirection
import com.msa.chatlab.core.domain.model.MessageEntity
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.storage.dao.MessageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMessageRepository(
    private val dao: MessageDao
) : MessageRepository {

    override fun observeMessages(profileId: ProfileId): Flow<List<ChatMessage>> {
        return dao.observeByProfile(profileId.value).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertOutgoing(
        profileId: ProfileId,
        messageId: MessageId,
        text: String,
        destination: String?
    ): ChatMessage {
        val msg = ChatMessage(
            id = messageId,
            profileId = profileId,
            direction = MessageDirection.OUT,
            localCreatedAt = TimestampMillis(System.currentTimeMillis()),
            text = text,
            destination = destination,
            status = MessageStatus.Sending
        )
        dao.upsert(msg.toEntity())
        return msg
    }

    override suspend fun insertIncoming(
        profileId: ProfileId,
        messageId: MessageId,
        text: String,
        source: String?
    ): ChatMessage {
        val msg = ChatMessage(
            id = messageId,
            profileId = profileId,
            direction = MessageDirection.IN,
            localCreatedAt = TimestampMillis(System.currentTimeMillis()),
            text = text,
            destination = source,
            status = MessageStatus.Delivered
        )
        dao.upsert(msg.toEntity())
        return msg
    }

    override suspend fun updateStatus(messageId: MessageId, status: MessageStatus, errorMessage: String?) {
        dao.updateStatusByMessageId(
            messageId = messageId.value,
            status = status.name,
            lastError = errorMessage,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteMessage(id: MessageId) {
        dao.deleteByMessageId(id.value)
    }

    override suspend fun clearAllFor(profileId: ProfileId) {
        dao.deleteByProfile(profileId.value)
    }

    override fun observeConversations(profileId: ProfileId): Flow<List<ConversationRow>> {
        return dao.observeConversations(profileId.value)
    }

    override fun observeConversation(profileId: ProfileId, destination: String): Flow<List<ChatMessage>> {
        return dao.observeConversation(profileId.value, destination).map { list -> list.map { it.toDomain() } }
    }
}

private fun MessageEntity.toDomain(): ChatMessage {
    val dir = runCatching { MessageDirection.valueOf(direction) }.getOrDefault(MessageDirection.OUT)

    val st = when (status.uppercase()) {
        "DRAFT" -> MessageStatus.Draft
        "SENDING", "QUEUED" -> MessageStatus.Sending
        "SENT" -> MessageStatus.Sent
        "DELIVERED", "RECEIVED" -> MessageStatus.Delivered
        "FAILED" -> MessageStatus.Failed
        else -> runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.Draft)
    }

    return ChatMessage(
        id = MessageId(messageId),
        profileId = ProfileId(profileId),
        direction = dir,
        localCreatedAt = TimestampMillis(createdAt),
        text = text,
        destination = destination,
        status = st,
        errorMessage = lastError
    )
}

private fun ChatMessage.toEntity(): MessageEntity {
    return MessageEntity(
        profileId = profileId.value,
        messageId = id.value,
        direction = direction.name,
        destination = destination ?: "default",
        text = text,
        createdAt = localCreatedAt.value,
        status = status.name,
        lastError = errorMessage,
        attempt = 0,
        queued = false
    )
}
