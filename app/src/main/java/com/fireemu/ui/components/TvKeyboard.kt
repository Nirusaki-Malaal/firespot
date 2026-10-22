package com.fireemu.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.ui.theme.BgDark
import com.fireemu.ui.theme.CardBg
import com.fireemu.ui.theme.FocusBorder
import com.fireemu.ui.theme.TextPrimary

@Composable
fun TvKeyboard(
    onChar: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstFocus = remember {FocusRequester()}
    LaunchedEffect(Unit) {firstFocus.requestFocus()}
    val rows = listOf(
        listOf('A', 'B', 'C', 'D', 'E', 'F'),
        listOf('G', 'H', 'I', 'J', 'K', 'L'),
        listOf('M', 'N', 'O', 'P', 'Q', 'R'),
        listOf('S', 'T', 'U', 'V', 'W', 'X'),
        listOf('Y', 'Z', '0', '1', '2', '3'),
        listOf('4', '5', '6', '7', '8', '9')
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowChars ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowChars.forEach { char ->
                    TvKey(
                        label = char.toString(),
                        modifier = if(char == 'A') Modifier.focusRequester(firstFocus) else Modifier,
                        onClick = { onChar(char) }
                    )
                }
            }
        }

        // Functional action keys row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            TvKey(
                label = "SPACE",
                widthDp = 104,
                onClick = onSpace
            )
            TvKey(
                label = "DEL",
                widthDp = 76,
                onClick = onBackspace
            )
            TvKey(
                label = "CLEAR",
                widthDp = 92,
                onClick = onClear
            )
        }
    }
}

@Composable
private fun TvKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    widthDp: Int = 44
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .size(width = widthDp.dp, height = 44.dp)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusBorder else Color(0x33FFFFFF),
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                color = if (isFocused) FocusBorder else CardBg,
                shape = RoundedCornerShape(6.dp)
            )
            .tvAction(interactionSource,action=onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isFocused) BgDark else TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
