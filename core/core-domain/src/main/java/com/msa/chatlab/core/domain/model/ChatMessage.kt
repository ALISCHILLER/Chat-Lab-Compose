package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis

data class ChatMessage(
    val id: MessageId,
    val localCreatedAt: TimestampMillis,
    val text: String,
    val status: MessageStatus = MessageStatus.Draft,
    val metadata: Map<String, String> = emptyMap()
)
