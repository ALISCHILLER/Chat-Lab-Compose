package com.msa.chatlab.di

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.active.InMemoryActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.lab.DeviceInfoProvider
import com.msa.chatlab.core.data.lab.ScenarioExecutor
import com.msa.chatlab.core.data.lab.SessionExporter
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.data.outbox.OutboxProcessor
import com.msa.chatlab.core.data.outbox.RoomOutboxQueue
import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.data.repository.RoomProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val DataModule = module {
    single<ActiveProfileStore> { InMemoryActiveProfileStore() }
    single { ProfileJsonCodec() }

    single<ProfileRepository> { RoomProfileRepository(get(), get()) }
    single { ProfileManager(get(), get()) }

    single { ProtocolResolver(get(), get()) }
    single { RoomOutboxQueue(get()) }
    single { MessageSender(get(), get()) }
    single { ConnectionManager(get()) }
    single { OutboxProcessor(get(), get(), get()) }

    single<DeviceInfoProvider> {
        object : DeviceInfoProvider {
            override fun deviceModel(): String = android.os.Build.MODEL ?: "unknown"
            override fun osVersion(): String = android.os.Build.VERSION.RELEASE ?: "unknown"
            override fun networkLabel(): String = "unknown"
        }
    }

    single { ScenarioExecutor(CoroutineScope(SupervisorJob() + Dispatchers.IO), get(), get(), get(), get()) }
    single { SessionExporter(get(), get()) }
}
