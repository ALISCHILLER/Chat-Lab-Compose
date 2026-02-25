package com.msa.chatlab.core.domain.rules

import com.msa.chatlab.core.domain.model.Profile

object ProfileValidator {

    fun validate(profile: Profile): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (profile.name.isBlank()) {
            errors += ValidationError.Required("profile.name", "نام پروفایل اجباری است")
        }

        // Transport
        errors += ConfigValidator.validateTransport(profile.protocolType, profile.transportConfig).errors

        // Outbox
        if (profile.outboxPolicy.stallTimeoutMillis <= 0) {
            errors += ValidationError.OutOfRange("outbox.stallTimeoutMillis", "stallTimeoutMillis باید > 0 باشد")
        }

        // Retry
        if (profile.retryPolicy.maxAttempts < 0) {
            errors += ValidationError.OutOfRange("retry.maxAttempts", "maxAttempts نباید منفی باشد")
        }
        if (profile.retryPolicy.delayMillis < 0) {
            errors += ValidationError.OutOfRange("retry.delayMillis", "delayMillis نباید منفی باشد")
        }
        if (profile.retryPolicy.jitterRatio !in 0.0..1.0) {
            errors += ValidationError.OutOfRange("retry.jitterRatio", "jitterRatio باید بین 0 و 1 باشد")
        }

        return if (errors.isEmpty()) ValidationResult.ok() else ValidationResult.fail(errors)
    }
}
