package com.msa.chatlab.core.observability.export

import com.msa.chatlab.core.observability.log.LogEntry
import org.json.JSONArray
import org.json.JSONObject

object LogExporters {

    fun toJson(entries: List<LogEntry>): String {
        val arr = JSONArray()
        entries.forEach { e ->
            val o = JSONObject()
            o.put("ts", e.ts)
            o.put("level", e.level.name)
            o.put("tag", e.tag)
            o.put("message", e.message)
            val ctx = JSONObject()
            e.context.forEach { (k, v) -> ctx.put(k, v) }
            o.put("context", ctx)
            e.throwable?.let { o.put("throwable", it.javaClass.name + ": " + (it.message ?: "")) }
            arr.put(o)
        }
        return arr.toString(2)
    }
}
