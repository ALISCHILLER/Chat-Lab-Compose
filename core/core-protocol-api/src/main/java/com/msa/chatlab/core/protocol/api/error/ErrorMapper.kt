package com.msa.chatlab.core.protocol.api.error

fun interface ErrorMapper {
    fun map(throwable: Throwable): TransportError
}
