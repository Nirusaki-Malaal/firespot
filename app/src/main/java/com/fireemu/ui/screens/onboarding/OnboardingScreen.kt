package com.fireemu.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.ui.components.TvButton
import com.fireemu.ui.theme.FireSpotAccentWarm
import com.fireemu.ui.theme.FireSpotDarkNavy
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextWhite

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FireSpotDarkNavy)
            .padding(horizontal = 48.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚡",
                    fontSize = 40.sp,
                    color = FireSpotAccentWarm
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Welcome to FireSpot",
                    color = FireSpotTextWhite,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "The ultimate streaming-style retro gaming hub for Amazon Fire TV.",
                color = FireSpotTextSecondary,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isScanning) {
                Text(
                    text = "Scanning storage for games... ${uiState.foundGamesCount} titles indexed",
                    color = FireSpotAccentWarm,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (uiState.scanCompleted) {
                Text(
                    text = "Setup Complete! Found ${uiState.foundGamesCount} games ready to play.",
                    color = FireSpotTextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                TvButton(
                    text = "Start Browsing FireSpot",
                    onClick = {
                        viewModel.completeOnboarding()
                        onFinish()
                    },
                    isPrimary = true
                )
            } else {
                TvButton(
                    text = "Scan Default Storage & USB",
                    onClick = { viewModel.startScan() },
                    isPrimary = true
                )
            }
        }
    }
}
