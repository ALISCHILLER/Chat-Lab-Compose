package com.msa.chatlab.core.data.repository

import com.msa.chatlab.core.domain.model.ChatMessage
import com.msa.chatlab.core.domain.model.MessageDirection
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.storage.dao.ConversationRow
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMessageRepository(
    private val dao: MessageDao
) : MessageRepository {
    override fun observeMessages(profileId: ProfileId): Flow<List<ChatMessage>> {
        return dao.observeByProfile(profileId.value).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertOutgoing(
        profileId: ProfileId,
        messageId: MessageId,
        text: String,
        destination: String?
    ): ChatMessage {
        val message = ChatMessage(
            id = messageId,
            profileId = profileId,
            direction = MessageDirection.OUT,
            localCreatedAt = TimestampMillis(System.currentTimeMillis()),
            text = text,
            destination = destination,
            status = MessageStatus.Sending // Or Queued if offline
        )
        dao.upsert(message.toEntity())
        return message
    }

    override suspend fun insertIncoming(
        profileId: ProfileId,
        messageId: MessageId,
        text: String,
        source: String?
    ): ChatMessage {
        val message = ChatMessage(
            id = messageId,
            profileId = profileId,
            direction = MessageDirection.IN,
            localCreatedAt = TimestampMillis(System.currentTimeMillis()),
            text = text,
            destination = source, // Destination for IN is the source
            status = MessageStatus.Delivered
        )
        dao.upsert(message.toEntity())
        return message
    }

    override suspend fun updateStatus(messageId: MessageId, status: MessageStatus, errorMessage: String?) {
        // This is a simplified version. A more robust implementation would fetch the existing message first.
        // dao.updateStatus(messageId.value, status.name, errorMessage)
    }

    override suspend fun deleteMessage(id: MessageId) {
        // Not implemented for now
    }

    override suspend fun clearAllFor(profileId: ProfileId) {
        dao.deleteByProfile(profileId.value)
    }

    override fun observeConversations(profileId: ProfileId): Flow<List<ConversationRow>> {
        return dao.observeConversations(profileId.value)
    }

    override fun observeConversation(profileId: ProfileId, destination: String): Flow<List<MessageEntity>> {
        return dao.observeConversation(profileId.value, destination)
    }
}

private fun MessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = MessageId(messageId),
        profileId = ProfileId(profileId),
        direction = MessageDirection.valueOf(direction),
        localCreatedAt = TimestampMillis(createdAt),
        text = text,
        destination = destination,
        status = MessageStatus.valueOf(status),
        errorMessage = lastError
    )
}

private fun ChatMessage.toEntity(): MessageEntity {
    return MessageEntity(
        profileId = profileId.value,
        messageId = id.value,
        direction = direction.name,
        text = text,
        destination = destination,
        createdAt = localCreatedAt.value,
        status = status.name,
        lastError = errorMessage,
        attempt = 0, // Should be handled more robustly
        queued = false // Should be handled more robustly
    )
}
