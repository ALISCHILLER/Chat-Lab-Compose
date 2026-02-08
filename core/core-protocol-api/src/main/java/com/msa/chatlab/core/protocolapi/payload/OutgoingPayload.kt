package com.msa.chatlab.core.protocolapi.payload

data class OutgoingPayload(
    val envelope: Envelope,
    val destination: String? = null   // مثلاً topic برای MQTT یا event برای socketio
)
