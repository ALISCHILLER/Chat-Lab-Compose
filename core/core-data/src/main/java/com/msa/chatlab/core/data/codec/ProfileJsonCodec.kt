package com.msa.chatlab.core.data.codec

import com.msa.chatlab.core.domain.model.*
import com.msa.chatlab.core.domain.value.ProfileId
import org.json.JSONArray
import org.json.JSONObject

class ProfileJsonCodec {

    private fun jStr(o: org.json.JSONObject, key: String, default: String): String =
        if (o.has(key) && !o.isNull(key)) o.optString(key, default) else default

    private fun jLong(o: org.json.JSONObject, key: String, default: Long): Long =
        if (o.has(key) && !o.isNull(key)) o.optLong(key, default) else default

    private fun jInt(o: org.json.JSONObject, key: String, default: Int): Int =
        if (o.has(key) && !o.isNull(key)) o.optInt(key, default) else default

    private fun jDouble(o: org.json.JSONObject, key: String, default: Double): Double =
        if (o.has(key) && !o.isNull(key)) o.optDouble(key, default) else default

    private fun jBool(o: org.json.JSONObject, key: String, default: Boolean): Boolean =
        if (o.has(key) && !o.isNull(key)) o.optBoolean(key, default) else default

    private fun jOptStr(o: org.json.JSONObject, key: String): String? =
        if (o.has(key) && !o.isNull(key)) o.optString(key) else null

