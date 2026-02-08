package com.msa.chatlab.core.domain.rules

import com.msa.chatlab.core.domain.model.Scenario

object ScenarioValidator {

    fun validate(s: Scenario): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (s.durationMs <= 0) errors += ValidationError.OutOfRange("scenario.durationMs", "durationMs باید > 0 باشد")
        if (s.messageRatePerSecond < 0) errors += ValidationError.OutOfRange("scenario.messageRatePerSecond", "rate نباید منفی باشد")
        if (s.burstSize < 0) errors += ValidationError.OutOfRange("scenario.burstSize", "burstSize نباید منفی باشد")

        s.customDisconnects.forEachIndexed { i, w ->
            if (w.atMsFromStart < 0) errors += ValidationError.OutOfRange("scenario.disconnect[$i].at", "atMsFromStart نباید منفی باشد")
            if (w.durationMs <= 0) errors += ValidationError.OutOfRange("scenario.disconnect[$i].duration", "durationMs باید > 0 باشد")
        }

        return if (errors.isEmpty()) ValidationResult.ok() else ValidationResult.fail(errors)
    }
}
