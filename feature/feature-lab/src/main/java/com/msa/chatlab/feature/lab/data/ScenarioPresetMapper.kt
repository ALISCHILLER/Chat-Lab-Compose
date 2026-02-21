package com.msa.chatlab.feature.lab.data

import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.feature.lab.state.ScenarioPreset

fun ScenarioPreset.toDataScenario(): Scenario {
    return when (this) {
        ScenarioPreset.Stable -> Scenario("Stable", 60, 10, 1024, "steady")
        ScenarioPreset.Intermittent -> Scenario("Intermittent", 60, 5, 1024, "intermittent")
        ScenarioPreset.OfflineBurst -> Scenario("OfflineBurst", 60, 20, 1024, "offline_burst")
        ScenarioPreset.Lossy -> Scenario("Lossy", 60, 10, 1024, "steady")
        ScenarioPreset.LoadBurst -> Scenario("LoadBurst", 60, 100, 1024, "burst")
        ScenarioPreset.Custom -> Scenario("Custom", 30, 5, 512, "steady")
    }
}