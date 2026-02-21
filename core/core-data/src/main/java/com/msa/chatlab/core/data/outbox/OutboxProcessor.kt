package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.codec.StandardEnvelopeCodec
import com.msa.chatlab.core.data.codec.WirePayloadCodec
import com.msa.chatlab.core.data.lab.ChaosEngine
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.domain.model.RetryPolicy
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class OutboxProcessor(
    private val outboxQueue: OutboxQueue,
    private val connectionManager: ConnectionManager,
    private val activeProfileStore: ActiveProfileStore,
    private val messageDao: MessageDao,
    private val wireCodec: WirePayloadCodec
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var flushJob: Job? = null

    fun start() {
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
            ) { st, profile, pendingCount, simOffline ->
                Quad(st, profile, pendingCount, simOffline)
            }.collectLatest { (st, profile, pendingCount, simOffline) ->
                val shouldFlush = st is ConnectionState.Connected && profile != null && pendingCount > 0 && !simOffline
                if (shouldFlush) {
                    startOrContinueFlush(profile!!.id.value)
                }
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
        val chaos = ChaosEngine(profile.chaosProfile.seed)

        while (connectionManager.isConnectedNow() && !connectionManager.simulateOffline.value) {
            val item = outboxQueue.peekOldestPending(profileId) ?: break

            if (item.attempt >= retryPolicy.maxAttempts) {
                outboxQueue.updateAttempt(profileId, item.messageId, item.attempt, OutboxStatus.FAILED, "Max retries reached")
                messageDao.updateDelivery(
                    profileId = profileId,
                    messageId = item.messageId,
                    queued = false,
                    attempt = item.attempt,
                    status = MessageStatus.Failed.name,
                    lastError = "Max retries reached",
                    updatedAt = System.currentTimeMillis()
                )
                continue
            }

            try {
                // chaos drop/delay
                if (profile.chaosProfile.enabled && chaos.shouldDrop(profile.chaosProfile.dropRatePercent)) {
                    throw RuntimeException("Chaos drop simulated")
                }
                val extraDelay = if (profile.chaosProfile.enabled) {
                    chaos.extraDelayMs(profile.chaosProfile.delayMinMs, profile.chaosProfile.delayMaxMs)
                } else 0
                if (extraDelay > 0) delay(extraDelay)

                val env = Envelope(
                    messageId = com.msa.chatlab.core.domain.value.MessageId(item.messageId),
                    createdAt = com.msa.chatlab.core.domain.value.TimestampMillis(item.createdAt),
                    contentType = item.contentType,
                    headers = emptyMap(),
                    body = item.body
                )
                val basePayload = OutgoingPayload(envelope = env, destination = item.destination)
                val payload = wireCodec.encode(profile, basePayload)

                messageDao.updateDelivery(
                    profileId = profileId,
                    messageId = item.messageId,
                    queued = false,
                    attempt = item.attempt,
                    status = MessageStatus.Sending.name,
                    lastError = null,
                    updatedAt = System.currentTimeMillis()
                )

                connectionManager.send(payload)
                outboxQueue.remove(profileId, item.messageId)

            } catch (e: Exception) {
                val nextAttempt = item.attempt + 1
                val err = e.message ?: "Unknown send error"

                outboxQueue.updateAttempt(profileId, item.messageId, nextAttempt, OutboxStatus.PENDING, err)

                messageDao.updateDelivery(
                    profileId = profileId,
                    messageId = item.messageId,
                    queued = true,
                    attempt = nextAttempt,
                    status = MessageStatus.Sending.name,
                    lastError = err,
                    updatedAt = System.currentTimeMillis()
                )

                delay(calculateBackoff(nextAttempt, retryPolicy))
            }
        }
    }

    private fun calculateBackoff(attempt: Int, policy: RetryPolicy): Long {
        val base = policy.initialBackoffMs * (2.0.pow(attempt.toDouble()))
        val capped = min(base, policy.maxBackoffMs.toDouble()).toLong()
        val jitter = (capped * policy.jitterRatio * (2 * Random.nextDouble() - 1)).toLong()
        return (capped + jitter).coerceAtLeast(0)
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}