package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.msa.chatlab.featurechat.model.ChatMessageUi

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
    @ColumnInfo(name = "direction") val direction: String, // "OUT" / "IN"
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "queued") val queued: Boolean = false,
    @ColumnInfo(name = "attempt") val attempt: Int = 0
) {
    fun toDomain(): ChatMessageUi = ChatMessageUi(
        messageId = messageId,
        direction = when (direction) {
            "OUT" -> ChatMessageUi.Direction.OUT
            else -> ChatMessageUi.Direction.IN
        },
        text = text,
        timeMs = createdAt,
        queued = queued,
        attempt = attempt
    )

    companion object {
        fun fromDomain(profileId: String, message: ChatMessageUi): MessageEntity {
            return MessageEntity(
                profileId = profileId,
                messageId = message.messageId,
                direction = when (message.direction) {
                    ChatMessageUi.Direction.OUT -> "OUT"
                    ChatMessageUi.Direction.IN -> "IN"
                },
                text = message.text,
                createdAt = message.timeMs,
                queued = message.queued,
                attempt = message.attempt
            )
        }
    }
}
