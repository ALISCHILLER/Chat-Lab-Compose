package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.domain.model.*
import com.msa.chatlab.core.domain.rules.ProfileValidator
import com.msa.chatlab.core.domain.rules.ValidationResult
import com.msa.chatlab.core.domain.value.ProfileId
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import com.msa.chatlab.core.data.active.ActiveProfileStore

class ProfileManager(
    private val repo: ProfileRepository,
    val activeStore: ActiveProfileStore
) {
    fun observeProfiles(): Flow<List<Profile>> = repo.observeAll()

    suspend fun getProfiles(): List<Profile> = repo.getAll()

    suspend fun getProfile(id: ProfileId): Profile? = repo.getById(id)

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

    suspend fun upsert(profile: Profile): ValidationResult {
        val validation = validate(profile)
        if (validation.isValid) {
            repo.upsert(profile)
        }
        return validation
    }

    suspend fun delete(id: ProfileId) {
        repo.deleteById(id)
        if (activeStore.activeProfile.value?.id == id) {
            activeStore.clear()
        }
    }
}
