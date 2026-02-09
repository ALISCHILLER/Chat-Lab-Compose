package com.msa.chatlab.core.protocolapi.error

import com.msa.chatlab.core.protocol.api.error.TransportError

fun interface ErrorMapper {
    fun map(throwable: Throwable): TransportError
}
