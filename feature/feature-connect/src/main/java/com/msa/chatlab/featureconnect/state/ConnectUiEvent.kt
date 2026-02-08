package com.msa.chatlab.featureconnect.state

sealed interface ConnectUiEvent {
    data object Prepare : ConnectUiEvent
    data object Connect : ConnectUiEvent
    data object Disconnect : ConnectUiEvent
    data object ClearError : ConnectUiEvent
}
