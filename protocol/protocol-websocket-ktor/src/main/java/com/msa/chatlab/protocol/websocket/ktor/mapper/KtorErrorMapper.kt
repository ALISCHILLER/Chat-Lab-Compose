package com.msa.chatlab.protocol.websocket.ktor.mapper

import com.msa.chatlab.core.protocol.api.event.TransportError
import io.ktor.client.plugins.websocket.*
import java.net.ConnectException

fun Throwable.toTransportError(): TransportError {
    return when (this) {
        is ConnectException -> TransportError(
            code = "CONNECTION_REFUSED",
            message = message ?: "Connection refused",
            throwable = this
        )
        is WebSocketException -> TransportError(
            code = "WEBSOCKET_ERROR",
            message = message ?: "WebSocket error",
            throwable = this
        )
        else -> TransportError(
            code = "UNKNOWN",
            message = message ?: "Unknown Ktor error",
            throwable = this
        )
    }
}
