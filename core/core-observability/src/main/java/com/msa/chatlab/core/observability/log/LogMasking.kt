package com.msa.chatlab.core.observability.log

private val SENSITIVE_KEYS = setOf(
    "password",
    "token",
    "authorization",
    "x-api-key",
    "secret"
)

fun mask(context: Map<String, String>): Map<String, String> {
    if (context.isEmpty()) return emptyMap()

    val masked = mutableMapOf<String, String>()
    for ((key, value) in context) {
        masked[key] = if (SENSITIVE_KEYS.contains(key.lowercase())) "***" else value
    }
    return masked
}
