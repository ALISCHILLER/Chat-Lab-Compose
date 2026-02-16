package com.msa.chatlab.navigation

sealed class Destinations(val route: String) {
    object Settings : Destinations("settings")
    object Lab : Destinations("lab")
    object Connect : Destinations("connect")
    object Chat : Destinations("chat")
    object Debug : Destinations("debug")
}
