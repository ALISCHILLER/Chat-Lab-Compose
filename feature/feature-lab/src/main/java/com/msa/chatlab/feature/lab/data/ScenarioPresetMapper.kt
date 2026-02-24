package com.msa.chatlab.feature.lab.data

import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.core.domain.model.ScenarioPreset

/**
 * ✅ Backward-compatible wrapper.
 * Canonical mapper:
 * com.msa.chatlab.feature.lab.mapper.ScenarioPresetMapper
 */
@Deprecated(
    message = "Moved to com.msa.chatlab.feature.lab.mapper.toDataScenario",
    replaceWith = ReplaceWith(
        "this.toDataScenario()",
        "com.msa.chatlab.feature.lab.mapper.toDataScenario"
    )
)
fun ScenarioPreset.toDataScenario(): Scenario = 
    com.msa.chatlab.feature.lab.mapper.toDataScenario(this)
