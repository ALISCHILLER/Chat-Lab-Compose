package com.msa.chatlab.core.data.active

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.value.ProfileId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "active_profile")

class DataStoreActiveProfileStore(
    private val context: Context,
    private val profileManager: Lazy<ProfileManager> // Use Lazy to break the dependency cycle
) : ActiveProfileStore {

    private val _activeProfile = MutableStateFlow<Profile?>(null)
    override val activeProfile: StateFlow<Profile?> = _activeProfile.asStateFlow()

    init {
        // On init, read from datastore and set the active profile
        CoroutineScope(Dispatchers.IO).launch {
            val activeProfileId = getActiveProfileId()
            if (activeProfileId != null) {
                val profile = profileManager.value.getProfile(activeProfileId)
                _activeProfile.value = profile
            }
        }
    }

    private suspend fun getActiveProfileId(): ProfileId? {
        val preferences = context.dataStore.data.first()
        val profileId = preferences[KEY_ACTIVE_PROFILE_ID]
        return profileId?.let { ProfileId(it) }
    }

    override fun getActiveNow(): Profile? {
        return activeProfile.value
    }

    override fun setActive(profile: Profile?) {
        _activeProfile.value = profile
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit {
                if (profile != null) {
                    it[KEY_ACTIVE_PROFILE_ID] = profile.id.value
                } else {
                    it.remove(KEY_ACTIVE_PROFILE_ID)
                }
            }
        }
    }

    override fun clear() {
        setActive(null)
    }

    companion object {
        private val KEY_ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
    }
}
