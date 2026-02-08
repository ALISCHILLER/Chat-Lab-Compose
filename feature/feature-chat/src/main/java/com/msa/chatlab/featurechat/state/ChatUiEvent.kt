package com.msa.chatlab.featurechat.state

sealed interface ChatUiEvent {
    data class InputChanged(val value: String) : ChatUiEvent
    data object Send : ChatUiEvent
    data object Clear : ChatUiEvent
}
