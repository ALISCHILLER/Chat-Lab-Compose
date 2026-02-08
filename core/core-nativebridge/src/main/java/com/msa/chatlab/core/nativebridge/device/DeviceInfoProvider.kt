package com.msa.chatlab.core.nativebridge.device

interface DeviceInfoProvider {
    fun deviceModel(): String
    fun osVersion(): String
    fun networkLabel(): String
}
