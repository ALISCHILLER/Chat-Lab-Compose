package com.msa.chatlab.core.data.telemetry

import java.util.UUID

object TelemetryHeaders {
    const val TRACE_ID = "x-trace-id"
    const val SPAN_ID = "x-span-id"
    const val PARENT_SPAN_ID = "x-parent-span-id"
    const val IDEMPOTENCY_KEY = "x-idempotency-key"
}

data class TraceContext(
    val traceId: String,
    val spanId: String,
    val parentSpanId: String? = null
)

object Trace {
    fun newRoot(): TraceContext = TraceContext(
        traceId = uuidNoDash(),
        spanId = spanId(),
        parentSpanId = null
    )

    fun childOf(parent: TraceContext): TraceContext = TraceContext(
        traceId = parent.traceId,
        spanId = spanId(),
        parentSpanId = parent.spanId
    )

    fun inject(headers: MutableMap<String, String>, ctx: TraceContext) {
        headers[TelemetryHeaders.TRACE_ID] = ctx.traceId
        headers[TelemetryHeaders.SPAN_ID] = ctx.spanId
        ctx.parentSpanId?.let { headers[TelemetryHeaders.PARENT_SPAN_ID] = it }
    }

    fun extract(headers: Map<String, String>): TraceContext? {
        val t = headers[TelemetryHeaders.TRACE_ID] ?: return null
        val s = headers[TelemetryHeaders.SPAN_ID] ?: return null
        val p = headers[TelemetryHeaders.PARENT_SPAN_ID]
        return TraceContext(traceId = t, spanId = s, parentSpanId = p)
    }

    private fun spanId(): String = uuidNoDash().take(16)
    private fun uuidNoDash(): String = UUID.randomUUID().toString().replace("-", "")
}