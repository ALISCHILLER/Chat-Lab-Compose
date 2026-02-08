package com.msa.chatlab.core.protocol.api.contract

data class TransportCapabilities(
    val supportsQoS: Boolean,
    val supportsAck: Boolean,
    val supportsNativeReconnect: Boolean,
    val supportsBinary: Boolean
)
