package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.model.MessageStatus
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.entity.MessageEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TransportMessageBinder(
    private val activeProfileStore: ActiveProfileStore,
    private val connectionManager: ConnectionManager,
    private val messageDao: MessageDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch {
            connectionManager.events.collectLatest { ev ->
                when (ev) {
                    is TransportEvent.MessageReceived -> {
                        val profile = activeProfileStore.getActiveNow() ?: return@collectLatest
                        val env = ev.payload.envelope

                        val text = if (env.contentType.startsWith("text/")) env.body.decodeToString()
                        else "<${env.contentType} • ${env.body.size} bytes>"

                        val destinationKey = when (ev.payload.source) {
                            null, "ws" -> "default"
                            else -> ev.payload.source
                        }

                        // Explicit type declaration to help the compiler
                        val eventSource: String? = ev.payload.source

                        messageDao.upsert(
                            MessageEntity(
                                profileId = profile.id.value,
                                messageId = env.messageId.value,
                                direction = "IN",
                                destination = destinationKey,
                                source = eventSource,
                                contentType = env.contentType,
                                headersJson = "{}",
                                text = text,
                                createdAt = env.createdAt.value,
                                status = MessageStatus.Delivered.name,
                                queued = false,
                                attempt = 0,
                                lastError = null,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }

                    is TransportEvent.MessageSent -> {
                        messageDao.updateStatusByMessageId(
                            messageId = ev.messageId,
                            status = MessageStatus.Sent.name,
                            lastError = null,
                            updatedAt = System.currentTimeMillis()
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}