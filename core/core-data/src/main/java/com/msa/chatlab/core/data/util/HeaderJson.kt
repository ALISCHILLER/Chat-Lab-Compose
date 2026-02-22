package com.msa.chatlab.core.data.util

import org.json.JSONObject

object HeaderJson {
    fun encode(map: Map<String, String>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    fun decode(json: String?): Map<String, String> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val keys = obj.keys()
            buildMap {
                while (keys.hasNext()) {
                    val k = keys.next()
                    put(k, obj.optString(k, ""))
                }
            }
        }.getOrDefault(emptyMap())
    }
}