package com.msa.chatlab.feature.chat.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.msa.chatlab.feature.chat.screen.ChatListRoute
import com.msa.chatlab.feature.chat.screen.ChatThreadRoute

@Composable
fun ChatRootRoute(padding: PaddingValues) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "chat/list") {
        composable("chat/list") {
            ChatListRoute(padding = padding, onOpen = { dest ->
                nav.navigate("chat/thread/$dest")
            })
        }
        composable("chat/thread/{dest}") { backStack ->
            val dest = backStack.arguments?.getString("dest") ?: "default"
            ChatThreadRoute(padding = padding, destination = dest, onBack = { nav.popBackStack() })
        }
    }
}
