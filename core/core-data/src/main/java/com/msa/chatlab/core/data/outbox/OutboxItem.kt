package com.msa.chatlab.core.data.outbox

import java.util.Arrays

data class OutboxItem(
    val id: String,
    val messageId: String,
    val destination: String?,
    val contentType: String,
    val headersJson: String,
    val body: ByteArray,
    val attempt: Int = 0,
    val createdAt: Long,
    val lastError: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OutboxItem

        if (id != other.id) return false
        if (messageId != other.messageId) return false
        if (destination != other.destination) return false
        if (contentType != other.contentType) return false
        if (headersJson != other.headersJson) return false
        if (!body.contentEquals(other.body)) return false
        if (attempt != other.attempt) return false
        if (createdAt != other.createdAt) return false
        if (lastError != other.lastError) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + (destination?.hashCode() ?: 0)
        result = 31 * result + contentType.hashCode()
        result = 31 * result + headersJson.hashCode()
        result = 31 * result + body.contentHashCode()
        result = 31 * result + attempt
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (lastError?.hashCode() ?: 0)
        return result
    }
}
