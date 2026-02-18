package com.msa.chatlab.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.msa.chatlab.navigation.TOP_LEVEL_DESTINATIONS
import com.msa.chatlab.navigation.TopLevelDestination

@Composable
fun rememberChatLabAppState(
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): ChatLabAppState {
    return remember(navController, snackbarHostState) {
        ChatLabAppState(navController, snackbarHostState)
    }
}

@Stable
class ChatLabAppState(
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState
) {
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val shouldShowBottomBar: Boolean
        @Composable get() {
            val route = currentDestination?.route
            return TOP_LEVEL_DESTINATIONS.any { it.route == route }
        }

    fun navigate(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
