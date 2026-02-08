package com.msa.chatlab.core.storage.util

/**
 * فاز ۱: فقط برای اینکه Storage مستقل باشد.
 * فاز ۲: این را با kotlinx.serialization یا moshi جایگزین می‌کنیم.
 */
object JsonCodec {
    fun encode(raw: String): String = raw
    fun decode(raw: String): String = raw
}
