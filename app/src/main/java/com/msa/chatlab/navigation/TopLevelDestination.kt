package com.msa.chatlab.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import com.msa.chatlab.R

sealed class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    data object Profiles : TopLevelDestination("profiles", R.string.profiles, Icons.Outlined.Settings)
    data object Connect  : TopLevelDestination("connect",  R.string.connect,  Icons.Outlined.Wifi)
    data object Chat     : TopLevelDestination("chat",     R.string.chat,     Icons.Outlined.Chat)
    data object Lab      : TopLevelDestination("lab",      R.string.lab,      Icons.Outlined.Science)
}

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination.Profiles,
    TopLevelDestination.Connect,
    TopLevelDestination.Chat,
    TopLevelDestination.Lab
)
