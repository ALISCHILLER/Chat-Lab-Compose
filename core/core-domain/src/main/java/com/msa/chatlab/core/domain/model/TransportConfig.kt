package com.msa.chatlab.core.domain.model

sealed interface TransportConfig {
    val endpoint: String
    val headers: Map<String, String>
}

/** WebSocket - OkHttp */
data class WsOkHttpConfig(
    override val endpoint: String,
    val pingIntervalMs: Long = 15_000,
    override val headers: Map<String, String> = emptyMap()
) : TransportConfig

/** WebSocket - Ktor */
data class WsKtorConfig(
    override val endpoint: String,
    val pingIntervalMs: Long = 15_000,
    val connectTimeoutMs: Long = 10_000,
    override val headers: Map<String, String> = emptyMap()
) : TransportConfig

/** MQTT */
data class MqttConfig(
    override val endpoint: String,          // tcp://host:1883
    val clientId: String,
    val topic: String,
    val qos: Int = 1,                       // 0..2
    val cleanSession: Boolean = true,
    val username: String? = null,
    val password: String? = null,
    override val headers: Map<String, String> = emptyMap()
) : TransportConfig

/** Socket.IO */
data class SocketIoConfig(
    override val endpoint: String,          // base url
    val namespace: String? = null,
    val connectPath: String? = null,        // /socket.io
    val events: List<String> = listOf("message"),
    override val headers: Map<String, String> = emptyMap()
) : TransportConfig

/** SignalR */
data class SignalRConfig(
    override val endpoint: String,          // hub url
    val hubMethodName: String = "Send",
    val transportPreference: SignalRTransportPreference = SignalRTransportPreference.Auto,
    override val headers: Map<String, String> = emptyMap()
) : TransportConfig

enum class SignalRTransportPreference {
    Auto, WebSocketOnly, SseOnly, LongPollingOnly
}
