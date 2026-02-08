package com.msa.chatlab.protocol.websocket.okhttp.transport

import com.msa.chatlab.core.protocol.api.contract.ConnectionState
import com.msa.chatlab.core.protocol.api.event.TransportEvent
import com.msa.chatlab.core.protocol.api.event.TransportStatsEvent
import com.msa.chatlab.protocol.websocket.okhttp.mapper.OkHttpErrorMapper
import com.msa.chatlab.protocol.websocket.okhttp.mapper.OkHttpEventMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WsOkHttpWebSocketListener(
    private val connectionState: MutableStateFlow<ConnectionState>,
    private val events: MutableSharedFlow<TransportEvent>,
    private val stats: MutableStateFlow<TransportStatsEvent>,
    private val now: () -> Long
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        connectionState.value = ConnectionState.Connected(System.currentTimeMillis())
        events.tryEmit(TransportEvent.Connected)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val payload = OkHttpEventMapper.incomingText(text, now())
        // آمار
        stats.value = stats.value.copy(bytesReceived = stats.value.bytesReceived + text.encodeToByteArray().size)
        events.tryEmit(TransportEvent.MessageReceived(payload))
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        val payload = OkHttpEventMapper.incomingBinary(bytes.toByteArray(), now())
        stats.value = stats.value.copy(bytesReceived = stats.value.bytesReceived + bytes.size)
        events.tryEmit(TransportEvent.MessageReceived(payload))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // OkHttp پیشنهاد می‌دهد close را بزنیم
        webSocket.close(code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        connectionState.value = ConnectionState.Disconnected(reason.ifBlank { null }, System.currentTimeMillis())
        events.tryEmit(TransportEvent.Disconnected(reason.ifBlank { null }))
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        val err = OkHttpErrorMapper.map(t)
        connectionState.value = ConnectionState.Disconnected(err.message, System.currentTimeMillis())
        events.tryEmit(TransportEvent.ErrorOccurred(err))
        events.tryEmit(TransportEvent.Disconnected(err.message))
    }
}
