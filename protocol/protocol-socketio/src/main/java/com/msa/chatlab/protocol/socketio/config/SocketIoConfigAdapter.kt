package com.msa.chatlab.protocol.socketio.config

import com.msa.chatlab.core.domain.model.SocketIoConfig
import io.socket.client.IO
import io.socket.client.Socket

object SocketIoConfigAdapter {
    fun SocketIoConfig.toSocketOptions(): IO.Options {
        val options = IO.Options()
        options.forceNew = true
        options.reconnection = false // Reconnection handled by ConnectionManager
        options.path = connectPath ?: "/socket.io/"
        return options
    }
}
