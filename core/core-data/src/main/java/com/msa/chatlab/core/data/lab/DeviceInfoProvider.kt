package com.msa.chatlab.core.data.lab

interface DeviceInfoProvider {
    fun deviceModel(): String
    fun osVersion(): String
    fun networkLabel(): String
}
