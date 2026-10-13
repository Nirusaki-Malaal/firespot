package com.fireemu.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.domain.models.ConsoleType
import com.fireemu.ui.components.ErrorState
import com.fireemu.ui.components.FireSpotHeader
import com.fireemu.ui.components.FireSpotLeftRail
import com.fireemu.ui.components.LeftRailTab
import com.fireemu.ui.components.LoadingState
import com.fireemu.ui.components.MockupConsoleCard
import com.fireemu.ui.components.MockupContinuePlayingCard
import com.fireemu.ui.components.MockupFavoritesCard
import com.fireemu.ui.theme.FireSpotBackgroundGradient
import com.fireemu.ui.theme.FireSpotSectionLabel

/**
 * Pixel-faithful FireSpot Home Screen matching reference image:
 * - Left: Slim vertical rail (72dp wide), icons only
 * - Top: Brand header with amber logo + Search & Settings pill buttons
 * - Section labels: Uppercase, 13sp, letter-spacing 1.2sp, weight 600, color #B8C2D9
 * - ROW 1: CONSOLES wide glass cards (~340dp x 108dp)
 * - ROW 2: CONTINUE PLAYING (RECENT) portrait cards (~215dp x 270dp)
 * - ROW 3: FAVORITES landscape-ish cards (~170dp x 128dp)
 * - Lower rows: RECENTLY ADDED, and per-console rows
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSelectConsole: (String) -> Unit,
    onSelectGame: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectRailTab: (LeftRailTab) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()

    if (uiState.isLoading) {
        LoadingState(message = "Loading FireSpot library...")
        return
    }

    if (uiState.error != null) {
        ErrorState(
            message = uiState.error ?: "Failed to load library",
            onRetry = { viewModel.loadData() }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FireSpotBackgroundGradient)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Far Left: Slim Vertical Navigation Rail (72dp wide)
            FireSpotLeftRail(
                selectedTab = LeftRailTab.HOME,
                onTabSelected = onSelectRailTab
            )

            // Content Area (with 48dp overscan-safe margins, vertical scroll)
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp)
            ) {
                // Header: Logo wordmark left, Search & Settings pills right
                item(key = "home_header") {
                    FireSpotHeader(
                        onOpenSearch = onOpenSearch,
                        onOpenSettings = onOpenSettings
                    )
                }

                // ROW 1: CONSOLES (Wide glass cards ~340dp x 108dp, 14dp gap)
                item(key = "section_consoles") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle("CONSOLES")
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 48.dp)
                        ) {
                            items(ConsoleType.values(), key = { it.id }) { console ->
                                val count = uiState.consoleCounts[console.id] ?: 0
                                MockupConsoleCard(
                                    console = console,
                                    titleCount = count,
                                    onClick = { onSelectConsole(console.id) }
                                )
                            }
                        }
                    }
                }

                // ROW 2: CONTINUE PLAYING (RECENT) (~215dp x 270dp, 16dp gap)
                item(key = "section_continue_playing") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle("CONTINUE PLAYING (RECENT)")
                        Spacer(modifier = Modifier.height(12.dp))
                        val recents = if (uiState.recentGames.isNotEmpty()) {
                            uiState.recentGames.take(15)
                        } else {
                            uiState.allGames.take(10)
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 48.dp)
                        ) {
                            items(recents, key = { "recent_${it.id}" }) { game ->
                                MockupContinuePlayingCard(
                                    game = game,
                                    isMostRecentOrHasState = true,
                                    onClick = { onSelectGame(game.id) }
                                )
                            }
                        }
                    }
                }

                // ROW 3: FAVORITES (~170dp x 128dp, 14dp gap)
                item(key = "section_favorites") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle("FAVORITES")
                        Spacer(modifier = Modifier.height(12.dp))
                        val favs = if (uiState.favoriteGames.isNotEmpty()) {
                            uiState.favoriteGames
                        } else {
                            uiState.allGames.reversed().take(12)
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 48.dp)
                        ) {
                            items(favs, key = { "fav_${it.id}" }) { game ->
                                MockupFavoritesCard(
                                    game = game,
                                    onClick = { onSelectGame(game.id) }
                                )
                            }
                        }
                    }
                }

                // ROW 4: RECENTLY ADDED (~170dp x 128dp, 14dp gap)
                item(key = "section_recently_added") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionTitle("RECENTLY ADDED")
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 48.dp)
                        ) {
                            items(uiState.recentlyAdded, key = { "added_${it.id}" }) { game ->
                                MockupFavoritesCard(
                                    game = game,
                                    onClick = { onSelectGame(game.id) }
                                )
                            }
                        }
                    }
                }

                // ROW 5+: Per-Console Rows using Favorites card style
                ConsoleType.values().forEach { console ->
                    val cGames = uiState.allGames.filter { it.consoleId.equals(console.id, ignoreCase = true) }
                    if (cGames.isNotEmpty()) {
                        item(key = "section_console_${console.id}") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SectionTitle("${console.shortName.uppercase()} GAMES")
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    contentPadding = PaddingValues(horizontal = 48.dp)
                                ) {
                                    items(cGames, key = { "c_${console.id}_${it.id}" }) { game ->
                                        MockupFavoritesCard(
                                            game = game,
                                            onClick = { onSelectGame(game.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = FireSpotSectionLabel,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold, // 600
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(horizontal = 48.dp)
    )
}
