package com.msa.chatlab.featureconnect.state

sealed interface ConnectUiEffect {
    data class Toast(val message: String) : ConnectUiEffect
}
