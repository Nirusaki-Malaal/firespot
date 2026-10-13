package com.fireemu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fireemu.domain.models.Game
import com.fireemu.ui.theme.FireSpotAccentWarm
import com.fireemu.ui.theme.FireSpotBlue
import com.fireemu.ui.theme.FireSpotFocusBorder
import com.fireemu.ui.theme.FireSpotSurfaceCard
import com.fireemu.ui.theme.FireSpotSurfaceCardHover
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextWhite
import com.fireemu.ui.theme.FireSpotViolet

/**
 * 2:3 Standard Poster Card (Hotstar style)
 */
@Composable
fun FireSpotPosterCard(
    game: Game,
    rank: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        modifier = modifier
            .width(if (rank != null) 210.dp else 165.dp)
            .height(250.dp)
    ) { isFocused, isExpandedPreview ->
        Row(modifier = Modifier.fillMaxSize()) {
            if (rank != null) {
                // Large Top-10 Rank Numeral beside card
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "$rank",
                        color = if (isFocused) FireSpotAccentWarm else FireSpotTextSecondary.copy(alpha = 0.5f),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Cover Image or Fallback
                if (!game.coverArtPath.isNullOrEmpty()) {
                    AsyncImage(
                        model = game.coverArtPath,
                        contentDescription = game.cleanTitle,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FireSpotArtworkPlaceholder(game = game)
                }

                // Bottom Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC0B0F1A), Color(0xF00B0F1A))
                            )
                        )
                )

                // Info overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = game.cleanTitle,
                        color = FireSpotTextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = game.console.shortName,
                            color = FireSpotAccentWarm,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " • ${game.genre}",
                            color = FireSpotTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    // Mini preview state when focused for 800ms
                    if (isExpandedPreview) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "▶ PLAY",
                                color = FireSpotBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " • ★ ${game.rating}",
                                color = FireSpotAccentWarm,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 16:9 Landscape Card (Continue Playing with progress bar)
 */
@Composable
fun FireSpotLandscapeCard(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        modifier = modifier
            .width(260.dp)
            .height(150.dp)
    ) { isFocused, isExpandedPreview ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (!game.coverArtPath.isNullOrEmpty()) {
                AsyncImage(
                    model = game.coverArtPath,
                    contentDescription = game.cleanTitle,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                FireSpotArtworkPlaceholder(game = game)
            }

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x990B0F1A), Color(0xF20B0F1A))
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FireSpotAccentWarm)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RESUME",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = game.console.shortName,
                        color = FireSpotTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.cleanTitle,
                    color = FireSpotTextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Simulated progress bar from play time
                Spacer(modifier = Modifier.height(6.dp))
                val progressFraction = (game.playTimeSeconds % 3600).toFloat() / 3600f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x40FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceIn(0.2f, 0.95f))
                            .fillMaxHeight()
                            .background(FireSpotAccentWarm)
                    )
                }
            }
        }
    }
}

/**
 * Console Logo Tile (Browse by Console row)
 */
@Composable
fun FireSpotConsoleTile(
    title: String,
    gameCount: Int,
    accentHex: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        modifier = modifier
            .width(200.dp)
            .height(110.dp)
    ) { isFocused, _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(accentHex).copy(alpha = 0.35f), FireSpotSurfaceCard)
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$gameCount Titles",
                            color = FireSpotTextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = title,
                    color = FireSpotTextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * High-quality procedural artwork fallback (Disney+ Hotstar themed)
 */
@Composable
fun FireSpotArtworkPlaceholder(
    game: Game,
    modifier: Modifier = Modifier
) {
    val consoleColor = Color(game.console.themeColorHex)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(consoleColor.copy(alpha = 0.8f), FireSpotSurfaceCard)
                )
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x33000000))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = game.console.shortName,
                    color = FireSpotAccentWarm,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = game.cleanTitle,
                color = FireSpotTextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
