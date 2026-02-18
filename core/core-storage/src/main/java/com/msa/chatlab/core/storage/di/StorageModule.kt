package com.msa.chatlab.core.storage.di

import androidx.room.Room
import com.msa.chatlab.core.storage.dao.*
import com.msa.chatlab.core.storage.db.ChatLabDatabase
import com.msa.chatlab.core.storage.db.Migrations
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val StorageModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ChatLabDatabase::class.java,
            ChatLabDatabase.DATABASE_NAME
        )
            .addMigrations(Migrations.MIGRATION_1_2)
            .build()
    }

    single { get<ChatLabDatabase>().messageDao() }
    single { get<ChatLabDatabase>().outboxDao() }
    single { get<ChatLabDatabase>().profileDao() }
    single { get<ChatLabDatabase>().runDao() }
    single { get<ChatLabDatabase>().eventDao() }
    single { get<ChatLabDatabase>().presetDao() }
}
