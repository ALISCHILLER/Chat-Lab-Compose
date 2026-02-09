package com.msa.chatlab.protocol.mqtt.mapper

import com.msa.chatlab.core.protocol.api.event.TransportError
import org.eclipse.paho.client.mqttv3.MqttException

fun MqttException.toTransportError(): TransportError {
    return TransportError(
        code = reasonCode.toString(),
        message = message ?: "Unknown MQTT error",
        throwable = this
    )
}

fun Throwable.toTransportError(): TransportError {
    return if (this is MqttException) {
        this.toTransportError()
    } else {
        TransportError(
            code = "UNKNOWN",
            message = message ?: "Unknown error",
            throwable = this
        )
    }
}
