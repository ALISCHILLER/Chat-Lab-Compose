package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.ack.AckTracker
import com.msa.chatlab.core.data.dedup.DedupStore
import com.msa.chatlab.core.data.telemetry.TelemetryHeaders
import com.msa.chatlab.core.data.telemetry.TelemetryLogger
import com.msa.chatlab.core.data.util.HeaderJson
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.msa.chatlab.core.data.active.ActiveProfileStore

class TransportMessageBinder(
    private val activeProfileStore: ActiveProfileStore,
    private val connectionManager: ConnectionManager,
    private val messageDao: MessageDao,
    private val ackTracker: AckTracker,
    private val dedupStore: DedupStore,
    private val telemetryLogger: TelemetryLogger,
    private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch {
            connectionManager.events.collectLatest { ev ->
                when (ev) {
                    is TransportEvent.MessageReceived -> handleMessageReceived(ev)
                    is TransportEvent.MessageSent -> handleMessageSent(ev)
                    else -> Unit
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun handleMessageReceived(ev: TransportEvent.MessageReceived) {
        val profile = activeProfileStore.getActiveNow() ?: return
        val envelope = ev.payload.envelope

        if (dedupStore.isDuplicate(profile.id.value, envelope.messageId.value)) return

        // persist IN message
        val entity = MessageEntity(
            profileId = profile.id.value,
            messageId = envelope.messageId.value,
            isOutgoing = false,
            destination = "local", // or parse from payload if available
            status = MessageStatus.Received.name,
            text = envelope.body.decodeToString(), // assuming text
            timestamp = envelope.createdAt.value,
            headers = HeaderJson.encode(envelope.headers)
        )
        messageDao.insert(entity)

        telemetryLogger.onMessageReceived(envelope.headers)
    }

    private suspend fun handleMessageSent(ev: TransportEvent.MessageSent) {
        val profileId = activeProfileStore.getActiveNow()?.id?.value ?: return

        ackTracker.onMessageSent(profileId, ev.messageId)
    }
}
