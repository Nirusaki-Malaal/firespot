package com.fireemu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fireemu.domain.models.Game
import com.fireemu.ui.theme.FireSpotAccentGradient
import com.fireemu.ui.theme.FireSpotAccentWarm
import com.fireemu.ui.theme.FireSpotBlue
import com.fireemu.ui.theme.FireSpotDarkNavy
import com.fireemu.ui.theme.FireSpotHeroBottomScrim
import com.fireemu.ui.theme.FireSpotHeroScrim
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextWhite
import kotlinx.coroutines.delay

/**
 * FireSpot Hero Carousel replicating Disney+ Hotstar TV:
 * - Full-width, ~55% screen height
 * - Large backdrop art of featured game
 * - Dark gradient scrim on left and bottom
 * - FireSpot-style logo tag, big game title, one-line description, metadata chips
 * - Buttons: [Play] (filled accent), [+ Favorites], [Details]
 * - Auto-advances every 6s, pauses when focus moves into it
 * - Dot / segment indicators
 */
@Composable
fun FireSpotHeroCarousel(
    featuredGames: List<Game>,
    onPlayGame: (Game) -> Unit,
    onToggleFavorite: (Game) -> Unit,
    onOpenDetails: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredGames.isEmpty()) return

    var currentIndex by remember { mutableStateOf(0) }
    var isCarouselFocused by remember { mutableStateOf(false) }

    // Auto-advance every 6s unless focused
    LaunchedEffect(isCarouselFocused, featuredGames.size) {
        while (true) {
            delay(6000)
            if (!isCarouselFocused && featuredGames.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % featuredGames.size
            }
        }
    }

    val currentGame = featuredGames[currentIndex.coerceIn(0, featuredGames.size - 1)]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .onFocusChanged { isCarouselFocused = it.hasFocus }
    ) {
        // Backdrop Image with Crossfade Animation (400ms)
        AnimatedContent(
            targetState = currentGame,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "firespot_hero_backdrop"
        ) { game ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (!game.coverArtPath.isNullOrEmpty()) {
                    AsyncImage(
                        model = game.coverArtPath,
                        contentDescription = game.cleanTitle,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterEnd)
                    )
                } else {
                    FireSpotArtworkPlaceholder(
                        game = game,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Left Dark Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FireSpotHeroScrim)
                )

                // Bottom Dark Scrim into content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FireSpotHeroBottomScrim)
                )
            }
        }

        // Hero Info & Call-To-Action Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.65f)
                .padding(start = 48.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // FireSpot Original Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "⚡ FIRESPOT FEATURED",
                    color = FireSpotAccentWarm,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "4K 60FPS",
                        color = FireSpotTextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Big Title (40sp+)
            Text(
                text = currentGame.cleanTitle,
                color = FireSpotTextWhite,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 48.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Metadata Chips (Console, Year, Genre, Rating, Players)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                MetadataBadge(text = currentGame.console.displayName)
                Spacer(modifier = Modifier.width(8.dp))
                MetadataBadge(text = "${currentGame.releaseYear}")
                Spacer(modifier = Modifier.width(8.dp))
                MetadataBadge(text = currentGame.genre)
                Spacer(modifier = Modifier.width(8.dp))
                MetadataBadge(text = "★ ${currentGame.rating}")
                Spacer(modifier = Modifier.width(8.dp))
                MetadataBadge(text = "${currentGame.maxPlayers} Players")
            }

            // Synopsis / Description
            Text(
                text = currentGame.description,
                color = FireSpotTextSecondary,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // Hero Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvButton(
                    text = "▶  Play Game",
                    onClick = { onPlayGame(currentGame) },
                    isPrimary = true
                )

                TvButton(
                    text = if (currentGame.isFavorite) "✓ In Favorites" else "+ Favorites",
                    onClick = { onToggleFavorite(currentGame) }
                )

                TvButton(
                    text = "Details",
                    onClick = { onOpenDetails(currentGame) }
                )
            }
        }

        // Dot / Segment Indicators (Right Bottom)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 48.dp, bottom = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            featuredGames.forEachIndexed { index, _ ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) FireSpotAccentWarm else Color(0x66FFFFFF))
                )
            }
        }
    }
}

@Composable
fun MetadataBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x2EFFFFFF))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = FireSpotTextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
