package com.msa.chatlab.protocol.mqtt.transport

import com.msa.chatlab.core.domain.model.MqttConfig
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.contract.TransportCapabilities
import com.msa.chatlab.core.protocol.api.contract.TransportContract
import com.msa.chatlab.core.protocol.api.event.TransportError
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.protocol.mqtt.config.toMqttConnectOptions
import com.msa.chatlab.protocol.mqtt.mapper.toIncomingPayload
import com.msa.chatlab.protocol.mqtt.mapper.toTransportError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttTransport(
    private val profile: Profile
) : TransportContract, MqttCallbackExtended {

    private val config = profile.transportConfig as MqttConfig
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<TransportEvent>(extraBufferCapacity = 64)
    override val events: Flow<TransportEvent> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState

    private val mqttClient: MqttAsyncClient = MqttAsyncClient(config.endpoint, config.clientId, MemoryPersistence())

    override val capabilities = TransportCapabilities(
        supportsQoS = true,
        supportsAck = true, // MQTT handles ACK internally via QoS
        supportsNativeReconnect = false,
        supportsBinary = true
    )

    init {
        mqttClient.setCallback(this)
    }

    override suspend fun connect() {
        if (mqttClient.isConnected) return
        _connectionState.value = ConnectionState.Connecting
        try {
            mqttClient.connect(config.toMqttConnectOptions()).waitForCompletion()
            mqttClient.subscribe(config.topic, config.qos)
        } catch (e: MqttException) {
            _connectionState.value = ConnectionState.Disconnected(e.reasonCode.toString())
            throw e.toTransportError()
        }
    }

    override suspend fun disconnect() {
        if (!mqttClient.isConnected) return
        try {
            mqttClient.disconnect().waitForCompletion()
        } catch (e: MqttException) {
            throw e.toTransportError()
        }
    }

    override suspend fun send(payload: OutgoingPayload) {
        val topic = payload.destination ?: config.topic
        val message = MqttMessage(payload.envelope.body)
        message.qos = config.qos
        try {
            mqttClient.publish(topic, message)
            _events.tryEmit(TransportEvent.MessageSent(payload.envelope.messageId.value))
        } catch (e: MqttException) {
            _events.tryEmit(TransportEvent.ErrorOccurred(e.toTransportError()))
            throw e.toTransportError()
        }
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        _connectionState.value = ConnectionState.Connected(System.currentTimeMillis())
        scope.launch { _events.emit(TransportEvent.Connected) }
    }

    override fun connectionLost(cause: Throwable?) {
        _connectionState.value = ConnectionState.Disconnected(cause?.message)
        scope.launch { _events.emit(TransportEvent.Disconnected(cause?.message)) }
        cause?.let {
            scope.launch { _events.emit(TransportEvent.ErrorOccurred(it.toTransportError())) }
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        message?.let {
            scope.launch { _events.emit(TransportEvent.MessageReceived(it.toIncomingPayload(topic))) }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // Not used for now, as we optimistically consider sent on publish
    }
}
