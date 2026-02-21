package com.msa.chatlab.core.domain.rules

import com.msa.chatlab.core.domain.model.Scenario

object ScenarioValidator {

    fun validate(s: Scenario): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (s.durationSec <= 0) errors += ValidationError.OutOfRange("scenario.durationSec", "durationSec باید > 0 باشد")
        if (s.rps < 0) errors += ValidationError.OutOfRange("scenario.rps", "rps نباید منفی باشد")

        return if (errors.isEmpty()) ValidationResult.ok() else ValidationResult.fail(errors)
    }
}
