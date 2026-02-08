package com.msa.chatlab.core.protocol.api.payload

data class OutgoingPayload(
    val envelope: Envelope,
    val destination: String? = null   // topic/event/channel
)