    fun encode(profile: Profile): String {
        val o = JSONObject()
        o.put("id", profile.id.value)
        o.put("name", profile.name)
        o.put("description", profile.description)
        o.put("tags", listToJson(profile.tags))

        o.put("protocolType", profile.protocolType.name)
        o.put("transportConfig", encodeTransport(profile.protocolType, profile.transportConfig))

        o.put("deliveryPolicy", encodeDeliveryPolicy(profile.deliveryPolicy))

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
        val desc = jStr(o, "description", "")
        val tags = o.optJSONArray("tags")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()

        val protocolType = ProtocolType.valueOf(o.getString("protocolType"))
        val transport = decodeTransport(protocolType, o.getJSONObject("transportConfig"))

        val deliveryPolicy = decodeDeliveryPolicy(o.optJSONObject("deliveryPolicy") ?: JSONObject())

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
            deliveryPolicy = deliveryPolicy,
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

    private fun decodeTransport(protocolType: ProtocolType, o: org.json.JSONObject): TransportConfig {
        val endpoint = o.getString("endpoint")
        val headers = o.optJSONObject("headers")?.let { obj ->
            val itKeys = obj.keys()
            buildMap<String, String> {
                while (itKeys.hasNext()) {
                    val k = itKeys.next()
                    put(k, obj.optString(k, ""))
                }
            }
        } ?: emptyMap()

        return when (protocolType) {
            ProtocolType.WS_OKHTTP -> WsOkHttpConfig(
                endpoint = endpoint,
                pingIntervalMs = jLong(o, "pingIntervalMs", 15_000),
                headers = headers
            )

            ProtocolType.WS_KTOR -> WsKtorConfig(
                endpoint = endpoint,
                pingIntervalMs = jLong(o, "pingIntervalMs", 15_000),
                connectTimeoutMs = jLong(o, "connectTimeoutMs", 10_000),
                headers = headers
            )

            ProtocolType.MQTT -> MqttConfig(
                endpoint = endpoint,
                clientId = o.getString("clientId"),
                topic = o.getString("topic"),
                qos = jInt(o, "qos", 1),
                cleanSession = jBool(o, "cleanSession", true),
                username = jOptStr(o, "username"),
                password = jOptStr(o, "password"),
                headers = headers
            )

            ProtocolType.SOCKETIO -> SocketIoConfig(
                endpoint = endpoint,
                namespace = jOptStr(o, "namespace"),
                connectPath = jOptStr(o, "connectPath"),
                events = o.optJSONArray("events")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: listOf("message"),
                headers = headers
            )

            ProtocolType.SIGNALR -> SignalRConfig(
                endpoint = endpoint,
                hubMethodName = jStr(o, "hubMethodName", "Send"),
                transportPreference = SignalRTransportPreference.valueOf(
                    jStr(o, "transportPreference", SignalRTransportPreference.Auto.name)
                ),
                headers = headers
            )
        }
    }

    // ----------------- delivery policy -----------------

    private fun encodeAckStrategy(a: AckStrategy): JSONObject {
        return when (a) {
            AckStrategy.None -> JSONObject().put("type", "NONE")
            AckStrategy.TransportLevel -> JSONObject().put("type", "TRANSPORT")
            is AckStrategy.ApplicationLevel -> JSONObject()
                .put("type", "APPLICATION")
                .put("ackTimeoutMs", a.ackTimeoutMs)
        }
    }

    private fun decodeAck(o: org.json.JSONObject): AckStrategy {
        return when (o.optString("type")) {
            "None" -> AckStrategy.None
            "TransportLevel" -> AckStrategy.TransportLevel
            "ApplicationLevel" -> AckStrategy.ApplicationLevel(
                ackTimeoutMs = jLong(o, "ackTimeoutMs", 5_000)
            )
            else -> AckStrategy.TransportLevel
        }
    }

    private fun encodeDeliveryPolicy(p: DeliveryPolicy): JSONObject = JSONObject()
        .put("semantics", p.semantics.name)
        .put("ack", encodeAckStrategy(p.ackStrategy))

    private fun decodeDeliveryPolicy(o: JSONObject): DeliveryPolicy = DeliveryPolicy(
        semantics = runCatching {
            DeliverySemantics.valueOf(jStr(o, "semantics", DeliverySemantics.AtLeastOnce.name))
        }.getOrDefault(DeliverySemantics.AtLeastOnce),
        ackStrategy = decodeAck(o.optJSONObject("ack") ?: JSONObject())
    )

    // ----------------- policies -----------------

    private fun encodeOutbox(p: OutboxPolicy): JSONObject = JSONObject()
        .put("enabled", p.enabled)
        .put("maxQueueSize", p.maxQueueSize)
        .put("persistToDisk", p.persistToDisk)
        .put("inFlightLeaseMs", p.inFlightLeaseMs)
        .put("flushBatchSize", p.flushBatchSize)

    private fun decodeOutbox(o: org.json.JSONObject): OutboxPolicy = OutboxPolicy(
        enabled = jBool(o, "enabled", true),
        maxQueueSize = jInt(o, "maxQueueSize", 1_000),
        persistToDisk = jBool(o, "persistToDisk", true),
        inFlightLeaseMs = jLong(o, "inFlightLeaseMs", 15_000),
        flushBatchSize = jInt(o, "flushBatchSize", 16)
    )

    private fun encodeRetry(p: RetryPolicy): JSONObject = JSONObject()
        .put("maxAttempts", p.maxAttempts)
        .put("initialBackoffMs", p.initialBackoffMs)
        .put("maxBackoffMs", p.maxBackoffMs)
        .put("jitterRatio", p.jitterRatio)

    private fun decodeRetry(o: org.json.JSONObject): RetryPolicy = RetryPolicy(
        maxAttempts = jInt(o, "maxAttempts", 5),
        initialBackoffMs = jLong(o, "initialBackoffMs", 500),
        maxBackoffMs = jLong(o, "maxBackoffMs", 30_000),
        jitterRatio = jDouble(o, "jitterRatio", 0.2)
    )

    private fun encodeReconnect(p: ReconnectPolicy): JSONObject = JSONObject()
        .put("enabled", p.enabled)
        .put("maxAttempts", p.maxAttempts)
        .put("backoffMs", p.backoffMs)
        .put("mode", p.mode.name)
        .put("maxBackoffMs", p.maxBackoffMs)
        .put("jitterRatio", p.jitterRatio)
        .put("resetAfterMs", p.resetAfterMs)

    private fun decodeReconnect(o: org.json.JSONObject): ReconnectPolicy = ReconnectPolicy(
        enabled = jBool(o, "enabled", true),
        maxAttempts = jInt(o, "maxAttempts", Int.MAX_VALUE),
        backoffMs = jLong(o, "backoffMs", 2_000),
        mode = runCatching {
            ReconnectBackoffMode.valueOf(jStr(o, "mode", ReconnectBackoffMode.Exponential.name))
        }.getOrDefault(ReconnectBackoffMode.Exponential),
        maxBackoffMs = jLong(o, "maxBackoffMs", 30_000),
        jitterRatio = jDouble(o, "jitterRatio", 0.2),
        resetAfterMs = jLong(o, "resetAfterMs", 30_000)
    )

    // ----------------- payload/chaos -----------------

    private fun encodePayload(p: PayloadProfile): JSONObject = JSONObject()
        .put("codec", p.codec.name)
        .put("targetSizeBytes", p.targetSizeBytes)
        .put("pattern", p.pattern.name)
        .put("seed", p.seed)

    private fun decodePayload(o: org.json.JSONObject): PayloadProfile = PayloadProfile(
        codec = CodecMode.valueOf(jStr(o, "codec", CodecMode.StandardEnvelope.name)),
        targetSizeBytes = jInt(o, "targetSizeBytes", 1024),
        pattern = PayloadPattern.valueOf(jStr(o, "pattern", PayloadPattern.Text.name)),
        seed = jLong(o, "seed", 42)
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

    private fun decodeChaos(o: org.json.JSONObject): ChaosProfile {
        val schedule = mutableListOf<DisconnectWindow>()
        val arr = o.optJSONArray("disconnectSchedule") ?: org.json.JSONArray()
        for (i in 0 until arr.length()) {
            val w = arr.getJSONObject(i)
            schedule += DisconnectWindow(
                atMsFromStart = w.optLong("atMsFromStart", 0),
                durationMs = w.optLong("durationMs", 0)
            )
        }
        return ChaosProfile(
            enabled = jBool(o, "enabled", false),
            dropRatePercent = jDouble(o, "dropRatePercent", 0.0),
            delayMinMs = jLong(o, "delayMinMs", 0),
            delayMaxMs = jLong(o, "delayMaxMs", 0),
            jitterMs = jLong(o, "jitterMs", 0),
            disconnectSchedule = schedule,
            seed = jLong(o, "seed", 42)
        )
    }

    private fun listToJson(list: List<String>): JSONArray = JSONArray(list)
    private fun mapToJson(map: Map<String, String>): JSONObject = JSONObject(map as Map<*, *>)
}