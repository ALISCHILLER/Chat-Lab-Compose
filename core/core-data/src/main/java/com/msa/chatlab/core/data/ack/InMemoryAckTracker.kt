package com.msa.chatlab.core.data.ack

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

class InMemoryAckTracker : AckTracker {

    private val waiters = ConcurrentHashMap<String, CompletableDeferred<Unit>>()
    private val ackedAt = ConcurrentHashMap<String, Long>()

    override fun onAck(messageId: String) {
        ackedAt[messageId] = System.currentTimeMillis()
        waiters.remove(messageId)?.complete(Unit)
    }

    override suspend fun awaitAck(messageId: String, timeoutMs: Long): Boolean {
        // ack already received?
        if (ackedAt.containsKey(messageId)) return true

        val d = waiters.computeIfAbsent(messageId) { CompletableDeferred() }
        val ok = withTimeoutOrNull(timeoutMs) { d.await(); true } ?: false
        return ok || ackedAt.containsKey(messageId)
    }

    override fun prune(olderThanMs: Long) {
        val now = System.currentTimeMillis()
        ackedAt.entries.removeIf { (_, ts) -> (now - ts) > olderThanMs }
    }
}