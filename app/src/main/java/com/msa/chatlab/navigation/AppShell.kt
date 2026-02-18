package com.msa.chatlab.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.msa.chatlab.core.common.ui.insets.AppContentInsets
import com.msa.chatlab.core.common.ui.messenger.UiEffect
import com.msa.chatlab.core.common.ui.messenger.UiMessenger
import com.msa.chatlab.feature.chat.route.ChatRootRoute
import com.msa.chatlab.feature.connect.route.ConnectRoute
import com.msa.chatlab.feature.debug.route.DebugRoute
import com.msa.chatlab.feature.lab.route.LabRoute
import com.msa.chatlab.feature.settings.route.SettingsRoute
import org.koin.androidx.compose.get

sealed class TopLevel(val route: String, val label: String, val icon: ImageVector) {
    object Chat : TopLevel("chat", "Chat", TopLevelDestination.Chat.icon)
    object Lab : TopLevel("lab", "Lab", TopLevelDestination.Lab.icon)
    object Connect : TopLevel("connect", "Connect", TopLevelDestination.Connect.icon)
    object Settings : TopLevel("settings", "Settings", TopLevelDestination.Profiles.icon)
    object Debug : TopLevel("debug", "Debug", Icons.Outlined.BugReport)
}

@Composable
fun AppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val messenger: UiMessenger = get()

    val items = listOf(TopLevel.Chat, TopLevel.Lab, TopLevel.Connect, TopLevel.Settings, TopLevel.Debug)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val snackHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        messenger.effects.collect { eff ->
            when (eff) {
                is UiEffect.Snackbar -> snackHost.showSnackbar(eff.message)
            }
        }
    }

    BoxWithConstraints(modifier) {
        val useRail = maxWidth >= 840.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackHost) },
            contentWindowInsets = AppContentInsets,
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        items.forEach { item ->
                            val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navController.navigate(item.route) { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Row(
                Modifier.fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding) // ✅ خیلی مهم
            ) {
                if (useRail) {
                    NavigationRail {
                        items.forEach { item ->
                            val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navController.navigate(item.route) { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = TopLevel.Chat.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(TopLevel.Chat.route) { ChatRootRoute(padding = PaddingValues(16.dp)) }
                    composable(TopLevel.Lab.route) { LabRoute(padding = PaddingValues(16.dp)) }
                    composable(TopLevel.Connect.route) {
                        ConnectRoute(
                            padding = PaddingValues(16.dp),
                            snackbarHostState = snackHost,
                            onGoSettings = { navController.navigate(TopLevel.Settings.route) }
                        )
                    }
                    composable(TopLevel.Settings.route) { SettingsRoute(padding = PaddingValues(16.dp), onGoLab= {}, onGoConnect= {}, onGoChat= {}, onGoDebug = {}) }
                    composable(TopLevel.Debug.route) { DebugRoute(onBack = { navController.popBackStack() }) }
                }
            }
        }
    }
}
