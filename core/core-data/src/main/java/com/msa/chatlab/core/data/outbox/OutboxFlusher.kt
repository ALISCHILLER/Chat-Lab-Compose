package com.msa.chatlab.core.data.outbox

import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.min

class OutboxFlusher(
    private val connectionManager: ConnectionManager,
    private val sender: MessageSender,
    private val outbox: OutboxQueue,
    private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return

        job = scope.launch(Dispatchers.Default) {
            // وقتی Connected شد و simulateOffline خاموش بود → flush loop
            combine(
                connectionManager.connectionState,
                sender.simulateOffline,
                outbox.observe()
            ) { conn, simOffline, items ->
                Triple(conn, simOffline, items.size)
            }
                .distinctUntilChanged()
                .collect { (conn, simOffline, _) ->
                    if (conn is ConnectionState.Connected && !simOffline) {
                        flushAll()
                    }
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun flushAll() {
        // اگر همزمان چند بار trigger شد، مشکلی نیست چون loop همیشه بر اساس queue جلو می‌ره
        while (true) {
            // اگر وسط کار disconnect شد یا simOffline روشن شد → stop flush
            if (!connectionManager.isConnectedNow()) return
            if (sender.simulateOffline.value) return

            val item = outbox.peekFirst() ?: return

            outbox.incrementAttempt(item.id)

            val attempt = outbox.snapshot().firstOrNull { it.id == item.id }?.attempt ?: 1

            val backoffMs = computeBackoffMs(attempt)
            // تلاش اول سریع، بعدش backoff
            if (attempt > 1) delay(backoffMs)

            val ok = runCatching {
                sender.forceSend(item.text)
                true
            }.getOrElse { false }

            if (ok) {
                outbox.remove(item.id)
            } else {
                // اگر fail شد، آیتم رو نگه می‌داریم و بعداً دوباره در حلقه تلاش می‌شه
                // برای جلوگیری از loop داغ:
                delay(300)
            }
        }
    }

    private fun computeBackoffMs(attempt: Int): Long {
        // 1: 0ms
        // 2: 500ms
        // 3: 1000ms
        // 4: 2000ms
        // ...
        val base = 500L
        val pow = 1 shl min(attempt - 2, 6) // cap
        return base * pow
    }
}
