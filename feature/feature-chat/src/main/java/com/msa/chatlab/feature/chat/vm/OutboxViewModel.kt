package com.msa.chatlab.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.domain.model.OutboxStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OutboxUiState(
    val pendingItems: List<com.msa.chatlab.core.data.outbox.OutboxItem> = emptyList(),
    val inFlightItems: List<com.msa.chatlab.core.data.outbox.OutboxItem> = emptyList(),
    val failedItems: List<com.msa.chatlab.core.data.outbox.OutboxItem> = emptyList()
)

class OutboxViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val outboxQueue: OutboxQueue
) : ViewModel() {

    val uiState: StateFlow<OutboxUiState> =
        activeProfileStore.activeProfile.flatMapLatest { profile ->
            if (profile == null) flowOf(OutboxUiState())
            else combine(
                outboxQueue.observeByStatus(profile.id.value, OutboxStatus.PENDING),
                outboxQueue.observeByStatus(profile.id.value, OutboxStatus.IN_FLIGHT),
                outboxQueue.observeByStatus(profile.id.value, OutboxStatus.FAILED)
            ) { pending, inflight, failed ->
                OutboxUiState(pendingItems = pending, inFlightItems = inflight, failedItems = failed)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OutboxUiState())

    fun onRetryAll() {
        viewModelScope.launch {
            val id = activeProfileStore.getActiveNow()?.id?.value ?: return@launch
            outboxQueue.retryAllFailed(id)
        }
    }

    fun onClearAll() {
        viewModelScope.launch {
            val id = activeProfileStore.getActiveNow()?.id?.value ?: return@launch
            outboxQueue.clearFailed(id)
        }
    }
}