package com.msa.chatlab.core.domain.model

data class PayloadProfile(
    val codec: CodecMode = CodecMode.StandardEnvelope,
    val targetSizeBytes: Int = 1024,
    val pattern: PayloadPattern = PayloadPattern.Text,
    val seed: Long = 42
)

enum class CodecMode {
    PlainText,
    StandardEnvelope
}

enum class PayloadPattern {
    Text,
    JsonLike,
    RandomBytesBase64
}
