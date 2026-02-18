package com.msa.chatlab.core.common.i18n

import androidx.core.text.BidiFormatter
import java.util.Locale

/**
 * For correctly displaying English strings (URLs, Endpoints, IDs) within RTL UI.
 */
object BidiText {
    fun wrap(text: String, locale: Locale = Locale.getDefault()): String {
        return BidiFormatter.getInstance(locale).unicodeWrap(text)
    }
}
