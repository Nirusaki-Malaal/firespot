package com.fireemu.ui.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fireemu.FireEmuApp
import com.fireemu.domain.models.ConsoleType
import com.fireemu.domain.models.Game
import com.fireemu.game.EmulatorActivity
import com.fireemu.ui.components.ExitConfirmDialog
import com.fireemu.ui.components.LeftRailTab
import com.fireemu.ui.screens.details.GameDetailsSheet
import com.fireemu.ui.screens.details.GameDetailsViewModel
import com.fireemu.ui.screens.home.HomeScreen
import com.fireemu.ui.screens.home.HomeViewModel
import com.fireemu.ui.screens.library.ConsoleLibraryScreen
import com.fireemu.ui.screens.library.FilteredGridScreen
import com.fireemu.ui.screens.library.LibraryViewModel
import com.fireemu.ui.screens.mapping.ControllerMappingScreen
import com.fireemu.ui.screens.mapping.MappingViewModel
import com.fireemu.ui.screens.onboarding.OnboardingScreen
import com.fireemu.ui.screens.onboarding.OnboardingViewModel
import com.fireemu.ui.screens.search.SearchScreen
import com.fireemu.ui.screens.search.SearchViewModel
import com.fireemu.ui.screens.settings.SettingsScreen
import com.fireemu.ui.screens.settings.SettingsViewModel
import com.fireemu.ui.screens.splash.SplashScreen
import com.fireemu.ui.screens.splash.SplashViewModel

