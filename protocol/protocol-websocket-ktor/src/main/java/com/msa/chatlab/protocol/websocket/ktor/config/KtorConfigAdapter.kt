package com.msa.chatlab.protocol.websocket.ktor.config

import com.msa.chatlab.core.domain.model.WsKtorConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*

object KtorConfigAdapter {
    fun WsKtorConfig.toHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = pingIntervalMs
            }
        }
    }
}
