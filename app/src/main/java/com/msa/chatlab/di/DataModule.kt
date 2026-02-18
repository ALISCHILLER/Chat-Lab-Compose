package com.msa.chatlab.di

import com.google.gson.Gson
import com.msa.chatlab.core.common.ui.messenger.ChannelUiMessenger
import com.msa.chatlab.core.common.ui.messenger.UiMessenger
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.active.PersistentActiveProfileStore
import com.msa.chatlab.core.data.lab.LabRunner
import com.msa.chatlab.core.data.lab.PresetRepository
import com.msa.chatlab.core.data.lab.RoomPresetRepository
import com.msa.chatlab.core.data.lab.RunBundleExporter
import com.msa.chatlab.core.data.lab.RunRecorder
import com.msa.chatlab.core.data.lab.RunRepository
import com.msa.chatlab.core.data.lab.RoomRunRepository
import com.msa.chatlab.core.data.manager.ConnectionManager
import com.msa.chatlab.core.data.manager.MessagePump
import com.msa.chatlab.core.data.manager.MessageSender
import com.msa.chatlab.core.data.message.MessageRepository
import com.msa.chatlab.core.data.message.RoomMessageRepository
import com.msa.chatlab.core.data.outbox.OutboxProcessor
import com.msa.chatlab.core.data.outbox.RoomOutboxQueue
import com.msa.chatlab.core.data.registry.ProtocolResolver
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.data.repository.RoomProfileRepository
import com.msa.chatlab.core.observability.AndroidLogcatLogger
import com.msa.chatlab.core.observability.AppLogger
import com.msa.chatlab.core.observability.CompositeLogger
import com.msa.chatlab.core.observability.CrashReporter
import com.msa.chatlab.core.observability.InMemoryLogStore
import com.msa.chatlab.core.observability.LogStore
import com.msa.chatlab.core.observability.StoreLogger
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DataModule = module {

    // UI messenger
    single<UiMessenger> { ChannelUiMessenger() }

    // logging
    single<LogStore> { InMemoryLogStore(maxEntries = 3000) }
    single<AppLogger> { CompositeLogger(AndroidLogcatLogger(), StoreLogger(get())) }
    single { CrashReporter(get()) }

    // repositories
    single<ProfileRepository> { RoomProfileRepository(get(), get()) }
    single<MessageRepository> { RoomMessageRepository(get()) }
    single<RunRepository> { RoomRunRepository(get(), get()) }
    single<PresetRepository> { RoomPresetRepository(get()) }

    // active profile store (persistent)
    single<ActiveProfileStore> { PersistentActiveProfileStore(androidContext(), get()) }

    // protocol resolver + connection
    single { ProtocolResolver(get(), get()) }
    single { ConnectionManager(get(), get()) }

    // outbox
    single { RoomOutboxQueue(get()) }

    // message pipeline
    single { MessageSender(get(), get(), get(), get(), get()) }
    single { MessagePump(get(), get(), get(), get()) }
    single { OutboxProcessor(get(), get(), get(), get(), get()) }

    // Lab
    single { RunRecorder(get(), get()) }
    single { LabRunner(get(), get(), get(), get()) }
    single { RunBundleExporter(get(), get(), get(), get()) }

    // misc
    single { Gson() }
}
