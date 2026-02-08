package com.msa.chatlab.core.protocolapi.event

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.protocolapi.error.TransportError
import com.msa.chatlab.core.protocolapi.payload.IncomingPayload

sealed class TransportEvent {

    data object Connected : TransportEvent()

    data class Disconnected(
        val reason: String? = null,
        val willRetry: Boolean = false
    ) : TransportEvent()

    data class MessageReceived(
        val payload: IncomingPayload
    ) : TransportEvent()

    data class MessageSent(
        val messageId: MessageId
    ) : TransportEvent()

    data class AckReceived(
        val messageId: MessageId
    ) : TransportEvent()

    data class ErrorOccurred(
        val error: TransportError
    ) : TransportEvent()

    /** فقط برای SignalR و جاهایی که underlying مهم است */
    data class UnderlyingTransportSelected(
        val transport: UnderlyingTransport
    ) : TransportEvent()

    data class FallbackTriggered(
        val from: UnderlyingTransport,
        val to: UnderlyingTransport,
        val reason: String
    ) : TransportEvent()
}
