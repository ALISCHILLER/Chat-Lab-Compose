package com.msa.chatlab.protocol.mqtt.config

import com.msa.chatlab.core.domain.model.MqttConfig
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

object MqttConfigAdapter {
    fun MqttConfig.toMqttConnectOptions(): MqttConnectOptions {
        val options = MqttConnectOptions()
        options.isCleanSession = cleanSession
        options.connectionTimeout = 10
        options.isAutomaticReconnect = false // Reconnect logic is handled by ConnectionManager
        if (!username.isNullOrBlank()) {
            options.userName = username
        }
        if (!password.isNullOrBlank()) {
            options.password = password.toCharArray()
        }
        return options
    }
}
