package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OutboxProcessor(
    private val scope: CoroutineScope,
    private val queue: OutboxQueue,
    private val connectionManager: ConnectionManager,
    private val retryConfig: RetryConfig = RetryConfig()
) {

    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch {
            connectionManager.connectionState.collect { state ->
                if (state is ConnectionState.Connected) {
                    flushLoop()
                }
            }
        }
    }

    suspend fun flushOnce(): Boolean {
        val item = queue.peekOldest() ?: return false
        return runCatching {
            connectionManager.sendViaPreparedTransport(item.payload)
        }.onSuccess {
            queue.remove(item.id)
        }.onFailure { ex ->
            val updated = item.copy(
                attempt = item.attempt + 1,
                lastError = ex.message ?: "send failed"
            )
            queue.update(updated)

            if (RetryPolicyEngine.canRetry(item.attempt, retryConfig)) {
                delay(RetryPolicyEngine.nextDelayMs(item.attempt, retryConfig))
            }
        }.isSuccess
    }

    private suspend fun flushLoop() {
        // تا وقتی وصل هستیم و صف خالی نشده
        while (connectionManager.isConnectedNow()) {
            val had = queue.peekOldest() ?: break
            flushOnce()
            // اگر هنوز همون item مونده (مثلاً maxAttempts تمام شده)، از loop بیرون میایم تا اسپم نشه
            val again = queue.peekOldest()
            if (again != null && again.id == had.id && !RetryPolicyEngine.canRetry(again.attempt, retryConfig)) break
        }
    }
}
