package com.msa.chatlab.di

import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.pipeline.EnvelopeFactory
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import com.msa.chatlab.core.data.registry.ProtocolResolver
import org.koin.dsl.module

val ConnectionModule = module {

    // Protocol bindings توسط protocol-* ثبت می‌شوند:
    // factory<List<ProtocolBinding>> (Koin خودش لیست را inject می‌کند)
    single { ProtocolRegistry(bindings = get()) }

    single { ProtocolResolver(activeProfileStore = get(), registry = get()) }

    single { EnvelopeFactory() }

    single { ConnectionManager(resolver = get()) }

    single { MessageSender(connectionManager = get(), envelopeFactory = get()) }
}
