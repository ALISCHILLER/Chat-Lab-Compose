package com.msa.chatlab.protocol.websocket.okhttp.config

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.WsOkHttpConfig
import okhttp3.Request

object WsOkHttpConfigAdapter {

    fun requireConfig(profile: Profile): WsOkHttpConfig {
        val cfg = profile.transportConfig
        require(cfg is WsOkHttpConfig) {
            "Profile ${profile.id.value} transportConfig is not WsOkHttpConfig"
        }
        return cfg
    }

    fun buildRequest(profile: Profile): Request {
        val cfg = requireConfig(profile)
        val builder = Request.Builder().url(cfg.endpoint)

        // headers
        cfg.headers.forEach { (k, v) ->
            builder.addHeader(k, v)
        }

        // می‌تونی اگر لازم داشتی User-Agent یا چیزی اضافه کنی
        return builder.build()
    }
}
