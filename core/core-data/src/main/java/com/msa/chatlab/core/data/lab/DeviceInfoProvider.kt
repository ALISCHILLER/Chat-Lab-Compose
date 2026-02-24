package com.msa.chatlab.core.data.lab

/**
 * ✅ Single source of truth: core-domain
 * This avoids DI mismatches (ScenarioExecutor expects core-data type; DI binds core-domain type).
 */
@Deprecated(
    message = "Use com.msa.chatlab.core.domain.model.device.DeviceInfoProvider",
    replaceWith = ReplaceWith(
        "DeviceInfoProvider",
        "com.msa.chatlab.core.domain.model.device.DeviceInfoProvider"
    )
)
typealias DeviceInfoProvider = com.msa.chatlab.core.domain.model.device.DeviceInfoProvider
