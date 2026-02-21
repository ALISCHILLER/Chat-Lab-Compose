package com.msa.chatlab.core.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

data class ConversationRow(
    val destination: String,
    val lastAt: Long,
    val lastText: String?,
    val lastStatus: String?,
    val total: Int
)

@Entity(
    tableName = "messages",
    primaryKeys = ["profile_id", "message_id"],
    indices = [
        Index(value = ["profile_id", "created_at"]),
        Index(value = ["profile_id", "destination", "created_at"])
    ]
)
data class MessageEntity(
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "message_id") val messageId: String,

    // "OUT" / "IN"
    @ColumnInfo(name = "direction") val direction: String,

    // for OUT = destination (topic/url/channel/whatever) — for IN, you can also store the active destination
    @ColumnInfo(name = "destination") val destination: String = "default",

    // for IN = source (if transport provides it)
    @ColumnInfo(name = "source") val source: String? = null,

    @ColumnInfo(name = "content_type") val contentType: String = "text/plain",
    @ColumnInfo(name = "headers_json") val headersJson: String = "{}", // for professionalism

    // for now for chat UI (later we can add raw body too)
    @ColumnInfo(name = "text") val text: String,

    @ColumnInfo(name = "created_at") val createdAt: Long,

    // "SENDING" | "QUEUED" | "SENT" | "FAILED" | "RECEIVED"
    @ColumnInfo(name = "status") val status: String = "SENT",

    @ColumnInfo(name = "queued") val queued: Boolean = false,
    @ColumnInfo(name = "attempt") val attempt: Int = 0,

    @ColumnInfo(name = "last_error") val lastError: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = createdAt
)
