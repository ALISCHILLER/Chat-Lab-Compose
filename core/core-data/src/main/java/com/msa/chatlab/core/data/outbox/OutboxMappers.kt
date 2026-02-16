package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.storage.entity.OutboxItemEntity

fun OutboxItem.toEntity(): OutboxItemEntity {
    return OutboxItemEntity(
        id = id,
        messageId = messageId,
        destination = destination ?: "",
        contentType = contentType,
        headersJson = headersJson,
        body = body,
        createdAt = createdAt,
        attempt = attempt,
        lastError = lastError
    )
}

fun OutboxItemEntity.toDomain(): OutboxItem {
    return OutboxItem(
        id = id,
        messageId = messageId,
        destination = destination,
        contentType = contentType,
        headersJson = headersJson,
        body = body,
        createdAt = createdAt,
        attempt = attempt,
        lastError = lastError
    )
}
