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
        if (profile.outboxPolicy.maxQueueSize <= 0) {
            errors += ValidationError.OutOfRange("outbox.maxQueueSize", "maxQueueSize باید > 0 باشد")
        }

        // Retry
        if (profile.retryPolicy.maxAttempts < 0) {
            errors += ValidationError.OutOfRange("retry.maxAttempts", "maxAttempts نباید منفی باشد")
        }
        if (profile.retryPolicy.initialBackoffMs < 0) {
            errors += ValidationError.OutOfRange("retry.initialBackoffMs", "initialBackoffMs نباید منفی باشد")
        }
        if (profile.retryPolicy.maxBackoffMs < profile.retryPolicy.initialBackoffMs) {
            errors += ValidationError.Invalid("retry.maxBackoffMs", "maxBackoffMs باید >= initialBackoffMs باشد")
        }
        if (profile.retryPolicy.jitterRatio !in 0.0..1.0) {
            errors += ValidationError.OutOfRange("retry.jitterRatio", "jitterRatio باید بین 0 و 1 باشد")
        }

        // Payload
        if (profile.payloadProfile.targetSizeBytes <= 0) {
            errors += ValidationError.OutOfRange("payload.targetSizeBytes", "targetSizeBytes باید > 0 باشد")
        }

        // Chaos
        val chaos = profile.chaosProfile
        if (chaos.dropRatePercent !in 0.0..100.0) {
            errors += ValidationError.OutOfRange("chaos.dropRatePercent", "dropRatePercent باید بین 0 و 100 باشد")
        }
        if (chaos.delayMinMs < 0 || chaos.delayMaxMs < 0) {
            errors += ValidationError.OutOfRange("chaos.delay", "delayMin/Max نباید منفی باشد")
        }
        if (chaos.delayMaxMs < chaos.delayMinMs) {
            errors += ValidationError.Invalid("chaos.delay", "delayMax باید >= delayMin باشد")
        }

        return if (errors.isEmpty()) ValidationResult.ok() else ValidationResult.fail(errors)
    }
}
