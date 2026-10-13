package com.fireemu.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fireemu.domain.models.ConsoleType
import com.fireemu.domain.models.Game
import com.fireemu.ui.theme.FireSpotResumeBadge
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextSubtitle
import com.fireemu.ui.theme.FireSpotTitleCyan
import com.fireemu.ui.theme.getConsoleColor

/**
 * ROW 1: Wide Glass Console Card (~340dp x 108dp, radius 14dp)
 * - 4dp colored left edge + faint matching gradient tint across card (fading out left to right)
 * - Console short name bold 18sp top-left (white)
 * - "N Titles" 13sp top-right in cyan #4FD1FF
 * - Full console name 13sp #8FA0C0 at bottom-left
 * - Focus: scale 1.06, 2dp accent border + glow, left edge brightens
 */
@Composable
fun MockupConsoleCard(
    console: ConsoleType,
    titleCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = getConsoleColor(console.id)

    TvFocusableCard(
        onClick = onClick,
        focusedScale = 1.06f,
        cornerRadiusDp = 14f,
        modifier = modifier
            .width(340.dp)
            .height(108.dp)
    ) { isFocused ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Faint matching gradient tint from left to right
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = if (isFocused) 0.35f else 0.18f),
                                accentColor.copy(alpha = if (isFocused) 0.12f else 0.04f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 4dp colored left edge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(if (isFocused) accentColor else accentColor.copy(alpha = 0.85f))
            )

            // Card content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Short name + "N Titles"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = console.shortName,
                        color = FireSpotTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$titleCount Titles",
                        color = FireSpotTitleCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Bottom: Full console name
                Text(
                    text = console.displayName,
                    color = Color(0xFF8FA0C0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * ROW 2: Continue Playing (Recent) Card (~215dp x 270dp, 4:5 portrait, radius 14dp)
 * - Full-bleed cover art (ContentScale.Crop)
 * - Bottom gradient scrim (transparent -> rgba(0,0,0,0.85), ~45% height)
 * - On scrim: Console tag (11sp, #CBD5EA, uppercase), Game title (16sp bold white, max 2 lines),
 *   Console name again (12sp #9AA7C2)
 * - Focused: scale 1.08, 2dp #3D8BFF border, outer glow, elevated with zIndex
 * - Bottom-right corner: blue triangular badge (#2F6BFF) with small gamepad icon (resume available)
 */
@Composable
fun MockupContinuePlayingCard(
    game: Game,
    isMostRecentOrHasState: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        focusedScale = 1.08f,
        cornerRadiusDp = 14f,
        modifier = modifier
            .width(215.dp)
            .height(270.dp)
    ) { isFocused ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed cover art or fallback
            if (!game.coverArtPath.isNullOrEmpty()) {
                AsyncImage(
                    model = game.coverArtPath,
                    contentDescription = game.cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                FallbackCover(
                    game = game,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom gradient scrim (~45% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.50f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC000000), Color(0xEE000000))
                        )
                    )
            )

            // Text content on scrim
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = game.console.shortName.uppercase(),
                    color = Color(0xFFCBD5EA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = game.cleanTitle,
                    color = FireSpotTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = game.console.displayName,
                    color = Color(0xFF9AA7C2),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Blue triangular/rounded corner badge (#2F6BFF) with gamepad icon on focused card
            if (isFocused && isMostRecentOrHasState) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FireSpotResumeBadge)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🎮 RESUME",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * ROW 3: Favorites Card (~170dp x 128dp, 4:3 landscape-ish, radius 12dp)
 * - Cover/logo art centered on dark tinted background
 * - Console tag tiny top-left (10sp)
 * - Title centered at bottom (14sp bold, up to 2 lines) over light bottom scrim
 */
@Composable
fun MockupFavoritesCard(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        focusedScale = 1.06f,
        cornerRadiusDp = 12f,
        modifier = modifier
            .width(170.dp)
            .height(128.dp)
    ) { isFocused ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (!game.coverArtPath.isNullOrEmpty()) {
                AsyncImage(
                    model = game.coverArtPath,
                    contentDescription = game.cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                FallbackCover(
                    game = game,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Console tag tiny top-left (10sp)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = game.console.shortName,
                    color = Color(0xFFCBD5EA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Light bottom scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC000000), Color(0xFA000000))
                        )
                    )
            )

            // Title centered at bottom (14sp bold, up to 2 lines)
            Text(
                text = game.cleanTitle,
                color = FireSpotTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun FallbackCover(
    game: Game,
    modifier: Modifier = Modifier
) {
    val col = getConsoleColor(game.console.id)
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(col.copy(alpha = 0.55f), Color(0xFF0F182B))
                )
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎮",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = game.cleanTitle,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
