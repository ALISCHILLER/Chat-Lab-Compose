package com.msa.chatlab.bootstrap

import android.content.Context
import com.msa.chatlab.core.data.manager.ProfileManager
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.core.data.active.ActiveProfileStore

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

        // اگر پروفایل فعال وجود نداشت، اولین پروفایل لیست را فعال کن
        val firstProfile = profileManager.getProfiles().firstOrNull()
        if (firstProfile != null) {
            activeStore.setActive(firstProfile)
        } else {
            // اگر هیچ پروفایلی وجود نداشت، یک নোটিশ برای ساخت پروفایل دیفالت نشان بده
            noticeStore.setShouldShowProfileCreationNotice(true)
        }
    }
}
