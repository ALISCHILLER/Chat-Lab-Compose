package com.msa.chatlab.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.msa.chatlab.feature.chat.route.ChatRoute
import com.msa.chatlab.feature.connect.route.ConnectRoute
import com.msa.chatlab.feature.lab.route.LabRoute
import com.msa.chatlab.feature.settings.route.SettingsRoute
import com.msa.chatlab.ui.ChatLabAppState

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appState: ChatLabAppState,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Profiles.route
    ) {
        composable(TopLevelDestination.Profiles.route) {
            SettingsRoute(
                onGoLab = { navController.navigate(TopLevelDestination.Lab.route) },
                onGoConnect = { navController.navigate(TopLevelDestination.Connect.route) },
                onGoChat = { navController.navigate(TopLevelDestination.Chat.route) },
                onGoDebug = { /* TODO */ }
            )
        }
        composable(TopLevelDestination.Connect.route) {
            ConnectRoute(
                padding = padding,
                snackbarHostState = appState.snackbarHostState,
                onGoSettings = { navController.navigate(TopLevelDestination.Profiles.route) }
            )
        }
        composable(TopLevelDestination.Chat.route) {
            ChatRoute(padding = padding)
        }
        composable(TopLevelDestination.Lab.route) {
            LabRoute(padding = padding)
        }
    }
}
