package com.msa.chatlab.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.msa.chatlab.feature.chat.route.ChatRoute
import com.msa.chatlab.feature.connect.route.ConnectRoute
import com.msa.chatlab.feature.debug.route.DebugRoute
import com.msa.chatlab.feature.lab.route.LabRoute
import com.msa.chatlab.feature.settings.route.SettingsRoute

@Composable
fun RootNavGraph(padding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.Settings.route
    ) {
        composable(Destinations.Settings.route) {
            SettingsRoute(
                padding = padding,
                onGoLab = { navController.navigate(Destinations.Lab.route) },
                onGoConnect = { navController.navigate(Destinations.Connect.route) },
                onGoChat = { navController.navigate(Destinations.Chat.route) },
                onGoDebug = { navController.navigate(Destinations.Debug.route) }
            )
        }

        composable(Destinations.Lab.route) {
            LabRoute(padding = padding)
        }

        composable(Destinations.Connect.route) {
            ConnectRoute(
                padding = padding,
                onGoSettings = { navController.navigate(Destinations.Settings.route) }
            )
        }

        composable(Destinations.Chat.route) {
            ChatRoute()
        }

        composable(Destinations.Debug.route) {
            DebugRoute(onBack = { navController.popBackStack() })
        }
    }
}
