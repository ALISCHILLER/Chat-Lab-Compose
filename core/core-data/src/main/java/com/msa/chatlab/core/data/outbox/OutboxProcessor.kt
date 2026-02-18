package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.domain.model.RetryPolicy
import com.msa.chatlab.core.domain.value.MessageId
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.payload.Envelope
import com.msa.chatlab.core.protocol.api.payload.OutgoingPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class OutboxProcessor(
    private val outboxQueue: OutboxQueue,
    private val connectionManager: ConnectionManager,
    private val activeProfileStore: ActiveProfileStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    fun start() {
        scope.launch {
            connectionManager.connectionState.collectLatest {
                if (it is ConnectionState.Connected) {
                    flush()
                }
            }
        }
    }

    private suspend fun flush() {
        while (connectionManager.isConnectedNow()) {
            val item = outboxQueue.peekOldest() ?: break

            val profile = activeProfileStore.activeProfile.first()
            val retryPolicy = profile?.retryPolicy ?: RetryPolicy()

            // اگر به سقف رسید → FAILED و برو سراغ آیتم بعدی
            if (item.attempt >= retryPolicy.maxAttempts) {
                outboxQueue.markAsFailed(item.id, "Max retries reached")
                continue
            }

            try {
                val envelope = Envelope.text(
                    item.body.toString(Charsets.UTF_8),
                    MessageId(item.messageId)
                )
                val payload = OutgoingPayload(envelope, item.destination)
                connectionManager.send(payload)
                outboxQueue.remove(item.id)
            } catch (e: Exception) {
                outboxQueue.incrementAttempt(item.id, e.message ?: "Unknown error")

                // attempt بعدی برای backoff
                val nextAttempt = item.attempt + 1
                val delayMs = calculateBackoff(nextAttempt, retryPolicy)
                delay(delayMs)
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
