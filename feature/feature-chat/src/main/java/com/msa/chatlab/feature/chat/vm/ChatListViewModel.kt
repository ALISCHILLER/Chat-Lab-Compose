package com.msa.chatlab.feature.chat.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.model.ConversationRow
import com.msa.chatlab.core.domain.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class ChatListUiState(
    val profileName: String = "No profile",
    val query: String = "",
    val items: List<ConversationRow> = emptyList()
)

class ChatListViewModel(
    private val activeProfileStore: ActiveProfileStore,
    private val repo: MessageRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val state: StateFlow<ChatListUiState> =
        activeProfileStore.activeProfile.flatMapLatest { profile ->
            if (profile == null) flowOf(ChatListUiState(profileName = "No profile"))
            else combine(
                repo.observeConversations(profile.id),
                query
            ) { items, q ->
                val filtered = if (q.isBlank()) items else items.filter {
                    it.destination.contains(q, true) || (it.lastText ?: "").contains(q, true)
                }
                ChatListUiState(profileName = profile.name, query = q, items = filtered)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatListUiState())

    fun onQuery(v: String) { query.value = v }
}
