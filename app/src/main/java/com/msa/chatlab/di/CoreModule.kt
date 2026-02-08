package com.msa.chatlab.di

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.data.outbox.InMemoryOutboxQueue
import com.msa.chatlab.core.data.outbox.OutboxProcessor
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val CoreModule = module {

    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    single { ActiveProfileStore() }
    single { ProfileManager() }
    single { ProfileJsonCodec() }

    single { ProtocolRegistry() }

    single<OutboxQueue> { InMemoryOutboxQueue() }

    single {
        ConnectionManager(
            scope = get(),
            activeProfileStore = get(),
            registry = get()
        )
    }

    single { MessageSender(connectionManager = get(), outbox = get()) }

    single {
        OutboxProcessor(
            scope = get(),
            queue = get(),
            connectionManager = get()
        )
    }
}
