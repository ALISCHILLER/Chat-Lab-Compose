package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.msa.chatlab.core.domain.model.ChatMessage
import com.msa.chatlab.core.domain.model.MessageDirection
import com.msa.chatlab.core.domain.value.MessageId

@Entity(
    tableName = "messages",
    primaryKeys = ["profile_id", "message_id"],
    indices = [
        Index(value = ["profile_id", "created_at"])
    ]
)
data class MessageEntity(
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "direction") val direction: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "queued") val queued: Boolean = false,
    @ColumnInfo(name = "attempt") val attempt: Int = 0
) {
    fun toDomain(): ChatMessage = ChatMessage(
        profileId = profileId,
        messageId = MessageId(messageId),
        direction = if (direction == "OUT") MessageDirection.OUT else MessageDirection.IN,
        text = text,
        createdAtMs = createdAt,
        queued = queued,
        attempt = attempt,
        status = com.msa.chatlab.core.domain.model.MessageStatus.Sent // Placeholder
    )

    companion object {
        fun fromDomain(profileId: String, message: ChatMessage): MessageEntity {
            return MessageEntity(
                profileId = profileId,
                messageId = message.messageId.value,
                direction = if (message.direction == MessageDirection.OUT) "OUT" else "IN",
                text = message.text,
                createdAt = message.createdAtMs,
                queued = message.queued,
                attempt = message.attempt
            )
        }
    }
}
