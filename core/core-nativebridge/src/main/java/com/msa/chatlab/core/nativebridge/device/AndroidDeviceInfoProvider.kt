package com.msa.chatlab.core.nativebridge.device

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.msa.chatlab.core.data.lab.DeviceInfoProvider

class AndroidDeviceInfoProvider(
    private val context: Context
) : DeviceInfoProvider {

    override fun deviceModel(): String = Build.MODEL ?: "unknown"
    override fun osVersion(): String = Build.VERSION.RELEASE ?: "unknown"

    override fun networkLabel(): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val n = cm.activeNetwork ?: return "none"
            val caps = cm.getNetworkCapabilities(n) ?: return "unknown"
            when {
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "other"
            }
        } catch (_: Exception) {
            "unknown"
        }
    }
}
