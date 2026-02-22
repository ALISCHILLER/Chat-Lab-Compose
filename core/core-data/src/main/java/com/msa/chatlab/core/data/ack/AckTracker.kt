package com.msa.chatlab.core.data.ack

interface AckTracker {
    fun onAck(messageId: String)
    suspend fun awaitAck(messageId: String, timeoutMs: Long): Boolean
    fun prune(olderThanMs: Long)
}