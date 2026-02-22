package com.msa.chatlab.feature.lab.mapper

import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.core.domain.model.ScenarioPreset

fun ScenarioPreset.toDataScenario(): Scenario {
    return when (this) {
        ScenarioPreset.Stable -> Scenario("Stable", 60, 10, 1024, "steady")
        ScenarioPreset.Intermittent -> Scenario("Intermittent", 60, 5, 1024, "intermittent")
        ScenarioPreset.OfflineBurst -> Scenario("Offline Burst", 60, 20, 1024, "offline_burst")
        ScenarioPreset.Lossy -> Scenario("Lossy", 60, 10, 1024, "lossy")
        ScenarioPreset.LoadBurst -> Scenario("Load Burst", 60, 100, 1024, "burst")

        // ✅ قبلا TODO → کرش قطعی
        ScenarioPreset.Custom -> Scenario("Custom", 30, 8, 512, "steady")
    }
}
