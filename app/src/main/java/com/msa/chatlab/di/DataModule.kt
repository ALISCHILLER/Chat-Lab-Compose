package com.msa.chatlab.di

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.active.PersistentActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.codec.StandardEnvelopeCodec
import com.msa.chatlab.core.data.codec.WirePayloadCodec
import com.msa.chatlab.core.data.lab.DeviceInfoProvider
import com.msa.chatlab.core.data.lab.ScenarioExecutor
import com.msa.chatlab.core.data.lab.SessionExporter
import com.msa.chatlab.core.data.manager.*
import com.msa.chatlab.core.data.outbox.OutboxProcessor
import com.msa.chatlab.core.data.outbox.OutboxQueue
import com.msa.chatlab.core.data.outbox.RoomOutboxQueue
import com.msa.chatlab.core.data.registry.ProtocolRegistry
import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.data.repository.RoomMessageRepository
import com.msa.chatlab.core.data.repository.RoomProfileRepository
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.nativebridge.device.AndroidDeviceInfoProvider
import com.msa.chatlab.core.observability.crash.CrashReporter
import com.msa.chatlab.core.observability.log.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    single { ConnectionLogBinder(get(), get(), CoroutineScope(SupervisorJob() + Dispatchers.IO)) }

    single { ConnectionManager(get(), get(), get(), get()) } // ActiveProfileStore, ProtocolResolver, AppLogger, CrashReporter

    // outbox
    single<OutboxQueue> { RoomOutboxQueue(get()) }
    single { OutboxProcessor(get(), get(), get(), get(), get()) } // Added wireCodec

    // sender
    single { MessageSender(get(), get(), get(), get()) }

    // message binder
    single { TransportMessageBinder(get(), get(), get()) }

    // lab dependencies
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider(androidContext()) }
    single { SessionExporter(get(), get()) }
    single {
        ScenarioExecutor(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            activeProfileStore = get(),
            connectionManager = get(),
            messageSender = get(),
            deviceInfo = get()
        )
    }

    // payload codec helpers
    single { WirePayloadCodec() }
}