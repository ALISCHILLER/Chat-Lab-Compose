package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.domain.model.*
import com.msa.chatlab.core.domain.rules.ProfileValidator
import com.msa.chatlab.core.domain.rules.ValidationResult
import com.msa.chatlab.core.domain.value.ProfileId
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProfileManager(
    private val repo: ProfileRepository,
    val activeStore: ActiveProfileStore
) {
    fun observeProfiles(): Flow<List<Profile>> = repo.observeAll()

    suspend fun getProfiles(): List<Profile> = repo.getAll()

    suspend fun getProfile(id: ProfileId): Profile? = repo.getById(id.value)

    suspend fun search(query: String): List<Profile> = repo.search(query)

    fun validate(profile: Profile): ValidationResult = ProfileValidator.validate(profile)

    suspend fun createDefaultWsOkHttpProfile(
        name: String = "WS-OkHttp Default",
        endpoint: String = "wss://echo.websocket.events"
    ): Profile {
        val profile = Profile(
            id = ProfileId(UUID.randomUUID().toString()),
            name = name,
            protocolType = ProtocolType.WS_OKHTTP,
            transportConfig = WsOkHttpConfig(endpoint = endpoint),
        )
        repo.upsert(profile)
        return profile
    }

    suspend fun upsert(profile: Profile): Profile {
        val validation = validate(profile)
        require(validation.isValid) { "Profile invalid: ${validation.errors}" }
        return repo.upsert(profile)
    }

    suspend fun delete(id: ProfileId) {
        // اگر پروفایل حذف شد و active بود → clear
        val active = activeStore.activeProfile.value
        if (active?.id == id) activeStore.clear()
        repo.deleteById(id.value)
    }

    fun setActive(profile: Profile) {
        activeStore.setActive(profile)
    }

    fun clearActive() {
        activeStore.clear()
    }
}
