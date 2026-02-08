package com.msa.chatlab.core.data.codec

import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) optString(key) else null

internal fun JSONObject.getStringOrDefault(key: String, default: String): String =
    if (has(key) && !isNull(key)) getString(key) else default

internal fun JSONObject.getLongOrDefault(key: String, default: Long): Long =
    if (has(key) && !isNull(key)) getLong(key) else default

internal fun JSONObject.getIntOrDefault(key: String, default: Int): Int =
    if (has(key) && !isNull(key)) getInt(key) else default

internal fun JSONObject.getDoubleOrDefault(key: String, default: Double): Double =
    if (has(key) && !isNull(key)) getDouble(key) else default

internal fun JSONObject.getBooleanOrDefault(key: String, default: Boolean): Boolean =
    if (has(key) && !isNull(key)) getBoolean(key) else default

internal fun JSONArray.toStringList(): List<String> =
    (0 until length()).map { getString(it) }

internal fun JSONObject.toStringMap(): Map<String, String> {
    val keys = keys()
    val map = mutableMapOf<String, String>()
    while (keys.hasNext()) {
        val k = keys.next()
        map[k] = getString(k)
    }
    return map
}

internal fun mapToJson(map: Map<String, String>): JSONObject {
    val o = JSONObject()
    map.forEach { (k, v) -> o.put(k, v) }
    return o
}

internal fun listToJson(list: List<String>): JSONArray {
    val a = JSONArray()
    list.forEach { a.put(it) }
    return a
}
