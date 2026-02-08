package com.msa.chatlab.core.protocolapi.event

data class TransportStatsEvent(
    val bytesSent: Long = 0,
    val bytesReceived: Long = 0,
    val reconnectCount: Long = 0,
    val lastRttMs: Long? = null
)
