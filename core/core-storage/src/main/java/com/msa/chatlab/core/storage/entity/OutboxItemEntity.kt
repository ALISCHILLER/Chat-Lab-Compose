package com.msa.chatlab.core.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.domain.value.MessageId

@Entity(
    tableName = "outbox",
    primaryKeys = ["profile_id", "message_id"],
    indices = [
        Index(value = ["profile_id", "created_at"])
    ]
)
data class OutboxItemEntity(
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "attempt") val attempt: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long
) {
    fun toDomain(): OutboxItem = OutboxItem(
        profileId = profileId,
        messageId = MessageId(messageId),
        text = text,
        attempt = attempt,
        createdAtMs = createdAt
    )

    companion object {
        fun fromDomain(profileId: String, item: OutboxItem): OutboxItemEntity {
            return OutboxItemEntity(
                profileId = profileId,
                messageId = item.messageId.value,
                text = item.text,
                attempt = item.attempt,
                createdAt = item.createdAtMs
            )
        }
    }
}
