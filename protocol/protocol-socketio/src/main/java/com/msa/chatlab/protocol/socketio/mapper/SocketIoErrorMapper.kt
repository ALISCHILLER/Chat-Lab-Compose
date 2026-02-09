package com.msa.chatlab.protocol.socketio.mapper

import com.msa.chatlab.core.protocol.api.event.TransportError
import io.socket.client.SocketIOException

fun Throwable.toTransportError(): TransportError {
    return when (this) {
        is SocketIOException -> TransportError(
            code = "SOCKET_IO_ERROR",
            message = message ?: "Socket.IO error",
            throwable = this
        )
        else -> TransportError(
            code = "UNKNOWN",
            message = message ?: "Unknown Socket.IO error",
            throwable = this
        )
    }
}
