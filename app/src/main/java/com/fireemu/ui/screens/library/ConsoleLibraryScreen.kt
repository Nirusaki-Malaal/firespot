package com.fireemu.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.fireemu.ui.components.etchedBackdrop
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.domain.models.ConsoleType
import com.fireemu.ui.components.EmptyState
import com.fireemu.ui.components.HeaderPillButton
import com.fireemu.ui.components.LoadingState
import com.fireemu.ui.components.MockupContinuePlayingCard
import com.fireemu.ui.theme.FireSpotAccentBlue
import com.fireemu.ui.theme.FireSpotBackgroundGradient
import com.fireemu.ui.theme.FireSpotBrandAmberTop
import com.fireemu.ui.theme.FireSpotGlassBorder
import com.fireemu.ui.theme.FireSpotGlassCard
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTitleCyan
import com.fireemu.ui.theme.getConsoleColor

/**
 * Console Library Screen:
 * - Header with console color accent bar + name + "N Titles"
 * - Chips for sort (A-Z, Recent, Added)
 * - Poster grid using Continue Playing card style (4:5 portrait), 6 columns
 */
@Composable
fun ConsoleLibraryScreen(
    viewModel: LibraryViewModel,
    onSelectGame: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val firstFocus = remember { FocusRequester() }
    val uiState by viewModel.uiState.collectAsState()
    var filterSortMode by remember { mutableStateOf("A-Z") }

    if (uiState.isLoading) {
        LoadingState(message = "Browsing titles...")
        return
    }

    val console = uiState.console
    val accentColor = getConsoleColor(console.id)

    val sortedGames = when (filterSortMode) {
        "Recent" -> uiState.games.sortedByDescending { it.lastPlayedTimestamp }
        "Added" -> uiState.games.sortedByDescending { it.dateAddedTimestamp }
        else -> uiState.games.sortedBy { it.cleanTitle }
    }

    LaunchedEffect(sortedGames.isNotEmpty()) { if(sortedGames.isNotEmpty()) firstFocus.requestFocus() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .etchedBackdrop()
            .padding(horizontal = 48.dp, vertical = 27.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row: Console Accent Bar + Name + "N Titles" + Sort Chips + Back
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Colored accent bar
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = console.displayName,
                                color = FireSpotTextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "${uiState.games.size} Titles",
                                color = FireSpotTitleCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Core: ${console.coreLibName}",
                            color = FireSpotTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Sort Chips & Back
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassSortChip("A-Z", isSelected = filterSortMode == "A-Z", onClick = { filterSortMode = "A-Z" })
                    GlassSortChip("Recent", isSelected = filterSortMode == "Recent", onClick = { filterSortMode = "Recent" })
                    GlassSortChip("Added", isSelected = filterSortMode == "Added", onClick = { filterSortMode = "Added" })
                    HeaderPillButton(icon = "back", label = "Back", onClick = onNavigateBack)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6-Column Poster Grid
            if (sortedGames.isEmpty()) {
                EmptyState(
                    title = "No Games Found",
                    message = "No ROM files detected for ${console.displayName}. Check ROM folders in Settings.",
                    actionLabel = "Open Settings",
                    onActionClick = onNavigateSettings
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(220.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 36.dp)
                ) {
                    items(sortedGames, key = { it.id }) { game ->
                        MockupContinuePlayingCard(
                            game = game,
                            isMostRecentOrHasState = false,
                                modifier = if(game.id == sortedGames.first().id) Modifier.focusRequester(firstFocus) else Modifier,
                            onClick = { onSelectGame(game.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassSortChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    isFocused -> Color(0x33838383)
                    isSelected -> Color(0x1F838383)
                    else -> FireSpotGlassCard
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FireSpotAccentBlue else FireSpotGlassBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected || isFocused) FireSpotTextPrimary else FireSpotTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}
