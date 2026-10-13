package com.fireemu.ui.screens.settings

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.domain.models.AudioResampling
import com.fireemu.domain.models.VideoScaling
import com.fireemu.ui.components.HeaderPillButton
import com.fireemu.ui.theme.FireSpotAccentBlue
import com.fireemu.ui.theme.FireSpotBackgroundGradient
import com.fireemu.ui.theme.FireSpotGlassBorder
import com.fireemu.ui.theme.FireSpotGlassCard
import com.fireemu.ui.theme.FireSpotSearchFocusedBg
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextSubtitle
import com.fireemu.ui.theme.FireSpotTitleCyan

enum class MockupSettingsTab(val label: String, val icon: String) {
    STORAGE("Storage & Scanner", "📁"),
    VIDEO("Video & Scaling", "📺"),
    AUDIO("Audio Resampling", "🔊"),
    CONTROLLER("Controller Mapping", "🎮"),
    ABOUT("About FireSpot", "ℹ")
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateMapping: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanMsg by viewModel.scanMessage.collectAsState()
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(MockupSettingsTab.STORAGE) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FireSpotBackgroundGradient)
            .padding(horizontal = 48.dp, vertical = 27.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FireSpot Settings",
                    color = FireSpotTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                HeaderPillButton(
                    icon = "←",
                    label = "Back to Home",
                    onClick = onNavigateBack
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Two Panels: Left List + Right Glass Card
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Left Tabs List
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MockupSettingsTab.values().forEach { tab ->
                        SettingsNavTab(
                            tab = tab,
                            isSelected = tab == currentTab,
                            onClick = { currentTab = tab }
                        )
                    }
                }

                // Right Glass Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FireSpotGlassCard)
                        .border(1.dp, FireSpotGlassBorder, RoundedCornerShape(14.dp))
                        .padding(28.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentTab) {
                        MockupSettingsTab.STORAGE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "ROM FOLDERS & SCANNER", color = FireSpotTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Monitors local storage and USB OTG drives for game files.", color = FireSpotTextSecondary, fontSize = 14.sp)
                                Text(text = "Paths: ${settings.romDirectories.joinToString(", ")}", color = FireSpotTitleCyan, fontSize = 14.sp)
                                HeaderPillButton(
                                    icon = "🔍",
                                    label = if (isScanning) "Scanning..." else "Scan ROMs Now",
                                    onClick = { viewModel.triggerScan() }
                                )
                                if (scanMsg != null) {
                                    Text(text = scanMsg ?: "", color = FireSpotTextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                        MockupSettingsTab.VIDEO -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "VIDEO & DISPLAY", color = FireSpotTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Scaling: ${settings.videoScaling.label}", color = FireSpotTextPrimary, fontSize = 15.sp)
                                    HeaderPillButton(icon = "📺", label = "Cycle Scaling", onClick = {
                                        val next = when (settings.videoScaling) {
                                            VideoScaling.ASPECT_RATIO -> VideoScaling.INTEGER
                                            VideoScaling.INTEGER -> VideoScaling.STRETCH
                                            VideoScaling.STRETCH -> VideoScaling.ASPECT_RATIO
                                        }
                                        viewModel.updateVideoScaling(next)
                                    })
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Scanlines: ${if (settings.scanlinesEnabled) "ON" else "OFF"}", color = FireSpotTextPrimary, fontSize = 15.sp)
                                    HeaderPillButton(icon = "⚡", label = "Toggle Filter", onClick = { viewModel.toggleScanlines() })
                                }
                            }
                        }
                        MockupSettingsTab.AUDIO -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "LOW-LATENCY AUDIO", color = FireSpotTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Resampling: ${settings.audioResampling.label}", color = FireSpotTextPrimary, fontSize = 15.sp)
                                    HeaderPillButton(icon = "🔊", label = "Change Resampler", onClick = {
                                        val next = when (settings.audioResampling) {
                                            AudioResampling.SINC_LOWER -> AudioResampling.SINC_HIGHER
                                            AudioResampling.SINC_HIGHER -> AudioResampling.NEAREST
                                            AudioResampling.NEAREST -> AudioResampling.SINC_LOWER
                                        }
                                        viewModel.updateAudioResampling(next)
                                    })
                                }
                            }
                        }
                        MockupSettingsTab.CONTROLLER -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "CONTROLLER MAPPING", color = FireSpotTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Remap Alexa Voice Remote and Bluetooth gamepad buttons.", color = FireSpotTextSecondary, fontSize = 14.sp)
                                HeaderPillButton(icon = "🎮", label = "Open Remap Screen", onClick = onNavigateMapping)
                            }
                        }
                        MockupSettingsTab.ABOUT -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(text = "ABOUT FIRESPOT", color = FireSpotTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "FireSpot TV Edition • Version 1.0.0", color = FireSpotTitleCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Integrated Cores: FCEUmm (NES), Gambatte (GB/GBC), mGBA (GBA), PCSX ReARMed (PS1), PPSSPP (PSP).", color = FireSpotTextSecondary, fontSize = 13.sp)
                                HeaderPillButton(
                                    icon = "📜",
                                    label = "View Licenses",
                                    onClick = {
                                        val intent = Intent(context, LicensesActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsNavTab(
    tab: MockupSettingsTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isFocused -> FireSpotSearchFocusedBg
                    isSelected -> Color(0x331E3C7A)
                    else -> FireSpotGlassCard
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FireSpotAccentBlue else FireSpotGlassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = tab.icon, fontSize = 16.sp, color = FireSpotTextPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = tab.label,
            color = if (isSelected || isFocused) FireSpotTextPrimary else FireSpotTextSecondary,
            fontSize = 14.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}
