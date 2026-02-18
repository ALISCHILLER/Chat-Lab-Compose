package com.msa.chatlab.core.domain.repository

import com.msa.chatlab.core.domain.model.ChatMessage
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.ProfileId
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeMessages(profileId: ProfileId): Flow<List<ChatMessage>>
    suspend fun insertOutgoing(profileId: ProfileId, messageId: MessageId, text: String, destination: String?): ChatMessage
    suspend fun insertIncoming(profileId: ProfileId, messageId: MessageId, text: String, source: String?): ChatMessage
    suspend fun updateStatus(messageId: MessageId, status: MessageStatus, errorMessage: String? = null)
    suspend fun deleteMessage(id: MessageId)
    suspend fun clearAllFor(profileId: ProfileId)
}
