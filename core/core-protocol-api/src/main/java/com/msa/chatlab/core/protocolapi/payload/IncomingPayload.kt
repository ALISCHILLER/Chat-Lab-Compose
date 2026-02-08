package com.msa.chatlab.core.protocolapi.payload

data class IncomingPayload(
    val envelope: Envelope,
    val source: String? = null        // مثلاً topic یا channel
)
