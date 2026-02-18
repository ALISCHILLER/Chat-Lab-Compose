package com.msa.chatlab.core.domain.error

sealed class AppError(val message: String) {
    class Network(message: String) : AppError(message)
    class Protocol(message: String) : AppError(message)
    class Validation(message: String) : AppError(message)
    class Storage(message: String) : AppError(message)
    class Unknown(message: String) : AppError(message)
}
