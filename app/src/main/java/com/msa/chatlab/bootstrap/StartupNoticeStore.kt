package com.msa.chatlab.bootstrap

import android.content.Context

class StartupNoticeStore(context: Context) {
    private val prefs = context.getSharedPreferences("chatlab_startup_notice", Context.MODE_PRIVATE)

    fun set(message: String) {
        prefs.edit().putString("notice", message).apply()
    }

    fun consume(): String? {
        val msg = prefs.getString("notice", null) ?: return null
        prefs.edit().remove("notice").apply()
        return msg
    }
}
