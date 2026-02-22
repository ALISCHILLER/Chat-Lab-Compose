package com.msa.chatlab.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.msa.chatlab.bootstrap.StartupNoticeStore
import com.msa.chatlab.core.common.ui.UiEffect
import com.msa.chatlab.core.common.ui.UiMessenger
import com.msa.chatlab.core.common.ui.insets.AppContentInsets
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
    val noticeStore: StartupNoticeStore = get()

    val items = listOf(TopLevel.Chat, TopLevel.Lab, TopLevel.Connect, TopLevel.Settings, TopLevel.Debug)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val snackHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        noticeStore.consume()?.let { msg -> snackHost.showSnackbar(msg) }
        messenger.effects.collect { eff ->
            when (eff) {
                is UiEffect.Snackbar -> snackHost.showSnackbar(eff.message)
            }
        }
    }

    fun navTop(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
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
                                onClick = { navTop(item.route) },
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
                    .consumeWindowInsets(padding)
            ) {
                if (useRail) {
                    NavigationRail {
                        items.forEach { item ->
                            val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navTop(item.route) },
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
                            onGoSettings = { navTop(TopLevel.Settings.route) }
                        )
                    }

                    // ✅ FIX: SettingsRoute فقط padding می‌گیرد
                    composable(TopLevel.Settings.route) { SettingsRoute(padding = PaddingValues(16.dp)) }

                    // ✅ FIX: DebugRoute فقط padding می‌گیرد
                    composable(TopLevel.Debug.route) { DebugRoute(padding = PaddingValues(16.dp)) }
                }
            }
        }
    }
}
