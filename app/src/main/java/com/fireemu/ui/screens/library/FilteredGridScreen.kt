package com.fireemu.ui.screens.library

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.domain.models.Game
import com.fireemu.ui.components.EmptyState
import com.fireemu.ui.components.FireSpotLeftRail
import com.fireemu.ui.components.HeaderPillButton
import com.fireemu.ui.components.LeftRailTab
import com.fireemu.ui.components.MockupContinuePlayingCard
import com.fireemu.ui.theme.FireSpotBackgroundGradient
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSecondary

@Composable
fun FilteredGridScreen(
    title: String,
    subtitle: String,
    activeRailTab: LeftRailTab,
    games: List<Game>,
    onSelectGame: (Long) -> Unit,
    onSelectRailTab: (LeftRailTab) -> Unit,
    emptyMessage: String = "No games found in this collection."
) {
    var sortMode by remember { mutableStateOf("A-Z") }
    val sortedGames = when (sortMode) {
        "Recent" -> games.sortedByDescending { it.lastPlayedTimestamp }
        "Added" -> games.sortedByDescending { it.dateAddedTimestamp }
        else -> games.sortedBy { it.cleanTitle }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FireSpotBackgroundGradient)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            FireSpotLeftRail(
                selectedTab = activeRailTab,
                onTabSelected = onSelectRailTab
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 27.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            color = FireSpotTextPrimary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = FireSpotTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassSortChip("A-Z", isSelected = sortMode == "A-Z", onClick = { sortMode = "A-Z" })
                        GlassSortChip("Recent", isSelected = sortMode == "Recent", onClick = { sortMode = "Recent" })
                        GlassSortChip("Added", isSelected = sortMode == "Added", onClick = { sortMode = "Added" })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (sortedGames.isEmpty()) {
                    EmptyState(
                        title = "Empty Collection",
                        message = emptyMessage,
                        actionLabel = "Back to Home",
                        onActionClick = { onSelectRailTab(LeftRailTab.HOME) }
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 36.dp)
                    ) {
                        items(sortedGames, key = { it.id }) { game ->
                            MockupContinuePlayingCard(
                                game = game,
                                isMostRecentOrHasState = false,
                                onClick = { onSelectGame(game.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
