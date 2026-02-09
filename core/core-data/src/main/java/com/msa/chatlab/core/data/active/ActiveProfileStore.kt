package com.msa.chatlab.core.data.active

import com.msa.chatlab.core.domain.model.Profile
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

    override fun setActive(profile: Profile?) {
        _active.value = profile
    }

    override fun clear() {
        _active.value = null
    }

    override fun getActiveNow(): Profile? = _active.value
}
