package com.msa.chatlab.core.storage.di

import androidx.room.Room
import com.msa.chatlab.core.storage.ChatLabDatabase
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.dao.ProfileDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val StorageModule = module {
    single<ChatLabDatabase> {
        Room.databaseBuilder(
            androidContext(),
            ChatLabDatabase::class.java,
            "chatlab-db"
        ).build()
    }

    single<MessageDao> {
        get<ChatLabDatabase>().messageDao()
    }

    single<OutboxDao> {
        get<ChatLabDatabase>().outboxDao()
    }

    single<ProfileDao> {
        get<ChatLabDatabase>().profileDao()
    }
}
