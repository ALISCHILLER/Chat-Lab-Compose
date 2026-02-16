package com.msa.chatlab.di

import com.msa.chatlab.core.data.repository.ActiveProfileStore
import com.msa.chatlab.core.data.repository.ConnectionManager
import com.msa.chatlab.core.data.repository.MessageSender
import com.msa.chatlab.core.data.outbox.OutboxProcessor
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.data.repository.ProtocolResolver
import com.msa.chatlab.core.data.outbox.RoomOutboxQueue
import com.msa.chatlab.core.data.lab.ScenarioExecutor
import com.msa.chatlab.core.data.lab.SessionExporter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { ActiveProfileStore(get()) }
    single { ProtocolResolver(get()) }
    single { RoomOutboxQueue(get()) }
    single { OutboxProcessor(get(), get(), get()) } // Added ActiveProfileStore
    single { MessageSender(get(), get()) }
    single { ConnectionManager(get()) }
    single { ProfileRepository(get()) }
    single { ScenarioExecutor(get(), get(), get()) }
    single { SessionExporter(androidContext()) }
}
