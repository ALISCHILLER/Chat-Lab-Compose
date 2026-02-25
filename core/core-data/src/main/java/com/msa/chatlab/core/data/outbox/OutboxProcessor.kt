package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.common.util.Backoff
import com.msa.chatlab.core.data.codec.WirePayloadCodec
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.telemetry.TelemetryHeaders
import com.msa.chatlab.core.data.telemetry.TelemetryLogger
import com.msa.chatlab.core.data.util.HeaderJson
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.domain.model.OutboxStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.msa.chatlab.core.data.active.ActiveProfileStore

class OutboxProcessor(
    private val outboxQueue: OutboxQueue,
    private val connectionManager: ConnectionManager,
    private val activeProfileStore: ActiveProfileStore,
    private val messageDao: MessageDao,
    private val wireCodec: WirePayloadCodec,
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
                        outboxQueue.requeueExpiredInflight(p.id.value, p.outboxPolicy.stallTimeoutMillis)
                    }
                    delay(2_000)
                }
            }
        }

        if (flushJob == null) {
            flushJob = scope.launch {
                combine(
                    connectionManager.connectionState,
                    connectionManager.simulateOffline,
                    activeProfileStore.activeProfile
                ) { state, offline, profile -> Quad(state, offline, profile, Unit) }
                    .distinctUntilChanged()
                    .collectLatest { (state, offline, profile) ->
                        if (state is ConnectionState.Connected && !offline && profile != null) {
                            flushQueue(profile.id.value)
                        } else {
                            // No need to do anything, the flush is either running or not applicable
                        }
                    }
            }
        }
    }

    fun stop() {
        flushJob?.cancel()
        flushJob = null
        maintenanceJob?.cancel()
        maintenanceJob = null
    }

    private suspend fun flushQueue(profileId: String) {
        val profile = activeProfileStore.getActiveNow() ?: return
        val retryPolicy = profile.retryPolicy
        val outboxPolicy = profile.outboxPolicy

        while (scope.isActive) {
            val items = outboxQueue.claimPendingBatch(
                profileId = profileId,
                leaseMs = outboxPolicy.stallTimeoutMillis,
                limit = 10
            )
            if (items.isEmpty()) {
                delay(500) // Wait before polling again
                continue
            }

            for (item in items) {
                if (!scope.isActive) break

                try {
                    val envelope = Envelope(
                        messageId = com.msa.chatlab.core.domain.value.MessageId(item.messageId),
                        createdAt = com.msa.chatlab.core.domain.value.TimestampMillis(item.createdAt),
                        contentType = item.contentType,
                        headers = HeaderJson.decode(item.headersJson),
                        body = item.body
                    )
                    val payload = OutgoingPayload(envelope, item.destination)

                    messageDao.updateDelivery(profileId, item.messageId, true, item.attempt, MessageStatus.Sending.name, null, System.currentTimeMillis())

                    outboxQueue.remove(profileId, item.messageId)
                    
                    connectionManager.send(payload)

                    messageDao.updateDelivery(profileId, item.messageId, false, item.attempt, MessageStatus.Sent.name, null, System.currentTimeMillis())
                    
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        outboxQueue.updateAttempt(profileId, item.messageId, item.attempt, OutboxStatus.PENDING, "Job cancelled")
                        break
                    }

                    val err = e.message ?: "Send error"

                    val nextAttempt = item.attempt + 1
                    outboxQueue.updateAttempt(profileId, item.messageId, nextAttempt, OutboxStatus.PENDING, err)
                    messageDao.updateDelivery(profileId, item.messageId, true, nextAttempt, MessageStatus.Sending.name, err, System.currentTimeMillis())

                    val delayMs = Backoff.fixed(retryPolicy.delayMillis, retryPolicy.jitterRatio)
                    delay(delayMs)
                }
            }

            // yield کوچک بین batch ها
            delay(5)
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
