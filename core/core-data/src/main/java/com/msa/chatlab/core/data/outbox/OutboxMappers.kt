package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.entity.OutboxItemEntity

fun OutboxItem.toEntity(): OutboxItemEntity {
    return OutboxItemEntity(
        profileId = profileId,
        messageId = messageId,
        destination = destination,
        contentType = contentType,
        headersJson = headersJson,
        body = body,
        createdAt = createdAt,
        attempt = attempt,
        lastError = lastError,
        status = status,
        updatedAt = System.currentTimeMillis()
    )
}

fun OutboxItemEntity.toDomain(): OutboxItem {
    return OutboxItem(
        profileId = profileId,
        messageId = messageId,
        destination = destination,
        contentType = contentType,
        headersJson = headersJson,
        body = body,
        createdAt = createdAt,
        attempt = attempt,
        lastError = lastError,
        status = status
    )
}
