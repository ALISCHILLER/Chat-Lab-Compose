package com.msa.chatlab.core.data.active

import android.content.Context
import com.msa.chatlab.core.data.repository.ProfileRepository
import com.msa.chatlab.core.domain.model.Profile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ActiveProfileStore {
    val activeProfile: StateFlow<Profile?>
    fun setActive(profile: Profile?)
    fun clear()
    fun getActiveNow(): Profile?
}

class InMemoryActiveProfileStore : ActiveProfileStore {
    private val _active = MutableStateFlow<Profile?>(null)
    override val activeProfile: StateFlow<Profile?> = _active.asStateFlow()
    override fun setActive(profile: Profile?) { _active.value = profile }
    override fun clear() { _active.value = null }
    override fun getActiveNow(): Profile? = _active.value
}

class PersistentActiveProfileStore(
    context: Context,
    private val repo: ProfileRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ActiveProfileStore {

    private val prefs = context.getSharedPreferences("chatlab_active_profile", Context.MODE_PRIVATE)
    private val _active = MutableStateFlow<Profile?>(null)
    override val activeProfile: StateFlow<Profile?> = _active.asStateFlow()

    init {
        val id = prefs.getString("active_profile_id", null)
        if (id != null) {
            scope.launch {
                _active.value = repo.getById(id)
            }
        }
    }

    override fun setActive(profile: Profile?) {
        _active.value = profile
        prefs.edit()
            .putString("active_profile_id", profile?.id?.value)
            .apply()
    }

    override fun clear() = setActive(null)
    override fun getActiveNow(): Profile? = _active.value
}
