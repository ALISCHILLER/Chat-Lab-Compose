package com.msa.chatlab.core.data.ack

import com.msa.chatlab.core.storage.dao.AckDao
import com.msa.chatlab.core.storage.entity.AckEntity
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class RoomAckTracker(private val ackDao: AckDao) : AckTracker {

    override fun onAck(messageId: String) {
        // This is intentionally left blank. The Ack is inserted into the database by another component.
    }

    override suspend fun awaitAck(messageId: String, timeoutMs: Long): Boolean {
        return withTimeoutOrNull(timeoutMs) {
            ackDao.getById(messageId).filterNotNull().first()
            true
        } ?: false
    }

    override fun prune(olderThanMs: Long) {
        // This is handled by a separate worker/job
    }
}
