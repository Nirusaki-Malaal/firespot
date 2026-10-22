package com.fireemu.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Consoles : Screen("consoles")
    object Games : Screen("games")
    object Favorites : Screen("favorites")
    object Categories : Screen("categories")
    object Library : Screen("library/{consoleId}") {
        fun createRoute(consoleId: String) = "library/$consoleId"
    }
    object Details : Screen("details/{gameId}") {
        fun createRoute(gameId: Long) = "details/$gameId"
    }
    object Discover : Screen("discover")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object ControllerMapping : Screen("controller_mapping")
    object Onboarding : Screen("onboarding")
}
