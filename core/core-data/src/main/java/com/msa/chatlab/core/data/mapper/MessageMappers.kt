package com.msa.chatlab.core.data.mapper

import com.msa.chatlab.core.storage.entity.MessageEntity
import com.msa.chatlab.featurechat.model.ChatMessageUi

fun MessageEntity.toUi(): ChatMessageUi =
    ChatMessageUi(
        id = messageId,
        direction = if (direction == "OUT") ChatMessageUi.Direction.OUT else ChatMessageUi.Direction.IN,
        text = text,
        timeMs = createdAt,
        queued = queued,
        attempt = attempt
    )

fun ChatMessageUi.toEntity(profileId: String): MessageEntity =
    MessageEntity(
        profileId = profileId,
        messageId = id,
        direction = if (direction == ChatMessageUi.Direction.OUT) "OUT" else "IN",
        text = text,
        createdAt = timeMs,
        queued = queued,
        attempt = attempt
    )
