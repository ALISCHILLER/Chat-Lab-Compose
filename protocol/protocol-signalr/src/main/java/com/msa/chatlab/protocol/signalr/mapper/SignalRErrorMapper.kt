package com.msa.chatlab.protocol.signalr.mapper

import com.msa.chatlab.core.protocol.api.event.TransportError

fun Exception.toTransportError(): TransportError {
    return TransportError(
        code = "SIGNALR_ERROR",
        message = message ?: "Unknown SignalR error",
        throwable = this
    )
}
