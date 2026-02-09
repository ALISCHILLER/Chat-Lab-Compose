package com.msa.chatlab.protocol.websocket.okhttp.config

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.domain.model.WsOkHttpConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

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

        cfg.headers.forEach { (k, v) ->
            builder.addHeader(k, v)
        }

        return builder.build()
    }
    
    fun WsOkHttpConfig.toOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .pingInterval(this.pingIntervalMs, TimeUnit.MILLISECONDS)
            .build()
    }
}