@Composable
fun FireEmuNavGraph(
    navController: NavHostController,
    onExitApp: () -> Unit
) {
    val app = FireEmuApp.instance
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    fun launchGame(game: Game, resumeState: Boolean) {
        val intent = Intent(context, EmulatorActivity::class.java).apply {
            putExtra(EmulatorActivity.EXTRA_CONSOLE_ID, game.consoleId)
            putExtra(EmulatorActivity.EXTRA_ROM_PATH, game.romPath)
            putExtra(EmulatorActivity.EXTRA_GAME_ID, game.id)
            putExtra(EmulatorActivity.EXTRA_GAME_TITLE, game.cleanTitle)
            putExtra(EmulatorActivity.EXTRA_RESUME, resumeState)
        }
        context.startActivity(intent)
    }

    fun handleRailNavigation(tab: LeftRailTab) {
        when (tab) {
            LeftRailTab.HOME -> navController.navigate(Screen.Home.route)
            LeftRailTab.LIBRARY -> navController.navigate(Screen.Games.route)
            LeftRailTab.CONSOLES -> navController.navigate(Screen.Library.createRoute("gameboy"))
            LeftRailTab.GAMES -> navController.navigate(Screen.Favorites.route)
            LeftRailTab.SETTINGS -> navController.navigate(Screen.Settings.route)
        }
    }

    if (showExitDialog) {
        ExitConfirmDialog(
            onConfirmExit = {
                showExitDialog = false
                onExitApp()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            val viewModel = remember { SplashViewModel(app.settingsRepository) }
            SplashScreen(
                viewModel = viewModel,
                onNavigate = { target ->
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen
        composable(Screen.Onboarding.route) {
            val viewModel = remember { OnboardingViewModel(app.settingsRepository, app.scanRomsUseCase) }
            OnboardingScreen(
                viewModel = viewModel,
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen (Matches Mockup Reference)
        composable(Screen.Home.route) {
            BackHandler {
                showExitDialog = true
            }

            val viewModel = remember { HomeViewModel(app.getGamesUseCase) }
            HomeScreen(
                viewModel = viewModel,
                onSelectConsole = { consoleId ->
                    navController.navigate(Screen.Library.createRoute(consoleId))
                },
                onSelectGame = { gameId ->
                    navController.navigate(Screen.Details.createRoute(gameId))
                },
                onOpenDiscover = { navController.navigate(Screen.Discover.route) },
                onOpenSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onSelectRailTab = { handleRailNavigation(it) }
            )
        }

        // 4. Consoles Screen (Grid)
        composable(Screen.Consoles.route) {
            BackHandler { navController.navigate(Screen.Home.route) }
            val viewModel = remember { HomeViewModel(app.getGamesUseCase) }
            val uiState by viewModel.uiState.collectAsState()
            FilteredGridScreen(
                title = "All Consoles",
                subtitle = "Browse by Emulated System",
                activeRailTab = LeftRailTab.CONSOLES,
                games = uiState.allGames,
                onSelectGame = { navController.navigate(Screen.Details.createRoute(it)) },
                onSelectRailTab = { handleRailNavigation(it) }
            )
        }

        // 5. Games Screen (All titles Grid)
        composable(Screen.Games.route) {
            BackHandler { navController.navigate(Screen.Home.route) }
            val viewModel = remember { HomeViewModel(app.getGamesUseCase) }
            val uiState by viewModel.uiState.collectAsState()
            FilteredGridScreen(
                title = "Your library",
                subtitle = "Installed games. Ready when you are.",
                activeRailTab = LeftRailTab.LIBRARY,
                games = uiState.allGames,
                onSelectGame = { navController.navigate(Screen.Details.createRoute(it)) },
                onSelectRailTab = { handleRailNavigation(it) }
            )
        }

        // 6. Favorites Screen
        composable(Screen.Favorites.route) {
            BackHandler { navController.navigate(Screen.Home.route) }
            val viewModel = remember { HomeViewModel(app.getGamesUseCase) }
            val uiState by viewModel.uiState.collectAsState()
            FilteredGridScreen(
                title = "Favorites",
                subtitle = "Saved Titles",
                activeRailTab = LeftRailTab.GAMES,
                games = uiState.favoriteGames,
                onSelectGame = { navController.navigate(Screen.Details.createRoute(it)) },
                onSelectRailTab = { handleRailNavigation(it) },
                emptyMessage = "No favorite games added yet."
            )
        }

        // 7. Console Library Screen
        composable(
            route = Screen.Library.route,
            arguments = listOf(navArgument("consoleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val consoleId = backStackEntry.arguments?.getString("consoleId") ?: "nes"
            val viewModel = remember(consoleId) { LibraryViewModel(consoleId, app.getGamesUseCase) }
            ConsoleLibraryScreen(
                viewModel = viewModel,
                onSelectGame = { gameId ->
                    navController.navigate(Screen.Details.createRoute(gameId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 8. Game Details Sheet
        composable(
            route = Screen.Details.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L
            val viewModel = remember(gameId) {
                GameDetailsViewModel(
                    gameId = gameId,
                    gameRepository = app.gameRepository,
                    toggleFavoriteUseCase = app.toggleFavoriteUseCase,
                    saveStateUseCase = app.saveStateUseCase
                )
            }
            GameDetailsSheet(
                viewModel = viewModel,
                onLaunchGame = { game, resumeState ->
                    launchGame(game, resumeState)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Search Screen
        composable(Screen.Search.route) {
            val viewModel = remember { SearchViewModel(app.getGamesUseCase) }
            SearchScreen(
                viewModel = viewModel,
                onSelectGame = { gameId ->
                    navController.navigate(Screen.Details.createRoute(gameId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Discover.route) {
            com.fireemu.ui.screens.discover.DiscoverScreen(
                onOpenGame = {navController.navigate(Screen.Details.createRoute(it))},
                onImportGame = {navController.navigate(Screen.Settings.route)},
                onBack = {navController.popBackStack()}
            )
        }

        // 10. Settings Screen
        composable(Screen.Settings.route) {
            val viewModel = remember {
                SettingsViewModel(
                    settingsUseCase = app.settingsUseCase,
                    scanRomsUseCase = app.scanRomsUseCase
                )
            }
            SettingsScreen(
                viewModel = viewModel,
                onOpenDiscover = {navController.navigate(Screen.Discover.route)},
                onNavigateMapping = {
                    navController.navigate(Screen.ControllerMapping.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Controller Mapping Screen
        composable(Screen.ControllerMapping.route) {
            val viewModel = remember { MappingViewModel(app.settingsRepository) }
            ControllerMappingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
