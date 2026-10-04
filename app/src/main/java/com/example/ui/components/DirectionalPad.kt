package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BraviaBlue
import com.example.ui.theme.BraviaBlueGlow
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight

@Composable
fun DirectionalPad(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dpadSize = 220.dp
    val buttonSize = 52.dp

    Box(
        modifier = modifier
            .size(dpadSize)
            .shadow(elevation = 12.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(DarkSurfaceCard)
            .border(2.dp, ButtonBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Subtle decorative rings
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .border(1.dp, DarkSurfaceHighlight, CircleShape)
        )

        // UP
        TactileButton(
            onClick = onUp,
            icon = Icons.Default.KeyboardArrowUp,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp)
                .size(width = 64.dp, height = buttonSize),
            testTag = "dpad_up"
        )

        // DOWN
        TactileButton(
            onClick = onDown,
            icon = Icons.Default.KeyboardArrowDown,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
                .size(width = 64.dp, height = buttonSize),
            testTag = "dpad_down"
        )

        // LEFT
        TactileButton(
            onClick = onLeft,
            icon = Icons.Default.KeyboardArrowLeft,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 10.dp)
                .size(width = buttonSize, height = 64.dp),
            testTag = "dpad_left"
        )

        // RIGHT
        TactileButton(
            onClick = onRight,
            icon = Icons.Default.KeyboardArrowRight,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp)
                .size(width = buttonSize, height = 64.dp),
            testTag = "dpad_right"
        )

        // CENTER OK BUTTON
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
                .border(1.5.dp, BraviaBlue.copy(alpha = 0.6f), CircleShape)
        ) {
            TactileButton(
                onClick = onConfirm,
                label = "OK",
                backgroundColor = Color.Transparent,
                borderColor = Color.Transparent,
                contentColor = BraviaBlueGlow,
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp),
                testTag = "dpad_ok"
            )
        }
    }
}
