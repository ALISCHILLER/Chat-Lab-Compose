package com.msa.chatlab.feature.connect.state

sealed interface ConnectUiEffect {
    data class Toast(val message: String) : ConnectUiEffect
}
