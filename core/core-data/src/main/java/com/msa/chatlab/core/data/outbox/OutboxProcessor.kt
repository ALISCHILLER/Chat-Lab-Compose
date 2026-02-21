package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.domain.model.RetryPolicy
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.domain.value.TimestampMillis
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.entity.OutboxStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class OutboxProcessor(
    private val outboxQueue: OutboxQueue,
    private val connectionManager: ConnectionManager,
    private val activeProfileStore: ActiveProfileStore,
    private val messageDao: MessageDao,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    fun start() {
        scope.launch {
            combine(connectionManager.connectionState, activeProfileStore.activeProfile) { st, p -> st to p }
                .collectLatest { (st, profile) ->
                    if (st is ConnectionState.Connected && profile != null) {
                        flush(profile.id.value, profile.retryPolicy ?: RetryPolicy())
                    }
                }
        }
    }

    private suspend fun flush(profileId: String, retryPolicy: RetryPolicy) {
        while (connectionManager.isConnectedNow()) {
            val item = outboxQueue.peekOldestPending(profileId) ?: break

            // سقف retry → FAILED
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
                val env = Envelope(
                    messageId = MessageId(item.messageId),
                    createdAt = TimestampMillis(item.createdAt),
                    contentType = item.contentType,
                    headers = emptyMap(),
                    body = item.body
                )
                val payload = OutgoingPayload(envelope = env, destination = item.destination)

                // status: Sending (Binder وقتی MessageSent رسید -> Sent)
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
        val baseBackoff = policy.initialBackoffMs * (2.0.pow(attempt.toDouble()))
        val cappedBackoff = min(baseBackoff, policy.maxBackoffMs.toDouble()).toLong()
        val jitter = (cappedBackoff * policy.jitterRatio * (2 * Random.nextDouble() - 1)).toLong()
        return (cappedBackoff + jitter).coerceAtLeast(0)
    }
}
