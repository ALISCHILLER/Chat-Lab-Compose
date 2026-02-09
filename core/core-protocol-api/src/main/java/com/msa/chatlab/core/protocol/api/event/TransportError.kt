package com.msa.chatlab.core.protocol.api.event

data class TransportError(
    val code: String,
    val message: String,
    val throwable: Throwable? = null
)
