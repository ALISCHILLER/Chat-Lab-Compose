package com.msa.chatlab.protocol.websocket.okhttp.mapper

import com.msa.chatlab.core.protocol.api.error.TransportError
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object OkHttpErrorMapper {

    fun map(t: Throwable): TransportError {
        return when (t) {
            is UnknownHostException -> TransportError("NETWORK", "Unknown host", t)
            is SocketTimeoutException -> TransportError("TIMEOUT", "Timeout", t)
            else -> TransportError("UNKNOWN", t.message ?: "Unknown error", t)
        }
    }
}
