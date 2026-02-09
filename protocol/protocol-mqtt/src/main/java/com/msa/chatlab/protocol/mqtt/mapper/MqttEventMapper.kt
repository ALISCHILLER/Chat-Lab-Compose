package com.msa.chatlab.protocol.mqtt.mapper

import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.IncomingPayload
import org.eclipse.paho.client.mqttv3.MqttMessage

fun MqttMessage.toIncomingPayload(topic: String?): IncomingPayload {
    val envelope = Envelope(
        messageId = MessageId("mqtt-${System.nanoTime()}"),
        createdAt = TimestampMillis(System.currentTimeMillis()),
        contentType = "application/octet-stream",
        headers = emptyMap(),
        body = payload
    )
    return IncomingPayload(
        envelope = envelope,
        source = topic
    )
}
