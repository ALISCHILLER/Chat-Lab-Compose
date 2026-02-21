package com.msa.chatlab.bootstrap

import android.content.Context
import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.domain.value.ProfileId

class StartupBootstrapper(
    private val context: Context,
    private val profileManager: ProfileManager,
    private val activeStore: ActiveProfileStore,
    private val noticeStore: StartupNoticeStore
) {
    suspend fun ensureActiveProfile() {
        // ⚠️ جلوگیری از race با PersistentActiveProfileStore:
        // اگر active_profile_id قبلاً set شده، اول همون رو try کن
        val activePrefs = context.getSharedPreferences("chatlab_active_profile", Context.MODE_PRIVATE)
        val savedId = activePrefs.getString("active_profile_id", null)

        if (savedId != null) {
            val p = profileManager.getProfile(ProfileId(savedId))
            if (p != null) {
                // مطمئن شو activeStore هم همگام است (در صورت دیر load شدن)
                if (activeStore.getActiveNow() == null) activeStore.setActive(p)
                return
            } else {
                // اگر id خراب بود
                activePrefs.edit().remove("active_profile_id").apply()
            }
        }

        // اگر هنوز active نداریم:
        if (activeStore.getActiveNow() != null) return

        val profiles = profileManager.getProfiles()
        val activated = if (profiles.isNotEmpty()) {
            profiles.first().also { profileManager.setActive(it) }
        } else {
            val p = profileManager.createDefaultWsOkHttpProfile(
                name = "WS-OkHttp Default",
                endpoint = "wss://echo.websocket.events"
            )
            profileManager.setActive(p)
            p
        }

        noticeStore.set("Active profile: ${activated.name} • ${activated.transportConfig.endpoint}")
    }
}
