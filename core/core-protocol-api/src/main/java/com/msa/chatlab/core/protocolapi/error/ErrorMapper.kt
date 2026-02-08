package com.msa.chatlab.core.protocolapi.error

fun interface ErrorMapper {
    fun map(throwable: Throwable): TransportError
}
