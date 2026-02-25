package com.msa.chatlab.di

import com.msa.chatlab.AppLifecycleObserver
import com.msa.chatlab.core.data.ack.AckTracker
import com.msa.chatlab.core.data.ack.RoomAckTracker
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
import com.msa.chatlab.core.data.active.ActiveProfileStore

val DataModule = module {
    includes(CoreModule)

    // codec + repositories
    single { ProfileJsonCodec() }
    single { WirePayloadCodec() }

    single<ProfileRepository> { RoomProfileRepository(get(), get()) }
    single<MessageRepository> { RoomMessageRepository(get()) }

    // active profile
    single<ActiveProfileStore> {
        DataStoreActiveProfileStore(
            context = androidContext(),
            profileRepository = get(),
            appScope = get(),
            dispatchers = get(),
        )
    }
    single { ProfileManager(get(), get()) }

    // protocols & connection
    single { ProtocolRegistry(getAll()) }
    single { ProtocolResolver(get(), get()) }
    single {
        ConnectionManager(
            appScope = get(),
            activeProfileStore = get(),
            resolver = get(),
            logger = get(),
            crash = get()
        )
    }
    single { ConnectionLogStore() }
    single {
        ConnectionLogBinder(
            connectionManager = get(),
            logStore = get(),
            appScope = get()
        ).apply { start() }
    }

    // outbox
    single<OutboxQueue> { RoomOutboxQueue(get()) }
    single<AckTracker> { RoomAckTracker(get()) }
    single {
        OutboxProcessor(
            outboxQueue = get(),
            connectionManager = get(),
            activeProfileStore = get(),
            messageDao = get(),
            wireCodec = get(),
            ackTracker = get(),
            telemetryLogger = get()
        )
    }
    single { MessageSender(get(), get(), get(), get()) }
    single {
        TransportMessageBinder(
            activeProfileStore = get(),
            connectionManager = get(),
            messageDao = get(),
            ackTracker = get(),
            dedupStore = get(),
            telemetryLogger = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        ).apply { start() }
    }

    // misc
    single<DeviceInfoProvider> { AndroidDeviceInfoProvider(androidContext()) }
    single<DedupStore> { RoomDedupStore(get()) }
    single { TelemetryLogger() }

    single {
        AppLifecycleObserver(
            outboxProcessor = get(),
            telemetryLogger = get()
        )
    }
    single {
        ScenarioExecutor(
            activeProfileStore = get(),
            connectionManager = get(),
            messageSender = get(),
            deviceInfo = get(),
        )
    }
    single {
        SessionExporter(
            context = androidContext(),
            runDao = get(),
            runEventDao = get()
        )
    }
}
