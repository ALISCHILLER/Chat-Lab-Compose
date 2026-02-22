package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.ack.AckTracker
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.dedup.DedupStore
import com.msa.chatlab.core.data.telemetry.TelemetryHeaders
import com.msa.chatlab.core.data.telemetry.TelemetryLogger
import com.msa.chatlab.core.data.util.HeaderJson
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class TransportMessageBinder(
    private val activeProfileStore: ActiveProfileStore,
    private val connectionManager: ConnectionManager,
    private val messageDao: MessageDao,
    private val ackTracker: AckTracker,
    private val dedupStore: DedupStore,
    private val telemetryLogger: TelemetryLogger
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch {
            connectionManager.events.collectLatest { ev ->
                when (ev) {

                    is TransportEvent.MessageReceived -> {
                        val profile = activeProfileStore.getActiveNow() ?: return@collectLatest
                        val env = ev.payload.envelope
                        val now = System.currentTimeMillis()

                        val headers = env.headers
                        telemetryLogger.logRecv(env.messageId.value, ev.payload.source, headers)

                        // ✅ اگر OUT بوده => ack/echo
                        val dir = messageDao.getDirection(profile.id.value, env.messageId.value)
                        if (dir == "OUT") {
                            messageDao.updateDelivery(
                                profileId = profile.id.value,
                                messageId = env.messageId.value,
                                queued = false,
                                attempt = 0,
                                status = MessageStatus.Delivered.name,
                                lastError = null,
                                updatedAt = now
                            )
                            ackTracker.onAck(env.messageId.value)
                            return@collectLatest
                        }

                        // ✅ Dedup: کلید را از idempotency header بگیر، fallback = messageId
                        val dedupKey = headers[TelemetryHeaders.IDEMPOTENCY_KEY] ?: env.messageId.value
                        if (!dedupStore.shouldProcess(dedupKey)) {
                            return@collectLatest
                        }

                        val text =
                            if (env.contentType.startsWith("text/")) env.body.decodeToString()
                            else "<${env.contentType} • ${env.body.size} bytes>"

                        val destinationKey = ev.payload.source ?: "default"

                        messageDao.upsert(
                            MessageEntity(
                                profileId = profile.id.value,
                                messageId = env.messageId.value,
                                direction = "IN",
                                destination = destinationKey,
                                source = ev.payload.source,
                                contentType = env.contentType,
                                headersJson = HeaderJson.encode(headers),
                                text = text,
                                createdAt = env.createdAt.value,
                                status = MessageStatus.Delivered.name,
                                queued = false,
                                attempt = 0,
                                lastError = null,
                                updatedAt = now
                            )
                        )
                    }

                    is TransportEvent.MessageSent -> {
                        messageDao.updateStatusByMessageId(
                            messageId = ev.messageId,
                            status = MessageStatus.Sent.name,
                            lastError = null,
                            updatedAt = System.currentTimeMillis()
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}