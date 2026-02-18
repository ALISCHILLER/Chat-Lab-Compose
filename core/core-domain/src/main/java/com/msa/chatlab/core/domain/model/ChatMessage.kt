package com.msa.chatlab.core.domain.model

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.core.domain.value.TimestampMillis

data class ChatMessage(
    val id: MessageId,
    val profileId: ProfileId,
    val direction: MessageDirection,
    val localCreatedAt: TimestampMillis,
    val text: String,
    val destination: String? = null,
    val status: MessageStatus = MessageStatus.Draft,
    val errorMessage: String? = null,
    val metadata: Map<String, String> = emptyMap()
)
