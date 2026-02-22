package com.msa.chatlab.core.nativebridge.device

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.msa.chatlab.core.domain.model.device.DeviceInfoProvider

class AndroidDeviceInfoProvider(private val context: Context) : DeviceInfoProvider {

    override fun deviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"

    override fun osVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    override fun networkLabel(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities?.let { "${it.linkDownstreamBandwidthKbps / 1000} Mbps" } ?: "N/A"
    }
}
