package com.msa.chatlab.di

import androidx.room.Room
import com.msa.chatlab.core.data.repository.MessageRepository
import com.msa.chatlab.core.storage.db.ChatLabDatabase
import com.msa.chatlab.core.storage.repository.RoomMessageRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val StorageModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            ChatLabDatabase::class.java,
            ChatLabDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // فقط برای فاز توسعه
            .build()
    }

    // DAOs
    single { get<ChatLabDatabase>().profileDao() }
    single { get<ChatLabDatabase>().runDao() }
    single { get<ChatLabDatabase>().eventDao() }
    single { get<ChatLabDatabase>().messageDao() }
    single { get<ChatLabDatabase>().outboxDao() }

    // Repositories
    single<MessageRepository> { RoomMessageRepository(get(), get()) }
}
