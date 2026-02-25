package com.msa.chatlab.feature.chat.domain.usecase

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.model.OutboxStatus
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.feature.chat.model.toChatMessageUi
import com.msa.chatlab.feature.chat.state.ChatUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class GetChatUiStateUseCase(
    private val activeProfileStore: ActiveProfileStore,
    private val messageRepository: MessageRepository,
    private val outboxQueue: com.msa.chatlab.core.data.outbox.OutboxQueue
) {
    operator fun invoke(errorFlow: Flow<String?>): Flow<ChatUiState> {
        return activeProfileStore.activeProfile
            .filterNotNull()
            .flatMapLatest { activeProfile ->
                combine(
                    messageRepository.observeMessages(activeProfile.id),
                    outboxQueue.observeCount(activeProfile.id.value, OutboxStatus.PENDING),
                    outboxQueue.observeCount(activeProfile.id.value, OutboxStatus.IN_FLIGHT),
                    outboxQueue.observeCount(activeProfile.id.value, OutboxStatus.FAILED),
                    errorFlow
                ) { messages, pendingCount, inflightCount, failedCount, error ->
                    ChatUiState(
                        profileName = activeProfile.name,
                        messages = messages.map { it.toChatMessageUi() },
                        outboxCount = pendingCount + inflightCount + failedCount,
                        error = error
                    )
                }
            }
    }
}
