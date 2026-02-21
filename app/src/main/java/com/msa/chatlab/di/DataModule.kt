package com.msa.chatlab.di

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.active.PersistentActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ConnectionLogStore
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.data.manager.TransportMessageBinder
import com.msa.chatlab.core.data.outbox.OutboxProcessor
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.data.outbox.RoomOutboxQueue
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.data.repository.RoomMessageRepository
import com.msa.chatlab.core.data.repository.RoomProfileRepository
import com.msa.chatlab.core.domain.repository.MessageRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DataModule = module {

    // codec + repositories
    single { ProfileJsonCodec() }
    single<ProfileRepository> { RoomProfileRepository(get(), get()) }
    single<MessageRepository> { RoomMessageRepository(get()) }

    // active profile
    single<ActiveProfileStore> { PersistentActiveProfileStore(androidContext(), get()) }
    single { ProfileManager(get(), get()) }

    // protocols & connection
    single { ProtocolRegistry(getAll()) }
    single { ProtocolResolver(get(), get()) }
    single { ConnectionLogStore() }
    single { ConnectionManager(get(), get(), get()) }

    // outbox
    single<OutboxQueue> { RoomOutboxQueue(get()) }
    single { OutboxProcessor(get(), get(), get(), get()) }

    // sender
    single { MessageSender(get(), get(), get()) }

    // message binder (TransportEvent -> DB)
    single { TransportMessageBinder(get(), get(), get()) }
}
