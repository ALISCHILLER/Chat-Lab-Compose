package com.msa.chatlab.core.protocolapi.contract

data class TransportCapabilities(
    val supportsQoS: Boolean,
    val supportsAck: Boolean,
    val supportsNativeReconnect: Boolean,
    val supportsBinary: Boolean
)
