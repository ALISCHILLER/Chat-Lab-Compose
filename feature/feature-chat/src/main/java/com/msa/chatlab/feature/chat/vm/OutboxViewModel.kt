package com.msa.chatlab.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OutboxUiState(
    val pendingItems: List<OutboxItem> = emptyList(),
    val failedItems: List<OutboxItem> = emptyList()
)

class OutboxViewModel(private val outboxQueue: OutboxQueue) : ViewModel() {

    val uiState: StateFlow<OutboxUiState> = combine(
        outboxQueue.observeByStatus(OutboxStatus.PENDING),
        outboxQueue.observeByStatus(OutboxStatus.FAILED)
    ) { pending, failed ->
        OutboxUiState(pendingItems = pending, failedItems = failed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OutboxUiState())

    fun onRetryAll() {
        viewModelScope.launch {
            outboxQueue.retryAllFailed()
        }
    }

    fun onClearAll() {
        viewModelScope.launch {
            outboxQueue.clearFailed()
        }
    }
}
