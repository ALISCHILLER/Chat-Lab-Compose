package com.msa.chatlab

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.msa.chatlab.core.data.manager.ConnectionLogBinder
import com.msa.chatlab.core.data.manager.TransportMessageBinder
import com.msa.chatlab.core.data.outbox.OutboxProcessor

class AppLifecycleObserver(
    private val connectionLogBinder: ConnectionLogBinder,
    private val transportMessageBinder: TransportMessageBinder,
    private val outboxProcessor: OutboxProcessor
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        connectionLogBinder.start()
        transportMessageBinder.start()
        outboxProcessor.start()
    }
}
