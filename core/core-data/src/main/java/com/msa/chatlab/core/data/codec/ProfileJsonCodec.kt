package com.msa.chatlab.core.data.codec

import com.msa.chatlab.core.domain.model.*
import com.msa.chatlab.core.domain.value.ProfileId
import org.json.JSONArray
import org.json.JSONObject

class ProfileJsonCodec {

    fun encode(profile: Profile): String {
        val o = JSONObject()
        o.put("id", profile.id.value)
        o.put("name", profile.name)
        o.put("description", profile.description)
        o.put("tags", listToJson(profile.tags))

        o.put("protocolType", profile.protocolType.name)
        o.put("transportConfig", encodeTransport(profile.protocolType, profile.transportConfig))

        o.put("deliverySemantics", profile.deliverySemantics.name)
        o.put("ackStrategy", encodeAck(profile.ackStrategy))

        o.put("outboxPolicy", encodeOutbox(profile.outboxPolicy))
        o.put("retryPolicy", encodeRetry(profile.retryPolicy))
        o.put("reconnectPolicy", encodeReconnect(profile.reconnectPolicy))

        o.put("payloadProfile", encodePayload(profile.payloadProfile))
        o.put("chaosProfile", encodeChaos(profile.chaosProfile))

        return o.toString()
    }

    fun decode(json: String): Profile {
        val o = JSONObject(json)

        val id = ProfileId(o.getString("id"))
        val name = o.getString("name")
        val desc = o.getStringOrDefault("description", "")
        val tags = o.optJSONArray("tags")?.toStringList() ?: emptyList()

        val protocolType = ProtocolType.valueOf(o.getString("protocolType"))
        val transport = decodeTransport(protocolType, o.getJSONObject("transportConfig"))

        val delivery = DeliverySemantics.valueOf(o.getString("deliverySemantics"))
        val ack = decodeAck(o.getJSONObject("ackStrategy"))

        val outbox = decodeOutbox(o.getJSONObject("outboxPolicy"))
        val retry = decodeRetry(o.getJSONObject("retryPolicy"))
        val reconnect = decodeReconnect(o.getJSONObject("reconnectPolicy"))

        val payload = decodePayload(o.getJSONObject("payloadProfile"))
        val chaos = decodeChaos(o.getJSONObject("chaosProfile"))

        return Profile(
            id = id,
            name = name,
            description = desc,
            tags = tags,
            protocolType = protocolType,
            transportConfig = transport,
            deliverySemantics = delivery,
            ackStrategy = ack,
            outboxPolicy = outbox,
            retryPolicy = retry,
            reconnectPolicy = reconnect,
            payloadProfile = payload,
            chaosProfile = chaos
        )
    }

    // ----------------- transport -----------------

    private fun encodeTransport(protocolType: ProtocolType, cfg: TransportConfig): JSONObject {
        val o = JSONObject()
        o.put("endpoint", cfg.endpoint)
        o.put("headers", mapToJson(cfg.headers))

        when (protocolType) {
            ProtocolType.WS_OKHTTP -> {
                val c = cfg as WsOkHttpConfig
                o.put("type", "WsOkHttpConfig")
                o.put("pingIntervalMs", c.pingIntervalMs)
            }
            ProtocolType.WS_KTOR -> {
                val c = cfg as WsKtorConfig
                o.put("type", "WsKtorConfig")
                o.put("pingIntervalMs", c.pingIntervalMs)
                o.put("connectTimeoutMs", c.connectTimeoutMs)
            }
            ProtocolType.MQTT -> {
                val c = cfg as MqttConfig
                o.put("type", "MqttConfig")
                o.put("clientId", c.clientId)
                o.put("topic", c.topic)
                o.put("qos", c.qos)
                o.put("cleanSession", c.cleanSession)
                c.username?.let { o.put("username", it) }
                c.password?.let { o.put("password", it) }
            }
            ProtocolType.SOCKETIO -> {
                val c = cfg as SocketIoConfig
                o.put("type", "SocketIoConfig")
                c.namespace?.let { o.put("namespace", it) }
                c.connectPath?.let { o.put("connectPath", it) }
                o.put("events", listToJson(c.events))
            }
            ProtocolType.SIGNALR -> {
                val c = cfg as SignalRConfig
                o.put("type", "SignalRConfig")
                o.put("hubMethodName", c.hubMethodName)
                o.put("transportPreference", c.transportPreference.name)
            }
        }
        return o
    }

    private fun decodeTransport(protocolType: ProtocolType, o: JSONObject): TransportConfig {
        val endpoint = o.getString("endpoint")
        val headers = o.optJSONObject("headers")?.toStringMap() ?: emptyMap()

        return when (protocolType) {
            ProtocolType.WS_OKHTTP -> WsOkHttpConfig(
                endpoint = endpoint,
                pingIntervalMs = o.getLongOrDefault("pingIntervalMs", 15_000),
                headers = headers
            )
            ProtocolType.WS_KTOR -> WsKtorConfig(
                endpoint = endpoint,
                pingIntervalMs = o.getLongOrDefault("pingIntervalMs", 15_000),
                connectTimeoutMs = o.getLongOrDefault("connectTimeoutMs", 10_000),
                headers = headers
            )
            ProtocolType.MQTT -> MqttConfig(
                endpoint = endpoint,
                clientId = o.getString("clientId"),
                topic = o.getString("topic"),
                qos = o.getIntOrDefault("qos", 1),
                cleanSession = o.getBooleanOrDefault("cleanSession", true),
                username = o.optStringOrNull("username"),
                password = o.optStringOrNull("password"),
                headers = headers
            )
            ProtocolType.SOCKETIO -> SocketIoConfig(
                endpoint = endpoint,
                namespace = o.optStringOrNull("namespace"),
                connectPath = o.optStringOrNull("connectPath"),
                events = o.optJSONArray("events")?.toStringList() ?: listOf("message"),
                headers = headers
            )
            ProtocolType.SIGNALR -> SignalRConfig(
                endpoint = endpoint,
                hubMethodName = o.getStringOrDefault("hubMethodName", "Send"),
                transportPreference = SignalRTransportPreference.valueOf(
                    o.getStringOrDefault("transportPreference", SignalRTransportPreference.Auto.name)
                ),
                headers = headers
            )
        }
    }

