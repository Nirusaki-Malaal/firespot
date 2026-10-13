package com.fireemu.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fireemu.domain.models.Game
import com.fireemu.ui.components.EmptyState
import com.fireemu.ui.components.FireSpotLeftRail
import com.fireemu.ui.components.LeftRailTab
import com.fireemu.ui.components.LoadingState
import com.fireemu.ui.theme.FireSpotAccentBlue
import com.fireemu.ui.theme.FireSpotGlassBorder
import com.fireemu.ui.theme.FireSpotGlassCard
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextSubtitle

/**
 * Game Details Screen: Pixel-faithful replica of the Mario/Bowser hero mockup:
 * - Left 72dp slim navigation rail with brand logo top
 * - Breadcrumbs at top: CONSOLES > NES > SUPER MARIO BROS. 🎮
 * - Big bold title with trailing period: SUPER MARIO BROS.
 * - Metadata line: NES • 1985 • Platformer • 1 Player
 * - Full synopsis text
 * - 2-row button grid:
 *   Row 1: [▶ PLAY NOW] (focused gold gradient with blue glow) [▶ RESUME] [💾 SAVE STATES]
 *   Row 2: [🎮 CONTROLLER] [⚙ SETTINGS] [❤ ADD TO FAVORITES]
 * - Right full-bleed hero character backdrop with soft fade into the left navy background
 */
@Composable
fun GameDetailsSheet(
    viewModel: GameDetailsViewModel,
    onLaunchGame: (Game, Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingState(message = "Loading title...")
        return
    }

    val game = uiState.game
    if (game == null) {
        EmptyState(
            title = "Title Unavailable",
            message = "This game could not be found in your library.",
            actionLabel = "Back to Home",
            onActionClick = onNavigateBack
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF0A1226), Color(0xFF0E1A33), Color(0xFF162A52))
                )
            )
    ) {
        // Right side: Full-bleed Hero Character Backdrop Artwork
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.55f)
                .align(Alignment.CenterEnd)
        ) {
            if (!game.coverArtPath.isNullOrEmpty()) {
                AsyncImage(
                    model = game.coverArtPath,
                    contentDescription = game.cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Left fade gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0A1226), Color(0x990A1226), Color.Transparent)
                        )
                    )
            )
        }

        // Layout: Left Slim Navigation Rail + Content Area
        Row(modifier = Modifier.fillMaxSize()) {
            FireSpotLeftRail(
                selectedTab = LeftRailTab.GAMES,
                onTabSelected = { onNavigateBack() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 48.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Breadcrumbs Top Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CONSOLES",
                        color = FireSpotTextSubtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateBack() }
                    )
                    Text(text = ">", color = Color(0xFF8FA0C0), fontSize = 12.sp)
                    Text(
                        text = game.console.shortName,
                        color = FireSpotTextSubtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateBack() }
                    )
                    Text(text = ">", color = Color(0xFF8FA0C0), fontSize = 12.sp)
                    Text(
                        text = "${game.cleanTitle.uppercase()}.",
                        color = FireSpotTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x66000000))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(text = "🎮", fontSize = 10.sp)
                    }
                }

                // Middle Content: Title, Metadata, Synopsis, Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .padding(vertical = 12.dp)
                ) {
                    // Big Bold Title with Period
                    Text(
                        text = "${game.cleanTitle.uppercase()}.",
                        color = FireSpotTextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 48.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metadata Line
                    Text(
                        text = "${game.console.shortName} • ${game.releaseYear} • ${game.genre} • ${game.maxPlayers} Player",
                        color = Color(0xFFCBD5EA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Synopsis
                    Text(
                        text = game.description,
                        color = FireSpotTextSubtitle,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 2-Row Action Buttons Grid (Matches Mockup)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Top Row: [▶ PLAY NOW] [▶ RESUME] [💾 SAVE STATES]
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MockupGoldPlayButton(
                                onClick = { onLaunchGame(game, false) }
                            )

                            MockupDetailPillButton(
                                icon = "▶",
                                label = "RESUME",
                                onClick = { onLaunchGame(game, true) }
                            )

                            MockupDetailPillButton(
                                icon = "💾",
                                label = "SAVE STATES",
                                onClick = { /* open states */ }
                            )
                        }

                        // Bottom Row: [🎮 CONTROLLER] [⚙ SETTINGS] [❤ ADD TO FAVORITES]
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MockupDetailPillButton(
                                icon = "🎮",
                                label = "CONTROLLER",
                                onClick = { /* open remap */ }
                            )

                            MockupDetailPillButton(
                                icon = "⚙",
                                label = "SETTINGS",
                                onClick = { /* open settings */ }
                            )

                            MockupDetailPillButton(
                                icon = if (uiState.isFavorite) "❤" else "♡",
                                label = if (uiState.isFavorite) "IN FAVORITES" else "ADD TO FAVORITES",
                                onClick = { viewModel.toggleFavorite() }
                            )
                        }
                    }
                }

            }
        }
    }
}

/**
 * Focused Gold Play Button with electric blue focus glow
 */
@Composable
fun MockupGoldPlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                drawRoundRect(
                    color = Color(0x8C3D8BFF),
                    topLeft = Offset(-3.dp.toPx(), -3.dp.toPx()),
                    size = Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            .border(2.dp, FireSpotAccentBlue, RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFAD961), Color(0xFFF76B1C))
                ),
                RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "▶", color = Color(0xFF12141A), fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "PLAY NOW", color = Color(0xFF12141A), fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

/**
 * Translucent Glass Action Pill Button
 */
@Composable
fun MockupDetailPillButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FireSpotAccentBlue else FireSpotGlassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (isFocused) Color(0x331E3C7A) else FireSpotGlassCard,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, color = FireSpotTextPrimary, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = FireSpotTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
