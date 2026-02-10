package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.domain.model.ProtocolType

data class Scenario(
    val preset: Preset = Preset.Stable,
    val durationMs: Long = 60_000,
    val messageRatePerSecond: Double = 3.0,
    val burstSize: Int = 0,
    val seed: Long = 42,
    val burstEvery: Int = 1,
    val dropRatePercent: Int = 0,
    val minExtraDelayMs: Long = 0L,
    val maxExtraDelayMs: Long = 0L,
    val disconnects: List<DisconnectWindow> = emptyList()
) {
    enum class Preset {
        Stable,          // بدون قطعی، نرخ ثابت
        Intermittent,    // قطعی‌های کوتاه و مکرر
        OfflineBurst,    // ۵ دقیقه آفلاین + ارسال انبوه
        Lossy,           // شبکه نامطمئن با بسته‌های گم‌شده
        LoadBurst        // تست تحمل با ترافیک بالا
    }

    data class DisconnectWindow(
        val atMsFromStart: Long,
        val durationMs: Long
    )
}

fun Scenario.defaultFor(preset: Scenario.Preset): Scenario = when (preset) {
    Scenario.Preset.Stable -> Scenario(
        preset = Scenario.Preset.Stable,
        durationMs = 60_000,
        messageRatePerSecond = 3.0,
        seed = System.currentTimeMillis()
    )
    Scenario.Preset.Intermittent -> Scenario(
        preset = Scenario.Preset.Intermittent,
        durationMs = 120_000,
        messageRatePerSecond = 2.0,
        disconnects = listOf(
            Scenario.DisconnectWindow(30_000, 5_000),
            Scenario.DisconnectWindow(60_000, 5_000),
            Scenario.DisconnectWindow(90_000, 5_000)
        ),
        seed = System.currentTimeMillis()
    )
    Scenario.Preset.OfflineBurst -> Scenario(
        preset = Scenario.Preset.OfflineBurst,
        durationMs = 300_000,
        messageRatePerSecond = 1.0,
        disconnects = listOf(
            Scenario.DisconnectWindow(0, 300_000) // ۵ دقیقه آفلاین
        ),
        seed = System.currentTimeMillis()
    )
    Scenario.Preset.Lossy -> Scenario(
        preset = Scenario.Preset.Lossy,
        durationMs = 90_000,
        messageRatePerSecond = 5.0,
        dropRatePercent = 10, // 10% packet loss
        minExtraDelayMs = 50,
        maxExtraDelayMs = 500,
        seed = System.currentTimeMillis()
    )
    Scenario.Preset.LoadBurst -> Scenario(
        preset = Scenario.Preset.LoadBurst,
        durationMs = 30_000,
        messageRatePerSecond = 50.0, // ۵۰ پیام در ثانیه
        burstEvery = 1000,
        burstSize = 100,
        seed = System.currentTimeMillis()
    )
}
