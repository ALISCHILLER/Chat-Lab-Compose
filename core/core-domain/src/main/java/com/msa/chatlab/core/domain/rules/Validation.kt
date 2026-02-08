package com.msa.chatlab.core.domain.rules

sealed class ValidationError(open val field: String, open val message: String) {
    data class Required(override val field: String, override val message: String) : ValidationError(field, message)
    data class Invalid(override val field: String, override val message: String) : ValidationError(field, message)
    data class OutOfRange(override val field: String, override val message: String) : ValidationError(field, message)
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    companion object {
        fun ok() = ValidationResult(true)
        fun fail(errors: List<ValidationError>) = ValidationResult(false, errors)
    }
}
