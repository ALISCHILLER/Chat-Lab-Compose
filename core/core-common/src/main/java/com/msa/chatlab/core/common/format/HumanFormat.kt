package com.msa.chatlab.core.common.format

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HumanFormat {

    fun number(value: Long, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getInstance(locale).format(value)

    fun bytes(value: Long, locale: Locale = Locale.getDefault()): String {
        val abs = kotlin.math.abs(value.toDouble())
        val (scaled, unit) = when {
            abs < 1024 -> value.toDouble() to "B"
            abs < 1024.0 * 1024 -> value / 1024.0 to "KB"
            abs < 1024.0 * 1024 * 1024 -> value / (1024.0 * 1024) to "MB"
            else -> value / (1024.0 * 1024 * 1024) to "GB"
        }
        val nf = NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 1 }
        return "${nf.format(scaled)} $unit"
    }

    fun timeMillis(epochMillis: Long, locale: Locale = Locale.getDefault()): String {
        val df = SimpleDateFormat("HH:mm", locale)
        return df.format(Date(epochMillis))
    }
}
