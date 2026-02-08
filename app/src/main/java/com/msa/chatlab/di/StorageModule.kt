package com.msa.chatlab.di

import androidx.room.Room
import com.msa.chatlab.core.storage.db.ChatLabDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val StorageModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ChatLabDatabase::class.java,
            "chatlab.db"
        )
            .fallbackToDestructiveMigration() // فاز اول OK
            .build()
    }

    single { get<ChatLabDatabase>().profileDao() }
    single { get<ChatLabDatabase>().runDao() }
    single { get<ChatLabDatabase>().eventDao() }
}
