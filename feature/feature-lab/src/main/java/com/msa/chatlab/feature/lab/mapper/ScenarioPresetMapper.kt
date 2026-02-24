package com.msa.chatlab.feature.lab.mapper

import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.core.domain.model.ScenarioPreset

fun toDataScenario(preset: ScenarioPreset): Scenario {
    return when (preset) {
        ScenarioPreset.Stable -> Scenario(
            name = "Stable Load",
            durationSec = 60,
            rps = 1,
            payloadBytes = 256,
            pattern = "steady"
        )
        ScenarioPreset.Intermittent -> Scenario(
            name = "Intermittent Connectivity",
            durationSec = 120,
            rps = 1,
            payloadBytes = 128,
            pattern = "intermittent"
        )
        ScenarioPreset.OfflineBurst -> Scenario(
            name = "Offline Burst",
            durationSec = 30,
            rps = 10,
            payloadBytes = 1024,
            pattern = "burst"
        )
        ScenarioPreset.Lossy -> Scenario(
            name = "Lossy Network",
            durationSec = 60,
            rps = 2,
            payloadBytes = 64,
            pattern = "steady"
        )
        ScenarioPreset.LoadBurst -> Scenario(
            name = "Load Burst",
            durationSec = 15,
            rps = 20,
            payloadBytes = 512,
            pattern = "burst"
        )
        ScenarioPreset.Custom -> Scenario(
            name = "Custom",
            durationSec = 60,
            rps = 1,
            payloadBytes = 128,
            pattern = "steady"
        )
    }
}
