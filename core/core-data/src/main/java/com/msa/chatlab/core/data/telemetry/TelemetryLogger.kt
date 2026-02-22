package com.msa.chatlab.core.data.telemetry

import com.msa.chatlab.core.observability.log.AppLogger

class TelemetryLogger(private val logger: AppLogger) {

    fun logSend(messageId: String, destination: String, headers: Map<String, String>) {
        val trace = Trace.extract(headers)
        logger.i(
            "Telemetry",
            "SEND msg=$messageId dest=$destination trace=${trace?.traceId ?: "-"} span=${trace?.spanId ?: "-"}"
        )
    }

    fun logRecv(messageId: String, source: String?, headers: Map<String, String>) {
        val trace = Trace.extract(headers)
        logger.i(
            "Telemetry",
            "RECV msg=$messageId src=${source ?: "-"} trace=${trace?.traceId ?: "-"} span=${trace?.spanId ?: "-"}"
        )
    }
}