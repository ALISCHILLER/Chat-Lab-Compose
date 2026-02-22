package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.codec.WirePayloadCodec
import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.data.telemetry.TelemetryHeaders
import com.msa.chatlab.core.data.telemetry.Trace
import com.msa.chatlab.core.data.util.HeaderJson
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.storage.entity.OutboxStatus
import java.util.UUID

class MessageSender(
    private val activeProfileStore: ActiveProfileStore,
    private val messageRepository: MessageRepository,
    private val outboxQueue: OutboxQueue,
    private val wireCodec: WirePayloadCodec
) {

    suspend fun sendText(text: String, destination: String) {
        val mid = UUID.randomUUID().toString()
        sendTextWithMessageId(text, destination, mid)
    }

    suspend fun sendTextWithMessageId(text: String, destination: String, messageId: String) {
        val profile = activeProfileStore.getActiveNow() ?: error("No active profile")
        val mid = MessageId(messageId)
        val now = System.currentTimeMillis()

        // persist OUT message (business text)
        messageRepository.insertOutgoing(
            profileId = profile.id,
            messageId = mid,
            text = text,
            destination = destination
        )

        // ✅ telemetry + idempotency headers
        val headers = mutableMapOf<String, String>()
        Trace.inject(headers, Trace.newRoot())
        headers[TelemetryHeaders.IDEMPOTENCY_KEY] = messageId // default

        val env = Envelope(
            messageId = mid,
            createdAt = TimestampMillis(now),
            contentType = "text/plain",
            headers = headers,
            body = text.encodeToByteArray()
        )

        val wire = wireCodec.encode(profile, OutgoingPayload(envelope = env, destination = destination))

        outboxQueue.enqueue(
            OutboxItem(
                profileId = profile.id.value,
                messageId = mid.value,
                destination = destination,
                contentType = wire.envelope.contentType,
                headersJson = HeaderJson.encode(wire.envelope.headers),
                body = wire.envelope.body,
                createdAt = now,
                attempt = 0,
                lastAttemptAt = null,
                lastError = null,
                status = OutboxStatus.PENDING
            )
        )
    }
}