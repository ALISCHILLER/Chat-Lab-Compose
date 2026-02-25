package com.msa.chatlab.protocol.mqtt

import com.msa.chatlab.core.domain.model.MqttConfig
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.error.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.core.protocol.api.mapper.decodeToTransportEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MqttTransport(
    private val profile: Profile,
    private val now: () -> Long = { System.currentTimeMillis() }
) : TransportContract {

    override val capabilities: TransportCapabilities = TransportCapabilities(
        supportsQoS = true,
        supportsAck = true,
        supportsNativeReconnect = false,
        supportsBinary = true
    )

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 256)
    override val events: Flow<TransportEvent> = _events.asSharedFlow()

    private val _stats = MutableStateFlow(TransportStatsEvent())
    override val stats: Flow<TransportStatsEvent> = _stats.asStateFlow()

    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var client: MqttAsyncClient? = null

    private fun requireConfig(): MqttConfig {
        val cfg = profile.transportConfig
        require(cfg is MqttConfig) { "Profile transportConfig is not MqttConfig" }
        return cfg
    }

    override suspend fun connect() {
        if (!started.compareAndSet(false, true)) return

        val cfg = requireConfig()
        _connectionState.value = ConnectionState.Connecting

        try {
            val mqttClient = MqttAsyncClient(cfg.endpoint, cfg.clientId, MemoryPersistence())
            client = mqttClient

            mqttClient.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    _connectionState.value = ConnectionState.Connected(at = now())
                    _events.tryEmit(TransportEvent.Connected)
                    if (reconnect) {
                        _stats.value = _stats.value.copy(reconnectCount = _stats.value.reconnectCount + 1)
                        scope.launch { safeSubscribe(cfg) }
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    _connectionState.value = ConnectionState.Disconnected(cause?.message, at = now())
                    _events.tryEmit(TransportEvent.Disconnected(cause?.message))
                    if (cause != null) {
                        _events.tryEmit(TransportEvent.ErrorOccurred(TransportError.ConnectionFailed(cause.message, cause)))
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val bytes = message?.payload ?: return
                    _stats.value = _stats.value.copy(bytesReceived = _stats.value.bytesReceived + bytes.size)

                    val text = runCatching { bytes.toString(Charsets.UTF_8) }.getOrNull()
                    if (text != null) {
                        _events.tryEmit(text.decodeToTransportEvent())
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
            })

            val opts = MqttConnectOptions().apply {
                isCleanSession = cfg.cleanSession
                isAutomaticReconnect = false
                connectionTimeout = 10
                keepAliveInterval = 20
                userName = cfg.username
                password = cfg.password?.toCharArray()
            }

            suspendCoroutine<Unit> { cont ->
                mqttClient.connect(opts, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        scope.launch {
                            runCatching { safeSubscribe(cfg) }
                                .onSuccess { cont.resume(Unit) }
                                .onFailure { cont.resumeWithException(it) }
                        }
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        cont.resumeWithException(exception ?: RuntimeException("MQTT connect failed"))
                    }
                })
            }
        } catch (e: Exception) {
            started.set(false)
            _connectionState.value = ConnectionState.Disconnected(e.message, at = now())
            _events.tryEmit(TransportEvent.ErrorOccurred(TransportError.ConnectionFailed(e.message, e)))
            throw e
        }
    }

    private suspend fun safeSubscribe(cfg: MqttConfig) {
        val mqttClient = client ?: return
        suspendCoroutine<Unit> { cont ->
            mqttClient.subscribe(cfg.topic, cfg.qos.coerceIn(0, 2), null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) = cont.resume(Unit)
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) =
                    cont.resumeWithException(exception ?: RuntimeException("MQTT subscribe failed"))
            })
        }
    }

    override suspend fun disconnect() {
        started.set(false)
        val mqttClient = client
        client = null

        runCatching {
            if (mqttClient != null && mqttClient.isConnected) {
                suspendCoroutine<Unit> { cont ->
                    mqttClient.disconnect(null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) = cont.resume(Unit)
                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) =
                            cont.resumeWithException(exception ?: RuntimeException("MQTT disconnect failed"))
                    })
                }
            }
            mqttClient?.close()
        }

        _connectionState.value = ConnectionState.Disconnected("client disconnect", at = now())
        _events.tryEmit(TransportEvent.Disconnected("client disconnect"))
    }

    override suspend fun send(payload: OutgoingPayload) {
        val cfg = requireConfig()
        val mqttClient = client ?: throw TransportError.NotConnected("MQTT client is null")
        if (!mqttClient.isConnected) throw TransportError.NotConnected("MQTT not connected")

        val topic = payload.destination?.takeIf { it.isNotBlank() } ?: cfg.topic
        val msg = MqttMessage(payload.envelope.body).apply {
            qos = cfg.qos.coerceIn(0, 2)
            isRetained = false
        }

        try {
            suspendCoroutine<Unit> { cont ->
                mqttClient.publish(topic, msg, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) = cont.resume(Unit)
                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) =
                        cont.resumeWithException(exception ?: RuntimeException("MQTT publish failed"))
                })
            }

            _stats.value = _stats.value.copy(bytesSent = _stats.value.bytesSent + payload.envelope.body.size)
            _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
        } catch (e: Exception) {
            val err = TransportError.SendFailed(e.message, e)
            _events.tryEmit(TransportEvent.ErrorOccurred(err))
            throw err
        }
    }
}
