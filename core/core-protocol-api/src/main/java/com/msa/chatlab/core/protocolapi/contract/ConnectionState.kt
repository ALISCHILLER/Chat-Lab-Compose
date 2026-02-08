package com.msa.chatlab.core.protocolapi.contract

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Disconnected(val reason: String? = null) : ConnectionState()
}