    // ----------------- ack -----------------

    private fun encodeAck(ack: AckStrategy): JSONObject {
        val o = JSONObject()
        when (ack) {
            is AckStrategy.None -> {
                o.put("type", "None")
            }
            is AckStrategy.TransportLevel -> {
                o.put("type", "TransportLevel")
            }
            is AckStrategy.ApplicationLevel -> {
                o.put("type", "ApplicationLevel")
                o.put("ackTimeoutMs", ack.ackTimeoutMs)
            }
        }
        return o
    }

    private fun decodeAck(o: JSONObject): AckStrategy {
        return when (o.getString("type")) {
            "None" -> AckStrategy.None
            "TransportLevel" -> AckStrategy.TransportLevel
            "ApplicationLevel" -> AckStrategy.ApplicationLevel(
                ackTimeoutMs = o.getLongOrDefault("ackTimeoutMs", 5_000)
            )
            else -> AckStrategy.TransportLevel
        }
    }

    // ----------------- policies -----------------

    private fun encodeOutbox(p: OutboxPolicy): JSONObject = JSONObject()
        .put("enabled", p.enabled)
        .put("maxQueueSize", p.maxQueueSize)
        .put("persistToDisk", p.persistToDisk)

    private fun decodeOutbox(o: JSONObject): OutboxPolicy = OutboxPolicy(
        enabled = o.getBooleanOrDefault("enabled", true),
        maxQueueSize = o.getIntOrDefault("maxQueueSize", 1_000),
        persistToDisk = o.getBooleanOrDefault("persistToDisk", true)
    )

    private fun encodeRetry(p: RetryPolicy): JSONObject = JSONObject()
        .put("maxAttempts", p.maxAttempts)
        .put("initialBackoffMs", p.initialBackoffMs)
        .put("maxBackoffMs", p.maxBackoffMs)
        .put("jitterRatio", p.jitterRatio)

    private fun decodeRetry(o: JSONObject): RetryPolicy = RetryPolicy(
        maxAttempts = o.getIntOrDefault("maxAttempts", 5),
        initialBackoffMs = o.getLongOrDefault("initialBackoffMs", 500),
        maxBackoffMs = o.getLongOrDefault("maxBackoffMs", 30_000),
        jitterRatio = o.getDoubleOrDefault("jitterRatio", 0.2)
    )

    private fun encodeReconnect(p: ReconnectPolicy): JSONObject = JSONObject()
        .put("enabled", p.enabled)
        .put("maxAttempts", p.maxAttempts)
        .put("backoffMs", p.backoffMs)

    private fun decodeReconnect(o: JSONObject): ReconnectPolicy = ReconnectPolicy(
        enabled = o.getBooleanOrDefault("enabled", true),
        maxAttempts = o.getIntOrDefault("maxAttempts", Int.MAX_VALUE),
        backoffMs = o.getLongOrDefault("backoffMs", 2_000)
    )

    // ----------------- payload/chaos -----------------

    private fun encodePayload(p: PayloadProfile): JSONObject = JSONObject()
        .put("codec", p.codec.name)
        .put("targetSizeBytes", p.targetSizeBytes)
        .put("pattern", p.pattern.name)
        .put("seed", p.seed)

    private fun decodePayload(o: JSONObject): PayloadProfile = PayloadProfile(
        codec = CodecMode.valueOf(o.getStringOrDefault("codec", CodecMode.StandardEnvelope.name)),
        targetSizeBytes = o.getIntOrDefault("targetSizeBytes", 1024),
        pattern = PayloadPattern.valueOf(o.getStringOrDefault("pattern", PayloadPattern.Text.name)),
        seed = o.getLongOrDefault("seed", 42)
    )

    private fun encodeChaos(c: ChaosProfile): JSONObject {
        val o = JSONObject()
        o.put("enabled", c.enabled)
        o.put("dropRatePercent", c.dropRatePercent)
        o.put("delayMinMs", c.delayMinMs)
        o.put("delayMaxMs", c.delayMaxMs)
        o.put("jitterMs", c.jitterMs)
        o.put("seed", c.seed)

        val arr = JSONArray()
        c.disconnectSchedule.forEach {
            arr.put(JSONObject().put("atMsFromStart", it.atMsFromStart).put("durationMs", it.durationMs))
        }
        o.put("disconnectSchedule", arr)

        return o
    }

    private fun decodeChaos(o: JSONObject): ChaosProfile {
        val schedule = mutableListOf<DisconnectWindow>()
        val arr = o.optJSONArray("disconnectSchedule") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val w = arr.getJSONObject(i)
            schedule += DisconnectWindow(
                atMsFromStart = w.getLong("atMsFromStart"),
                durationMs = w.getLong("durationMs")
            )
        }
        return ChaosProfile(
            enabled = o.getBooleanOrDefault("enabled", false),
            dropRatePercent = o.getDoubleOrDefault("dropRatePercent", 0.0),
            delayMinMs = o.getLongOrDefault("delayMinMs", 0),
            delayMaxMs = o.getLongOrDefault("delayMaxMs", 0),
            jitterMs = o.getLongOrDefault("jitterMs", 0),
            disconnectSchedule = schedule,
            seed = o.getLongOrDefault("seed", 42)
        )
    }
}
