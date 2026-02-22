package com.msa.chatlab.core.domain.model

data class OutboxPolicy(
    val enabled: Boolean = true,
    val maxQueueSize: Int = 1_000,
    val persistToDisk: Boolean = true,
    val inFlightLeaseMs: Long = 15_000,
    val flushBatchSize: Int = 16
)

data class RetryPolicy(
    val maxAttempts: Int = 5,
    val initialBackoffMs: Long = 500,
    val maxBackoffMs: Long = 30_000,
    val jitterRatio: Double = 0.2
)

enum class ReconnectBackoffMode { Fixed, Exponential }

data class ReconnectPolicy(
    val enabled: Boolean = true,
    val maxAttempts: Int = Int.MAX_VALUE,
    val backoffMs: Long = 2_000,
    val mode: ReconnectBackoffMode = ReconnectBackoffMode.Exponential,
    val maxBackoffMs: Long = 30_000,
    val jitterRatio: Double = 0.2,
    val resetAfterMs: Long = 30_000
)

/**
 * ✅ فاز 2.1: semantics واقعی پیام‌ها
 */
enum class DeliverySemantics { AtMostOnce, AtLeastOnce }

/**
 * ✅ فاز 2.1: ack strategy
 * - None: فقط send() موفق شد = تمام
 * - TransportLevel: send() موفق شد = تمام (مثل tcp/ws-level)
 * - ApplicationLevel: تا وقتی ACK (echo/ack app) نیاد، outbox حذف نمی‌شه
 */
sealed interface AckStrategy {
    data object None : AckStrategy
    data object TransportLevel : AckStrategy
    data class ApplicationLevel(val ackTimeoutMs: Long = 5_000) : AckStrategy
}

data class DeliveryPolicy(
    val semantics: DeliverySemantics = DeliverySemantics.AtLeastOnce,
    val ackStrategy: AckStrategy = AckStrategy.TransportLevel
)