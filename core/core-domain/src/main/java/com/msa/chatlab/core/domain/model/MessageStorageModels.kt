package com.msa.chatlab.core.domain.model

/**
 * Projection برای لیست گفتگوها (Chat list).
 * این POJO هیچ وابستگی به Room ندارد.
 */
data class ConversationRow(
    val destination: String,
    val lastAt: Long,
    val lastText: String?,
    val lastStatus: String?,
    val total: Int
)