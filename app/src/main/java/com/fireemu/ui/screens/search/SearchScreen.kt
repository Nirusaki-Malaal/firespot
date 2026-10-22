package com.fireemu.ui.screens.search

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.ui.components.EmptyState
import com.fireemu.ui.components.HeaderPillButton
import com.fireemu.ui.components.LoadingState
import com.fireemu.ui.components.MockupContinuePlayingCard
import com.fireemu.ui.components.TvKeyboard
import com.fireemu.ui.theme.FireSpotBackgroundGradient
import com.fireemu.ui.theme.FireSpotBrandAmberTop
import com.fireemu.ui.theme.FireSpotGlassCard
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextSubtitle

/**
 * Search Screen:
 * - On-screen D-pad keyboard left, results grid right, glass styling
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onSelectGame: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                val key=event.nativeKeyEvent
                val typed=key.unicodeChar
                val editing=key.keyCode==android.view.KeyEvent.KEYCODE_DEL || (typed>=32 && !key.isCtrlPressed && !key.isAltPressed)
                if(editing) {
                    if(key.action==android.view.KeyEvent.ACTION_DOWN) {
                        if(key.keyCode==android.view.KeyEvent.KEYCODE_DEL) viewModel.backspace() else viewModel.appendChar(typed.toChar())
                    }
                    true
                } else false
            }
            .background(FireSpotBackgroundGradient)
            .padding(horizontal = 48.dp, vertical = 27.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "",
                        fontSize = 24.sp,
                        color = FireSpotBrandAmberTop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search Library",
                        color = FireSpotTextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Box(
                        modifier = Modifier
                            .width(380.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(FireSpotGlassCard)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (uiState.query.isEmpty()) "Type game name on keyboard..." else uiState.query,
                            color = if (uiState.query.isEmpty()) FireSpotTextSecondary else FireSpotTextPrimary,
                            fontSize = 15.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeaderPillButton(icon = "back", label = "Back", onClick = onNavigateBack)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Two Columns: Keyboard Left, Results Right
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                ) {
                    TvKeyboard(
                        onChar = { viewModel.appendChar(it) },
                        onBackspace = { viewModel.backspace() },
                        onSpace = { viewModel.appendSpace() },
                        onClear = { viewModel.clearQuery() }
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (uiState.isSearching) {
                        LoadingState(message = "Searching...")
                    } else if (uiState.results.isEmpty() && uiState.query.isNotEmpty()) {
                        EmptyState(
                            title = "No Matches Found",
                            message = "Try searching with a shorter keyword.",
                            actionLabel = "Clear Query",
                            onActionClick = { viewModel.clearQuery() }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(210.dp),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(uiState.results, key = { it.id }) { game ->
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
}
