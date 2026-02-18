package com.msa.chatlab.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.msa.chatlab.navigation.TOP_LEVEL_DESTINATIONS

@Composable
fun AppBottomBar(appState: ChatLabAppState) {
    NavigationBar {
        TOP_LEVEL_DESTINATIONS.forEach { dest ->
            val selected = appState.currentDestination?.route == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = { appState.navigate(dest) },
                icon = { Icon(dest.icon, null) },
                label = { Text(stringResource(dest.labelRes)) }
            )
        }
    }
}
