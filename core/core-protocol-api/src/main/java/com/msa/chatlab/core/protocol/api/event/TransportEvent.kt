package com.msa.chatlab.core.protocol.api.event

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload

sealed interface TransportEvent {
    data object Connected : TransportEvent
    data class Disconnected(val reason: String? = null) : TransportEvent

    data class MessageReceived(val payload: IncomingPayload) : TransportEvent
    data class MessageSent(val messageId: String) : TransportEvent

    data class ErrorOccurred(val error: TransportError) : TransportEvent
}
