package com.msa.chatlab.core.domain.rules

import com.msa.chatlab.core.domain.model.*

object ConfigValidator {

    fun validateTransport(protocolType: ProtocolType, config: TransportConfig): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (config.endpoint.isBlank()) {
            errors += ValidationError.Required("transport.endpoint", "Endpoint/URL نباید خالی باشد")
        }

        when (protocolType) {
            ProtocolType.WS_OKHTTP -> if (config !is WsOkHttpConfig)
                errors += ValidationError.Invalid("transportConfig", "برای WS_OKHTTP باید WsOkHttpConfig باشد")

            ProtocolType.WS_KTOR -> if (config !is WsKtorConfig)
                errors += ValidationError.Invalid("transportConfig", "برای WS_KTOR باید WsKtorConfig باشد")

            ProtocolType.MQTT -> {
                val c = config as? MqttConfig
                if (c == null) {
                    errors += ValidationError.Invalid("transportConfig", "برای MQTT باید MqttConfig باشد")
                } else {
                    if (c.clientId.isBlank()) errors += ValidationError.Required("mqtt.clientId", "clientId اجباری است")
                    if (c.topic.isBlank()) errors += ValidationError.Required("mqtt.topic", "topic اجباری است")
                    if (c.qos !in 0..2) errors += ValidationError.OutOfRange("mqtt.qos", "qos باید 0 یا 1 یا 2 باشد")
                }
            }

            ProtocolType.SOCKETIO -> {
                val c = config as? SocketIoConfig
                if (c == null) {
                    errors += ValidationError.Invalid("transportConfig", "برای Socket.IO باید SocketIoConfig باشد")
                } else {
                    if (c.events.isEmpty()) errors += ValidationError.Required("socketio.events", "حداقل یک event لازم است")
                }
            }

            ProtocolType.SIGNALR -> {
                val c = config as? SignalRConfig
                if (c == null) {
                    errors += ValidationError.Invalid("transportConfig", "برای SignalR باید SignalRConfig باشد")
                } else {
                    if (c.hubMethodName.isBlank()) {
                        errors += ValidationError.Required("signalr.hubMethodName", "نام متد hub اجباری است")
                    }
                }
            }
        }

        return if (errors.isEmpty()) ValidationResult.ok() else ValidationResult.fail(errors)
    }
}
