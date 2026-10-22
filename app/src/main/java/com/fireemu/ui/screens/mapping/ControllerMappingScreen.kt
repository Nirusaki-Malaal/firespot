package com.fireemu.ui.screens.mapping

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import com.fireemu.ui.components.tvAction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.domain.models.KeyBinding
import com.fireemu.ui.components.HeaderPillButton
import com.fireemu.ui.theme.FireSpotAccentBlue
import com.fireemu.ui.theme.FireSpotBackgroundGradient
import com.fireemu.ui.theme.FireSpotGlassBorder
import com.fireemu.ui.theme.FireSpotGlassCard
import com.fireemu.ui.theme.FireSpotResumeBadge
import com.fireemu.ui.theme.FireSpotSearchFocusedBg
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSecondary

@Composable
fun ControllerMappingScreen(
    viewModel: MappingViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val bindings = state.bindings.filterKeys { it in listOf(com.fireemu.domain.models.RetroButton.A, com.fireemu.domain.models.RetroButton.B, com.fireemu.domain.models.RetroButton.X, com.fireemu.domain.models.RetroButton.Y, com.fireemu.domain.models.RetroButton.L, com.fireemu.domain.models.RetroButton.R, com.fireemu.domain.models.RetroButton.SELECT, com.fireemu.domain.models.RetroButton.START, com.fireemu.domain.models.RetroButton.UP, com.fireemu.domain.models.RetroButton.DOWN, com.fireemu.domain.models.RetroButton.LEFT, com.fireemu.domain.models.RetroButton.RIGHT) }.map { (button, code) -> KeyBinding(button, code) }
    val listeningAction = state.listeningForButton

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (state.listeningForButton != null && event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP) {
                    if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_BACK) viewModel.cancelListening()
                    else viewModel.bindKey(event.nativeKeyEvent.keyCode)
                    true
                } else state.listeningForButton != null
            }
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
                    text = "Controller & Remote Mapping",
                    color = FireSpotTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeaderPillButton(icon = "refresh", label = "Reset Defaults", onClick = { viewModel.resetDefaults() })
                    HeaderPillButton(icon = "back", label = "Back", onClick = onNavigateBack)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(state.statusMessage, color=FireSpotTextSecondary, fontSize=13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bindings, key = { it.button.id }) { binding ->
                    MockupBindingRow(
                        binding = binding,
                        isListening = listeningAction == binding.button,
                        onStartListening = { viewModel.startListeningFor(binding.button) }
                    )
                }
            }
        }
    }
}

@Composable
fun MockupBindingRow(
    binding: KeyBinding,
    isListening: Boolean,
    onStartListening: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isListening) { if (isListening) focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFocused) FireSpotSearchFocusedBg else FireSpotGlassCard)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FireSpotAccentBlue else FireSpotGlassBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .focusRequester(focusRequester)
            .tvAction(interactionSource, action = onStartListening)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = binding.button.label,
            color = FireSpotTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isListening) FireSpotResumeBadge else Color(0x333A3A3A))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isListening) "PRESS BUTTON NOW..." else android.view.KeyEvent.keyCodeToString(binding.primaryKeyCode),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
