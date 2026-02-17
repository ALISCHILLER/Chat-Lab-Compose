package com.msa.chatlab.feature.lab.mapper

import com.msa.chatlab.core.domain.model.ScenarioPreset as DomainScenarioPreset
import com.msa.chatlab.core.data.lab.Scenario as DataScenario

fun DomainScenarioPreset.toDataPreset(): DataScenario.Preset {
    return when (this) {
        DomainScenarioPreset.Stable -> DataScenario.Preset.Stable
        DomainScenarioPreset.Intermittent -> DataScenario.Preset.Intermittent
        DomainScenarioPreset.OfflineBurst -> DataScenario.Preset.OfflineBurst
        DomainScenarioPreset.Lossy -> DataScenario.Preset.Lossy
        DomainScenarioPreset.LoadBurst -> DataScenario.Preset.LoadBurst
        DomainScenarioPreset.Custom -> TODO("Custom scenario preset not yet supported in the lab")
    }
}
