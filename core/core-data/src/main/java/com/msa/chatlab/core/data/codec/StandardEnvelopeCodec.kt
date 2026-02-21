package com.msa.chatlab.core.data.codec

import android.util.Base64
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import org.json.JSONObject

/**
 * JSON wire envelope for preserving messageId through echo-based transports.
 *
 * Format:
 * { id, createdAt, contentType, headers{...}, bodyB64 }
 */
object StandardEnvelopeCodec {
    private const val KEY_ID = "id"
    private const val KEY_CREATED_AT = "createdAt"
    private const val KEY_CT = "contentType"
    private const val KEY_HEADERS = "headers"
    private const val KEY_BODY_B64 = "bodyB64"

    fun encode(envelope: Envelope): String {
        val o = JSONObject()
        o.put(KEY_ID, envelope.messageId.value)
        o.put(KEY_CREATED_AT, envelope.createdAt.value)
        o.put(KEY_CT, envelope.contentType)

        val h = JSONObject()
        envelope.headers.forEach { (k, v) -> h.put(k, v) }
        o.put(KEY_HEADERS, h)

        val b64 = Base64.encodeToString(envelope.body, Base64.NO_WRAP)
        o.put(KEY_BODY_B64, b64)
        return o.toString()
    }

    /** Returns decoded Envelope if payload matches our format; otherwise null. */
    fun decodeOrNull(text: String): Envelope? {
        val t = text.trim()
        if (!t.startsWith("{") || !t.endsWith("}")) return null

        return runCatching {
            val o = JSONObject(t)
            if (!o.has(KEY_ID) || !o.has(KEY_CT) || !o.has(KEY_BODY_B64)) return@runCatching null

            val id = MessageId(o.getString(KEY_ID))
            val createdAt = TimestampMillis(o.optLong(KEY_CREATED_AT, System.currentTimeMillis()))
            val ct = o.getString(KEY_CT)

            val headersObj = o.optJSONObject(KEY_HEADERS)
            val headers = buildMap {
                if (headersObj != null) {
                    val it = headersObj.keys()
                    while (it.hasNext()) {
                        val k = it.next()
                        put(k, headersObj.optString(k, ""))
                    }
                }
            }

            val body = Base64.decode(o.getString(KEY_BODY_B64), Base64.NO_WRAP)

            Envelope(
                messageId = id,
                createdAt = createdAt,
                contentType = ct,
                headers = headers,
                body = body
            )
        }.getOrNull()
    }
}