package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.common.util.Backoff
import com.msa.chatlab.core.data.ack.AckTracker
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.codec.WirePayloadCodec
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.telemetry.TelemetryHeaders
import com.msa.chatlab.core.data.telemetry.TelemetryLogger
import com.msa.chatlab.core.data.util.HeaderJson
import com.msa.chatlab.core.domain.model.AckStrategy
import com.msa.chatlab.core.domain.model.DeliverySemantics
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class OutboxProcessor(
    private val outboxQueue: OutboxQueue,
    private val connectionManager: ConnectionManager,
    private val activeProfileStore: ActiveProfileStore,
    private val messageDao: MessageDao,
    private val wireCodec: WirePayloadCodec,
    private val ackTracker: AckTracker,
    private val telemetryLogger: TelemetryLogger
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var flushJob: Job? = null
    private var maintenanceJob: Job? = null

    fun start() {
        if (maintenanceJob == null) {
            maintenanceJob = scope.launch {
                while (isActive) {
                    val p = activeProfileStore.getActiveNow()
                    if (p != null) {
                        outboxQueue.requeueExpiredInflight(p.id.value, p.outboxPolicy.inFlightLeaseMs)
                    }
                    delay(2_000)
                }
            }
        }

        scope.launch {
            val profileFlow = activeProfileStore.activeProfile
            val pendingCountFlow = profileFlow.flatMapLatest { p ->
                if (p == null) flowOf(0) else outboxQueue.observeCount(p.id.value, OutboxStatus.PENDING)
            }

            combine(
                connectionManager.connectionState,
                profileFlow,
                pendingCountFlow,
                connectionManager.simulateOffline
            ) { st, profile, pending, simOffline ->
                Quad(st, profile, pending, simOffline)
            }.collectLatest { (st, profile, pending, simOffline) ->
                val shouldFlush =
                    st is ConnectionState.Connected && profile != null && pending > 0 && !simOffline

                if (shouldFlush) startOrContinueFlush(profile!!.id.value)
            }
        }
    }

    private fun startOrContinueFlush(profileId: String) {
        if (flushJob?.isActive == true) return
        flushJob = scope.launch { flush(profileId) }
    }

    private suspend fun flush(profileId: String) {
        val profile = activeProfileStore.getActiveNow() ?: return

        val retryPolicy = profile.retryPolicy
        val outboxPolicy = profile.outboxPolicy
        val deliveryPolicy = profile.deliveryPolicy

        // requeue expired inflight before sending
        outboxQueue.requeueExpiredInflight(profileId, outboxPolicy.inFlightLeaseMs)

        val batchSize = outboxPolicy.flushBatchSize.coerceIn(1, 256)

        while (connectionManager.isConnectedNow() && !connectionManager.simulateOffline.value) {

            val batch = outboxQueue.claimPendingBatch(profileId, outboxPolicy.inFlightLeaseMs, limit = batchSize)
            if (batch.isEmpty()) break

            // اگر وسط batch disconnect شد، باقی‌ها را سریع برگردان PENDING
            fun requeueRemaining(fromIndex: Int) {
                for (i in fromIndex until batch.size) {
                    val it = batch[i]
                    scope.launch {
                        outboxQueue.updateAttempt(profileId, it.messageId, it.attempt, OutboxStatus.PENDING, "Disconnected during batch")
                    }
                }
            }

            for ((index, item) in batch.withIndex()) {

                if (!connectionManager.isConnectedNow() || connectionManager.simulateOffline.value) {
                    requeueRemaining(index)
                    return
                }

                if (item.attempt >= retryPolicy.maxAttempts) {
                    outboxQueue.updateAttempt(profileId, item.messageId, item.attempt, OutboxStatus.FAILED, "Max retries reached")
                    messageDao.updateDelivery(profileId, item.messageId, false, item.attempt, MessageStatus.Failed.name, "Max retries reached", System.currentTimeMillis())
                    continue
                }

                try {
                    val headers = HeaderJson.decode(item.headersJson).toMutableMap()

                    // ✅ تضمین idempotency key
                    headers.putIfAbsent(TelemetryHeaders.IDEMPOTENCY_KEY, item.messageId)

                    val env = Envelope(
                        messageId = com.msa.chatlab.core.domain.value.MessageId(item.messageId),
                        createdAt = com.msa.chatlab.core.domain.value.TimestampMillis(item.createdAt),
                        contentType = item.contentType,
                        headers = headers,
                        body = item.body
                    )

                    val basePayload = OutgoingPayload(envelope = env, destination = item.destination)
                    val payload = wireCodec.encode(profile, basePayload)

                    telemetryLogger.logSend(item.messageId, item.destination, payload.envelope.headers)

                    messageDao.updateDelivery(profileId, item.messageId, queued = false, attempt = item.attempt, status = MessageStatus.Sending.name, lastError = null, updatedAt = System.currentTimeMillis())

                    connectionManager.send(payload)

                    when (val ack = deliveryPolicy.ackStrategy) {
                        AckStrategy.None, AckStrategy.TransportLevel -> {
                            outboxQueue.remove(profileId, item.messageId)
                            messageDao.updateDelivery(profileId, item.messageId, false, item.attempt, MessageStatus.Sent.name, null, System.currentTimeMillis())
                        }

                        is AckStrategy.ApplicationLevel -> {
                            val ok = ackTracker.awaitAck(item.messageId, timeoutMs = ack.ackTimeoutMs)
                            if (ok) {
                                outboxQueue.remove(profileId, item.messageId)
                                messageDao.updateDelivery(profileId, item.messageId, false, item.attempt, MessageStatus.Delivered.name, null, System.currentTimeMillis())
                            } else {
                                if (deliveryPolicy.semantics == DeliverySemantics.AtMostOnce) {
                                    outboxQueue.remove(profileId, item.messageId)
                                    messageDao.updateDelivery(profileId, item.messageId, false, item.attempt, MessageStatus.Failed.name, "ACK timeout (AtMostOnce)", System.currentTimeMillis())
                                } else {
                                    val nextAttempt = item.attempt + 1
                                    outboxQueue.updateAttempt(profileId, item.messageId, nextAttempt, OutboxStatus.PENDING, "ACK timeout")
                                    messageDao.updateDelivery(profileId, item.messageId, true, nextAttempt, MessageStatus.Sending.name, "ACK timeout", System.currentTimeMillis())

                                    val delayMs = Backoff.exponential(nextAttempt, retryPolicy.initialBackoffMs, retryPolicy.maxBackoffMs, retryPolicy.jitterRatio)
                                    delay(delayMs)
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    if (!connectionManager.isConnectedNow() || connectionManager.simulateOffline.value) {
                        // برگردان PENDING بدون افزایش attempt
                        outboxQueue.updateAttempt(profileId, item.messageId, item.attempt, OutboxStatus.PENDING, "Disconnected")
                        requeueRemaining(index + 1)
                        return
                    }

                    val err = e.message ?: "Send error"

                    if (deliveryPolicy.semantics == DeliverySemantics.AtMostOnce) {
                        outboxQueue.remove(profileId, item.messageId)
                        messageDao.updateDelivery(profileId, item.messageId, false, item.attempt, MessageStatus.Failed.name, err, System.currentTimeMillis())
                        continue
                    }

                    val nextAttempt = item.attempt + 1
                    outboxQueue.updateAttempt(profileId, item.messageId, nextAttempt, OutboxStatus.PENDING, err)
                    messageDao.updateDelivery(profileId, item.messageId, true, nextAttempt, MessageStatus.Sending.name, err, System.currentTimeMillis())

                    val delayMs = Backoff.exponential(nextAttempt, retryPolicy.initialBackoffMs, retryPolicy.maxBackoffMs, retryPolicy.jitterRatio)
                    delay(delayMs)
                }
            }

            // yield کوچک بین batch ها
            delay(5)
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}