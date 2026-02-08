package com.msa.chatlab.core.protocol.api.error

sealed class TransportError(open val message: String, open val cause: Throwable? = null) {

    data class Network(override val message: String, override val cause: Throwable? = null) :
        TransportError(message, cause)

    data class Timeout(override val message: String, override val cause: Throwable? = null) :
        TransportError(message, cause)

    data class Auth(override val message: String, override val cause: Throwable? = null) :
        TransportError(message, cause)

    data class Protocol(override val message: String, override val cause: Throwable? = null) :
        TransportError(message, cause)

    data class Unknown(override val message: String, override val cause: Throwable? = null) :
        TransportError(message, cause)
}
