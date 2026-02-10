package com.msa.chatlab.core.domain.lab

import com.msa.chatlab.core.domain.value.TimestampMillis

sealed interface RunEvent {
    val t: TimestampMillis

    data class Connected(override val t: TimestampMillis) : RunEvent
    data class Disconnected(override val t: TimestampMillis, val reason: String) : RunEvent

    data class Sent(override val t: TimestampMillis, val messageId: String) : RunEvent
    data class Received(override val t: TimestampMillis, val messageId: String) : RunEvent

    data class Failed(
        override val t: TimestampMillis,
        val messageId: String?,
        val error: String
    ) : RunEvent
}

// اگر SessionExporter timestampMs می‌خواد:
val RunEvent.timestampMs: Long get() = t.value
