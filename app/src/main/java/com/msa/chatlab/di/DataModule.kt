package com.msa.chatlab.di

import com.msa.chatlab.AppLifecycleObserver
import com.msa.chatlab.core.data.ack.AckTracker
import com.msa.chatlab.core.data.ack.RoomAckTracker
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.active.DataStoreActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.codec.WirePayloadCodec
import com.msa.chatlab.core.data.dedup.DedupStore
import com.msa.chatlab.core.data.dedup.RoomDedupStore
import com.msa.chatlab.core.domain.model.device.DeviceInfoProvider
import com.msa.chatlab.core.data.lab.ScenarioExecutor
import com.msa.chatlab.core.data.lab.SessionExporter
import com.msa.chatlab.core.data.manager.ConnectionLogBinder
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
import com.msa.chatlab.core.data.telemetry.TelemetryLogger
import com.msa.chatlab.core.domain.repository.MessageRepository
import com.msa.chatlab.core.nativebridge.device.AndroidDeviceInfoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DataModule = module {
    includes(CoreModule)

    // codec + repositories
    single { ProfileJsonCodec() }
    single { WirePayloadCodec() }

    single<ProfileRepository> { RoomProfileRepository(get(), get()) }
    single<MessageRepository> { RoomMessageRepository(get()) }

    // active profile
    single<ActiveProfileStore> { DataStoreActiveProfileStore(androidContext(), get()) }
    single { ProfileManager(get(), get()) }

    // protocols & connection
    single { ProtocolRegistry(getAll()) }
    single { ProtocolResolver(get(), get()) }

    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { ConnectionLogStore() }
    single { ConnectionLogBinder(get(), get(), get()) }

    single { ConnectionManager(get(), get(), get(), get(), get()) }

    // outbox
    single<OutboxQueue> { RoomOutboxQueue(get()) }
    single<AckTracker> { RoomAckTracker(get()) }
    single<DedupStore> { RoomDedupStore(get()) }
    single { OutboxProcessor(get(), get(), get(), get(), get(), get(), get()) }

    // sender
    single { MessageSender(get(), get(), get(), get()) }

    // message binder
    single { TransportMessageBinder(get(), get(), get(), get(), get(), get(), get()) }



    // lab dependencies
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider(androidContext()) }
    single { SessionExporter(get(), get()) }
    single { TelemetryLogger(get()) }

    single {
        ScenarioExecutor(
            activeProfileStore = get(),
            connectionManager = get(),
            messageSender = get(),
            deviceInfo = get()
        )
    }

    // lifecycle observer
    single { AppLifecycleObserver(get(), get(), get()) }
}
