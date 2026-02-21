package com.msa.chatlab.feature.lab.mapper

import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.core.domain.model.ScenarioPreset

fun ScenarioPreset.toDataScenario(): Scenario {
    return when (this) {
        ScenarioPreset.Stable -> Scenario(name = "Stable", durationSec = 60, rps = 10, payloadBytes = 1024, pattern = "steady")
        ScenarioPreset.Intermittent -> Scenario(name = "Intermittent", durationSec = 60, rps = 5, payloadBytes = 1024, pattern = "steady")
        ScenarioPreset.OfflineBurst -> Scenario(name = "OfflineBurst", durationSec = 60, rps = 20, payloadBytes = 1024, pattern = "burst")
        ScenarioPreset.Lossy -> Scenario(name = "Lossy", durationSec = 60, rps = 10, payloadBytes = 1024, pattern = "steady")
        ScenarioPreset.LoadBurst -> Scenario(name = "LoadBurst", durationSec = 60, rps = 100, payloadBytes = 1024, pattern = "burst")
        ScenarioPreset.Custom -> TODO("Custom scenario preset not yet supported in the lab")
    }
}
