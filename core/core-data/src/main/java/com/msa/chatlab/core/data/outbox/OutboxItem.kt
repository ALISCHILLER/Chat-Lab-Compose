package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.entity.OutboxStatus

data class OutboxItem(
    val profileId: String,
    val messageId: String,
    val destination: String,
    val contentType: String,
    val headersJson: String,
    val body: ByteArray,
    val createdAt: Long,
    val attempt: Int = 0,
    val lastError: String? = null,
    val status: OutboxStatus = OutboxStatus.PENDING
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OutboxItem) return false
        return profileId == other.profileId &&
            messageId == other.messageId &&
            destination == other.destination &&
            contentType == other.contentType &&
            headersJson == other.headersJson &&
            body.contentEquals(other.body) &&
            createdAt == other.createdAt &&
            attempt == other.attempt &&
            lastError == other.lastError &&
            status == other.status
    }

    override fun hashCode(): Int {
        var result = profileId.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + headersJson.hashCode()
        result = 31 * result + body.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + attempt
        result = 31 * result + (lastError?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        return result
    }
}
