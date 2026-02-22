package com.msa.chatlab.core.domain.model.device

interface DeviceInfoProvider {
    fun deviceModel(): String
    fun osVersion(): String
    fun networkLabel(): String
}
