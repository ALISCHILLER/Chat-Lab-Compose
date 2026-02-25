package com.msa.chatlab.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProtocolType {
    WS_OKHTTP,
    WS_KTOR,
    MQTT,
    SOCKETIO,
    SIGNALR
}
