package com.msa.chatlab.core.protocol.api.error

sealed class TransportError(override val message: String, override val cause: Throwable? = null) : Throwable(message, cause) {
    class ConnectionFailed(message: String? = null, cause: Throwable? = null) :
        TransportError(message ?: "Connection failed", cause)

    class NotConnected(message: String? = null, cause: Throwable? = null) :
        TransportError(message ?: "Not connected", cause)

    class SendFailed(message: String? = null, cause: Throwable? = null) :
        TransportError(message ?: "Send failed", cause)

    class Other(message: String, cause: Throwable? = null) :
        TransportError(message, cause)
}
