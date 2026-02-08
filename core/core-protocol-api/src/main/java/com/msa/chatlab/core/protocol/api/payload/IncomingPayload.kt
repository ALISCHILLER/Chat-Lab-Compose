package com.msa.chatlab.core.protocol.api.payload

data class IncomingPayload(
    val envelope: Envelope,
    val source: String? = null
)
