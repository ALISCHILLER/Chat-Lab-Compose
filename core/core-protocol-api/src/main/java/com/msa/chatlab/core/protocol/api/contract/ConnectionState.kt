package com.msa.chatlab.core.protocol.api.contract

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Connecting : ConnectionState()
    data class Connected(val at: Long = System.currentTimeMillis()) : ConnectionState()
    data class Disconnected(val reason: String? = null, val at: Long = System.currentTimeMillis()) : ConnectionState()
}
