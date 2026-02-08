package com.msa.chatlab.di

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.active.InMemoryActiveProfileStore
import com.msa.chatlab.core.data.codec.ProfileJsonCodec
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.data.repository.RoomProfileRepository
import org.koin.dsl.module

val DataModule = module {

    single { ProfileJsonCodec() }

    single<ActiveProfileStore> { InMemoryActiveProfileStore() }

    single<ProfileRepository> {
        RoomProfileRepository(
            dao = get(),          // ProfileDao از StorageModule
            codec = get()
        )
    }

    single {
        ProfileManager(
            repo = get(),
            activeStore = get()
        )
    }
}
