package com.msa.chatlab.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.msa.chatlab.featurechat.route.ChatRoute
import com.msa.chatlab.featureconnect.route.ConnectRoute
import com.msa.chatlab.featuredebug.route.DebugRoute
import com.msa.chatlab.featurelab.route.LabRoute
import com.msa.chatlab.featuresettings.route.SettingsRoute

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.Settings
    ) {
        composable(Destinations.Settings) {
            SettingsRoute(
                onGoLab = { navController.navigate(Destinations.Lab) },
                onGoConnect = { navController.navigate(Destinations.Connect) },
                onGoChat = { navController.navigate(Destinations.Chat) },
                onGoDebug = { navController.navigate(Destinations.Debug) }
            )
        }

        composable(Destinations.Lab) {
            LabRoute(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.Connect) {
            ConnectRoute(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.Chat) {
            ChatRoute(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.Debug) {
            DebugRoute(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
