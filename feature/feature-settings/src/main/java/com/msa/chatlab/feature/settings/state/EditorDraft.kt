package com.msa.chatlab.feature.settings.state

import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.value.ProfileId
import java.util.UUID

data class EditorDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val tagsCsv: String = "",

    val protocolType: ProtocolType = ProtocolType.WS_OKHTTP,

    // common
    val endpoint: String = "wss://echo.websocket.events",
    val headersText: String = "", // "Key:Value\nK2:V2"

    // ws-okhttp only (فعلاً تو editor)
    val wsPingIntervalMs: Long = 15_000
) {
    fun profileId(): ProfileId = ProfileId(id)
}
